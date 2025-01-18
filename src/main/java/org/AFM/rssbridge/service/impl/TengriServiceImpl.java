package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.TengriService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TengriServiceImpl implements TengriService {
    private final DateTimeFormatterUtil dateUtil;


    @Override
    public List<News> toNews(Elements elements) {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + "ELEMENTS");
        for (Element element : elements) {
            try {
                String title = element.select(".content_main_item_title a").text();

                String url = element.select(".content_main_item_title a").attr("href");

                String imageUrl = element.select("img").attr("src");
                if (imageUrl.isEmpty()) {
                    imageUrl = "";
                }

                String summary = element.select(".content_main_item_announce").text();

                String mainText = fetchMainText(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                List<Comment> comments = fetchComments(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.TENGRI_MAIN.getLabel() + url);

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

            Document doc = connectToWebPage(WebSiteConstants.TENGRI_NEWS.getLabel());

            return doc.select(".content_main_item");
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }
    }

    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);

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
        Set<Comment> uniqueComments = new HashSet<>();
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

                int rating;
                try {
                    WebElement ratingElement = commentElement.findElement(
                            By.xpath(".//span[contains(@class, 'tn-comment-rating-button')]/span"));
                    String ratingText = ratingElement.getText().trim();
                    rating = Integer.parseInt(ratingText);
                } catch (Exception e) {
                    rating = 0;
                }
                if (!author.isEmpty() && !content.isEmpty()) {
                    Comment comment = new Comment();
                    comment.setAuthor(author);
                    comment.setContent(content);
                    comment.setTime(time);
                    comment.setLikes(rating);
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
            Document articleDoc = connectToWebPage(articleUrl);

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
            Document doc = connectToWebPage(articleUrl);

            Element dateTimeElement = doc.select("div.date-time").first();
            String dateTimeString = "";
            if (dateTimeElement != null) {
                dateTimeString = dateTimeElement.text();
            }
            return dateUtil.parseTengriTime(dateTimeString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document connectToWebPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Chrome")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get();
    }
}