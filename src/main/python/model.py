from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
from transformers import AutoModel, AutoTokenizer, pipeline

# Загрузка моделей и токенизаторов
model_name = 'Snowflake/snowflake-arctic-embed-l-v2.0'
sentiment_model_name = 'cointegrated/rubert-tiny-sentiment-balanced'

tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModel.from_pretrained(model_name, add_pooling_layer=False)
sentiment_analyzer = pipeline('sentiment-analysis', model=sentiment_model_name, tokenizer=sentiment_model_name)
model.eval()

app = FastAPI()

STATIC_QUERIES = [
    "ПОД/ФТ", "Отмывание денег", "финансирование терроризма", "қаржылық барлау", "қаржы барлау",
    "теневая экономика", "көлеңкелі экономика", "қаражатты жылыстату", "кірістерді жылыстату",
    "қаражатты заңдастыру", "легализация денег", "терроризм қаржыландыру", "экономические преступления",
    "экономика қылмыс", "хищение бюджета", "хищение средств", "қаражатты жымқыру", "бюджет жымқыру",
    "СФМ", "субъект финансового мониторинга", "финоперация", "субъект финмониторинга", "қаржы операция",
    "қаражат қолма-қол ақша аудару", "легализация доходов", "интернет мошенничество", "интернет алаяқ",
    "ПФР", "Финразведка", "Рекомендации ФАТФ FATF", "Национальная оценка рисков",
    "Евразийская группа по противодействию легализации преступных доходов и финансированию терроризма, ЕАГ",

    "Алиев Данияр ДЭР Астана", "Букешев Дастан ДЭР Акмола", "Акжанов Данияр ДЭР Алматы",
    "Абдыханов Куат ДЭР Актобе", "Исмагулов Нуржан ДЭР Алматинская область", "Турганбаев Данияр ДЭР Атырау",
    "Касимов Руслан ДЭР Жамбыл", "Толеубай Нураби ДЭР Караганда", "Абенов Аскар ДЭР Костанай",
    "Кенес Аслан ДЭР Кызылорда", "Булегенов Ержан ДЭР Мангыстау", "Кайшыбеков Бакыт ДЭР Шымкент",
    "Есенов Ерик ДЭР Павлодар", "Абулхаиров Бакытжан ДЭР СКО", "Молдабеков Айдын ДЭР ВКО",
    "Балгабаев Ерболат ДЭР Туркестан", "Каримов Нурлан ДЭР Абайской области", "Тюмелиев Еркин ДЭР Жетісу",
    "Битанов Ерлан ДЭР Улытау", "Дюсенов Асылбек ДЭР ЗКО", "игорный бизнес", "ойын бизнес",
    "контрабанда", "экономическая контрабанда", "подпольный бизнес", "жасырын бизнес",
    "фин пирамида", "қаржы пирамида", "фальшивомонетчик", "фальшивомонетничество",
    "жалған ақша жасау", "незаконный бизнес", "заңсыз бизнес", "фиктивные счет фактуры",
    "жалған шот фактура", "незаконное обналичивание средств", "обнальщик", "обнал",
    "подпольное казино", "жасырын казино", "подпольный цех", "жасырын цех", "Теневик",
    "қылмыстық топ", "ОПГ", "организованные преступные группировки (группы)", "наркотрафик",
    "есірткі тасымалы", "незаконные перевозки товаров", "заңсыз тасымал", "Хищение средств",
    "хищение денег", "қаражат ұрлау", "жымқыру", "Хищение бюджетных средств",
    "Бюджет қаражатын ұрлау", "Бюджет қаражатын жымқыру", "Ущемление прав детей сирот",
    "Жетім балалар құқын бұзу", "Завышенная проектная стоимость", "Жоба құнын арттыру",
    "Продажа бухгалтерских документов", "Бухгалтерлік құжат сату", "Финансовые пирамиды",
    "Қаржы пирамида", "Закуп по завышенной стоимости", "бағасы жоғары",
    "Приобретение по завышенной стоимости", "көтерме бағамен сату", "казнокрадство",
    "мемлекеттік ақшаны жымқыру", "Жанат Элиманов", "Улан Раисов", "Женис Елемесов",
    "Кайрат Бижанов", "Амир Сагындыков", "Финансовый мониторинг", "Финмониторинг",
    "АФМ", "ҚМА", "ДЭР", "Департамент экономических расследований",
    "Экономикалық тергеп-тексеру департаменті"
]


class Query(BaseModel):
    raw_text: str


def analyze_sentiments(texts, sentiment_threshold=0.7):
    """Анализ сентимента списка текстов (батчинг)."""
    results = sentiment_analyzer(texts)
    return [
        {
            "is_negative": res['label'] == 'NEGATIVE' and res['score'] > sentiment_threshold,
            "sentiment_score": res['score']
        }
        for res in results
    ]


@app.post("/predict/")
async def predict(query: Query):
    try:
        queries_with_prefix = [f"query: {q}" for q in STATIC_QUERIES]
        query_tokens = tokenizer(queries_with_prefix, padding=True, truncation=True, return_tensors='pt', max_length=512)

        # Обрабатываем raw_text целиком, без разделения на предложения
        documents = [query.raw_text]  # Теперь весь текст - один "документ"

        if not documents:
            return {"document_scores": []}

        document_tokens = tokenizer(documents, padding=True, truncation=True, return_tensors='pt', max_length=512)

        with torch.no_grad():
            query_embeddings = model(**query_tokens)[0][:, 0]
            document_embeddings = model(**document_tokens)[0][:, 0]

        query_embeddings = torch.nn.functional.normalize(query_embeddings, p=2, dim=1)
        document_embeddings = torch.nn.functional.normalize(document_embeddings, p=2, dim=1)

        scores = torch.mm(query_embeddings, document_embeddings.transpose(0, 1))

        similarity_threshold = 0.25
        result = []

        for query_text, query_scores in zip(STATIC_QUERIES, scores):
            doc_score_pairs = [(doc, score.item()) for doc, score in zip(documents, query_scores) if score.item() >= similarity_threshold]

            if not doc_score_pairs:
                continue

            docs_for_sentiment = [doc for doc, _ in doc_score_pairs]
            sentiment_results = analyze_sentiments(docs_for_sentiment)

            sorted_results = sorted([
                {
                    "document": doc,
                    "similarity_score": score,
                    "is_negative": sentiment["is_negative"],
                    "sentiment_score": sentiment["sentiment_score"]
                }
                for (doc, score), sentiment in zip(doc_score_pairs, sentiment_results)
            ], key=lambda x: x["similarity_score"], reverse=True)

            result.append({
                "query": query_text,
                "document_scores": sorted_results
            })

        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/analyze_sentiment/")
async def sentiment_endpoint(text: str):
    try:
        sentiment_result = analyze_sentiments([text])[0]
        return {
            "text": text,
            "is_negative": sentiment_result["is_negative"],
            "sentiment_score": sentiment_result["sentiment_score"]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Для запуска приложения
# uvicorn afm:app --reload
