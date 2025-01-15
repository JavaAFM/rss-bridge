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
            String baseUrl = websiteConfig.getUrl();
            Elements allElements = new Elements();
            
            // Get the first page
            Document doc = Jsoup.connect(baseUrl).get();
            allElements.addAll(doc.select(".content_main_item"));
            
            // Get additional pages (let's get first 5 pages for example)
            for (int page = 2; page <= 5; page++) {
                String pageUrl = baseUrl + "page/" + page + "/";
                try {
                    Document pageDoc = Jsoup.connect(pageUrl).get();
                    Elements pageElements = pageDoc.select(".content_main_item");
                    if (pageElements.isEmpty()) {
                        break; // No more news items found
                    }
                    allElements.addAll(pageElements);
                } catch (IOException e) {
                    break; // Stop if we can't load more pages
                }
            }
            
            return allElements;
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
        List<Comment> commentList = new ArrayList<>();

        // Setup WebDriver (ensure you have the appropriate WebDriver for your browser, e.g., ChromeDriver)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Optional: run in headless mode
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);

            // Wait for the comments section to be fully loaded (adjust the wait condition based on your page structure)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'tn-comment-item')]")));

            List<WebElement> commentElements = driver.findElements(By.xpath("//div[@class='tn-comment-item']"));

            for (WebElement commentElement : commentElements) {
                WebElement authorElement = commentElement.findElement(By.xpath(".//a[@class='tn-user-name']"));
                WebElement contentElement = commentElement.findElement(By.xpath(".//div[contains(@class,'tn-comment-item-content-text')]"));
                WebElement timeElement = commentElement.findElement(By.xpath(".//time"));

                String author = (authorElement != null) ? authorElement.getText() : "";
                String content = (contentElement != null) ? contentElement.getText() : "";
                String time = (timeElement != null) ? timeElement.getText() : "";

                Comment comment = new Comment();
                comment.setAuthor(author);
                comment.setContent(content);
                comment.setTime(time);

                // Add the comment to the list
                commentList.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the driver after use
            driver.quit();
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