package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.WebsiteConfig;
import org.AFM.rssbridge.service.RSSBridge;
import org.AFM.rssbridge.uitl.JsonReader;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class RSSBridgeImpl implements RSSBridge {
    private final JsonReader jsonReader;


    @Override
    public List<News> toNews(Elements elements) {
        List<News> newsList = new ArrayList<>();

        for (Element element : elements) {
            try {
                String title = element.select(".content_main_item_title a").text();

                String url = element.select(".content_main_item_title a").attr("href");

                String imageUrl = element.select("img").attr("src");
                if (imageUrl.isEmpty()) {
                    imageUrl = "";
                }

                String summary = element.select(".content_main_item_announce").text();

                String mainText = fetchMainText("https://tengrinews.kz" + url);
                List<Comment> comments = fetchComments("https://tengrinews.kz" + url);
                List<String> tags = fetchTags("https://tengrinews.kz" + url);
                int views = fetchViews("https://tengrinews.kz/news/");
                LocalDate publicationDate = LocalDate.now();

                News news = new News();
                news.setTitle(title);
                news.setUrl(url);
                news.setImage_url(imageUrl);
                news.setSummary(summary);
                news.setPublicationDate(publicationDate);
                news.setViewings(0);
                news.setMainText(mainText);
                news.setComments(comments);
                news.setTags(tags);
                news.setViewings(views);

                newsList.add(news);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(newsList);
        return newsList;
    }

    @Override
    public Elements allNewsElements() {
        try {
            WebsiteConfig websiteConfig = jsonReader.readWebsiteConfig("src/main/resources/website.json");

            Document doc = Jsoup.connect(websiteConfig.getUrl()).get();

            return doc.select(".content_main_item");
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }
    }

    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = Jsoup.connect(articleUrl).get();

            Element mainTextElement = articleDoc.select(".content_main_text").first();
            if (mainTextElement != null) {
                return mainTextElement.text();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public int fetchViews(String articleUrl) {
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            HtmlPage page = webClient.getPage(articleUrl);

            webClient.waitForBackgroundJavaScript(10000);

            HtmlElement viewElement = page.getFirstByXPath("//span[@class='tn-text-preloader-dark']");

            String viewCount = viewElement != null ? viewElement.getTextContent() : "1";

            return Integer.parseInt(viewCount);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<Comment> fetchComments(String url) {
        List<Comment> commentList = new ArrayList<>();
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            HtmlPage page = webClient.getPage(url);

            webClient.waitForBackgroundJavaScript(10000);

            List<HtmlDivision> commentElements = page.getByXPath("//div[contains(@class,'tn-comment-item')]");

            for (HtmlDivision commentElement : commentElements) {
                HtmlElement authorElement = commentElement.getFirstByXPath(".//a[@class='tn-user-name']");
                HtmlElement contentElement = commentElement.getFirstByXPath(".//div[contains(@class,'tn-comment-item-content-text')]");
                HtmlElement timeElement = commentElement.getFirstByXPath(".//time");

                String author = (authorElement != null) ? authorElement.getTextContent() : "";
                String content = (contentElement != null) ? contentElement.getTextContent() : "";
                String time = (timeElement != null) ? timeElement.getTextContent() : "";

                Comment comment = new Comment();
                comment.setAuthor(author);
                comment.setContent(content);
                comment.setTime(time);

                commentList.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentList;
    }

    @Override
    public List<String> fetchTags(String articleUrl) {
        List<String> tagList = new ArrayList<>();
        try {
            Document articleDoc = Jsoup.connect(articleUrl).get();

            Elements tagElements = articleDoc.select("#comm.content_main_text_tags a");

            for (Element tagElement : tagElements) {
                tagList.add(tagElement.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tagList;
    }

}