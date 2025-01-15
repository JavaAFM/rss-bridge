package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.WebsiteConfig;
import org.AFM.rssbridge.service.RSSBridge;
import org.AFM.rssbridge.uitl.JsonReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RSSBridgeImpl implements RSSBridge {
    private final JsonReader jsonReader;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();

    private List<Comment> fetchCommentsViaXHR(String articleId) {
        try {
            // Simulate XHR request to comments endpoint
            String commentsUrl = "https://tengrinews.kz/comments/load/" + articleId + "/";
            Request request = new Request.Builder()
                .url(commentsUrl)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Referer", "https://tengrinews.kz/")
                .header("Accept", "*/*")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return new ArrayList<>();

                Document doc = Jsoup.parse(response.body().string());
                Elements commentElements = doc.select(".comment-item");
                List<Comment> comments = new ArrayList<>();

                for (Element commentEl : commentElements) {
                    Comment comment = new Comment();
                    comment.setId(commentEl.attr("data-id"));
                    comment.setAuthor(commentEl.select(".comment-author").text());
                    comment.setTime(commentEl.select(".comment-time").text());
                    comment.setContent(commentEl.select(".comment-text").text());
                    comment.setLikes(parseCommentLikes(commentEl.select(".likes-count").text()));
                    comments.add(comment);
                }
                return comments;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private int parseCommentLikes(String likesText) {
        try {
            return Integer.parseInt(likesText.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Update the original fetchCommentsViaApi method to use this new implementation
    private List<Comment> fetchCommentsViaApi(String articleId) {
        return fetchCommentsViaXHR(articleId);
    }

    @Override
    public List<News> toNews(Elements elements) {
        List<CompletableFuture<News>> futures = new ArrayList<>();

        for (Element element : elements) {
            CompletableFuture<News> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String title = element.select(".content_main_item_title a").text();
                    String url = "https://tengrinews.kz" + element.select(".content_main_item_title a").attr("href");
                    String imageUrl = element.select("img").attr("src");
                    String summary = element.select(".content_main_item_announce").text();
                    String articleId = extractArticleId(url); // Add this helper method

                    News news = new News();
                    news.setTitle(title);
                    news.setUrl(url);
                    news.setImage_url(imageUrl.isEmpty() ? "" : imageUrl);
                    news.setSummary(summary);

                    // Fetch article details in parallel
                    Document articleDoc = Jsoup.connect(url)
                            .timeout(5000)
                            .get();

                    news.setMainText(extractMainText(articleDoc));
                    news.setTags(extractTags(articleDoc));
                    news.setPublicationDate(extractDate(articleDoc));
                    news.setComments(fetchCommentsViaApi(articleId)); // Fetch comments via API instead of scraping

                    return news;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService);
            futures.add(future);
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(news -> news != null)
                .toList();
    }

    private String extractArticleId(String url) {
        // Extract article ID from URL
        // Example: https://tengrinews.kz/kazakhstan_news/123456-title
        try {
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.matches("\\d+.*")) {
                    return part.split("-")[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Elements allNewsElements() {
        try {
            WebsiteConfig websiteConfig = jsonReader.readWebsiteConfig("src/main/resources/website.json");
            Document doc = Jsoup.connect(websiteConfig.getUrl())
                    .timeout(5000)
                    .get();
            return doc.select(".content_main_item");
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }
    }

    private String extractMainText(Document articleDoc) {
        Element mainTextElement = articleDoc.select(".content_main_text").first();
        return mainTextElement != null ? mainTextElement.text() : "";
    }

    private List<String> extractTags(Document articleDoc) {
        Elements tagElements = articleDoc.select("#comm.content_main_text_tags a");
        return tagElements.stream()
                .map(Element::text)
                .toList();
    }

    private LocalDateTime extractDate(Document articleDoc) {
        try {
            Element dateTimeElement = articleDoc.select("div.date-time").first();
            if (dateTimeElement != null) {
                String dateTimeString = dateTimeElement.text();
                
                // Handle "Вчера" (Yesterday) case
                if (dateTimeString.startsWith("Вчера")) {
                    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                    String timeStr = dateTimeString.split("\\|")[1].trim();
                    String[] timeParts = timeStr.split(":");
                    return yesterday
                        .withHour(Integer.parseInt(timeParts[0]))
                        .withMinute(Integer.parseInt(timeParts[1]))
                        .withSecond(0)
                        .withNano(0);
                }
                
                // Handle regular date format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy | HH:mm");
                return LocalDateTime.parse(dateTimeString, formatter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LocalDateTime.now();
    }

    // These methods are kept for interface compatibility but simplified
    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = Jsoup.connect(articleUrl).timeout(5000).get();
            return extractMainText(articleDoc);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public List<Comment> fetchComments(String articleUrl) {
        return new ArrayList<>(); // Comments disabled as they required JavaScript
    }

    @Override
    public List<String> fetchTags(String articleUrl) {
        try {
            Document articleDoc = Jsoup.connect(articleUrl).timeout(5000).get();
            return extractTags(articleDoc);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public LocalDateTime fetchDate(String articleUrl) {
        try {
            Document articleDoc = Jsoup.connect(articleUrl).timeout(5000).get();
            return extractDate(articleDoc);
        } catch (IOException e) {
            return LocalDateTime.now();
        }
    }
}