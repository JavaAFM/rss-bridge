from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel
import torch
from transformers import AutoModel, AutoTokenizer, pipeline
from langdetect import detect
import asyncpg
from datetime import datetime, timedelta

# Проверяем доступность GPU
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"Using device: {device}")

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "user": "postgres",
    "password": "1234",
    "database": "RSS_Bridge"
}

# Загрузка моделей
model_name = 'Snowflake/snowflake-arctic-embed-l-v2.0'
ru_sentiment_model_name = 'cointegrated/rubert-tiny-sentiment-balanced'
kk_sentiment_model_name = 'issai/rembert-sentiment-analysis-polarity-classification-kazakh'

# Инициализация токенизатора и модели
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModel.from_pretrained(model_name, add_pooling_layer=False).to(device)  # Переносим модель на GPU

# Для моделей сентимента
ru_sentiment_analyzer = pipeline(
    'sentiment-analysis',
    model=ru_sentiment_model_name,
    tokenizer=ru_sentiment_model_name,
    device=0 if device.type == "cuda" else -1  # Используем GPU, если доступен
)
kk_sentiment_analyzer = pipeline(
    'sentiment-analysis',
    model=kk_sentiment_model_name,
    tokenizer=kk_sentiment_model_name,
    device=0 if device.type == "cuda" else -1  # Используем GPU, если доступен
)

model.eval()

app = FastAPI()

class Tag(BaseModel):
    tag: str

async def get_db_connection():
    try:
        return await asyncpg.connect(**DB_CONFIG)
    except Exception as e:
        print(f"Database connection error: {e}")
        raise HTTPException(status_code=500, detail="Database connection failed.")

def detect_language(text):
    try:
        return detect(text)
    except:
        return "ru"

def analyze_sentiments(text):
    lang = detect_language(text)
    if lang == "kk":
        res = kk_sentiment_analyzer(text[:1000])[0]  # Обрезаем текст для моделей с ограничениями
        is_negative = res['label'] == 'negative'
    else:
        res = ru_sentiment_analyzer(text[:1000])[0]
        is_negative = res['label'] == 'NEGATIVE'
    return {
        "is_negative": is_negative,
        "sentiment_score": res['score'],
        "lang": lang
    }

@app.post("/predict/")
async def predict(tag: Tag, db=Depends(get_db_connection)):
    try:
        last_week = datetime.utcnow() - timedelta(days=1)
        query = "SELECT id, news_body FROM news WHERE created_at >= $1"
        rows = await db.fetch(query, last_week)
        results = []

        # Токенизация тега один раз
        tag_with_prefix = f"query: {tag.tag}"
        tag_tokens = tokenizer(
            [tag_with_prefix],
            padding=True,
            truncation=True,
            max_length=512,
            return_tensors='pt'
        ).to(device)  # Переносим токены на GPU

        with torch.no_grad():
            tag_embeddings = model(**tag_tokens)[0][:, 0]
            tag_embeddings = torch.nn.functional.normalize(tag_embeddings, p=2, dim=1)

        for row in rows:
            text = row["news_body"]
            news_id = row["id"]

            try:
                # Токенизация текста
                text_with_prefix = f"passage: {text}"
                text_tokens = tokenizer(
                    [text_with_prefix],
                    padding=True,
                    truncation=True,
                    max_length=512,
                    return_tensors='pt'
                ).to(device)  # Переносим токены на GPU

                with torch.no_grad():
                    text_embeddings = model(**text_tokens)[0][:, 0]
                    text_embeddings = torch.nn.functional.normalize(text_embeddings, p=2, dim=1)

                similarity_score = torch.mm(tag_embeddings, text_embeddings.transpose(0, 1)).item()

                if similarity_score > 0.25:
                    sentiment_result = analyze_sentiments(text)

                    results.append({
                        "news_id": news_id,
                        "tag": tag.tag,
                        "text": text[:500] + "..." if len(text) > 500 else text,
                        "similarity_score": similarity_score,
                        "is_negative": sentiment_result["is_negative"],
                        "sentiment_score": sentiment_result["sentiment_score"],
                        "lang": sentiment_result["lang"]
                    })

                # Очистка памяти GPU после обработки каждого текста
                torch.cuda.empty_cache()

            except Exception as e:
                print(f"Error processing text (ID: {news_id}): {str(e)}")
                continue

        await db.close()
        return results

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze_sentiment/")
async def sentiment_endpoint(text: str):
    try:
        sentiment_result = analyze_sentiments(text)
        return {
            "text": text,
            "is_negative": sentiment_result["is_negative"],
            "sentiment_score": sentiment_result["sentiment_score"],
            "lang": sentiment_result["lang"]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# uvicorn afm:app --host 0.0.0.0 --port 8000 --reload