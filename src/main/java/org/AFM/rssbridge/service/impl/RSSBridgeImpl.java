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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                LocalDateTime publicationDate = fetchDate("https://tengrinews.kz" + url);

                News news = new News();
                news.setTitle(title);
                news.setUrl(url);
                news.setImage_url(imageUrl);
                news.setSummary(summary);
                news.setPublicationDate(publicationDate);
                news.setMainText(mainText);
                news.setComments(comments);
                news.setTags(tags);

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


    public List<Comment> fetchComments(String url) {
        Set<Comment> uniqueComments = new HashSet<>(); // Use Set for uniqueness
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            int maxAttempts = 5;
            int attempts = 0;

            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class,'tn-comment-item')]")));

            while (attempts < maxAttempts) {
                try {
                    WebElement moreButton = driver.findElement(By.className("more-comments"));
                    js.executeScript("arguments[0].click();", moreButton);
                    attempts++;
                    Thread.sleep(1500);
                } catch (Exception e) {
                    break;
                }
            }

            List<WebElement> commentElements = driver.findElements(
                By.xpath("//div[@class='tn-comment-item']"));

            for (WebElement commentElement : commentElements) {
                String author = commentElement.findElement(
                    By.xpath(".//a[@class='tn-user-name']")).getText().trim();
                String content = commentElement.findElement(
                    By.xpath(".//div[contains(@class,'tn-comment-item-content-text')]")).getText().trim();
                String time = commentElement.findElement(
                    By.xpath(".//time")).getText().trim();

                // Only add non-empty comments
                if (!author.isEmpty() && !content.isEmpty()) {
                    Comment comment = new Comment();
                    comment.setAuthor(author);
                    comment.setContent(content);
                    comment.setTime(time);
                    uniqueComments.add(comment);
                }
            }

            return new ArrayList<>(uniqueComments);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            driver.quit();
        }
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

    @Override
    public LocalDateTime fetchDate(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl).get();

            Element dateTimeElement = doc.select("div.date-time").first();
            String dateTimeString = "";
            if (dateTimeElement != null) {
                 dateTimeString = dateTimeElement.text();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy | HH:mm");

            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);

            return localDateTime;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}