package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.SourceService;
import org.AFM.rssbridge.service.scrapper.TengriService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TengriServiceImpl implements TengriService {
    private final DateTimeFormatterUtil dateUtil;
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final SourceService sourceService;

    @Override
    public List<News> toNews(Elements elements) throws NotFoundException {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + "ELEMENTS");
        Source tengri = sourceService.getSourceByName("Tengri");
        for (Element element : elements) {
            try {
                String title = element.select(".content_main_item_title a").text();

                String url = element.select(".content_main_item_title a").attr("href");

                String summary = element.select(".content_main_item_announce").text();

                String mainText = fetchMainText(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                List<Comment> comments = fetchComments(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.TENGRI_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.TENGRI_MAIN.getLabel() + url);

                News news = new News();
                news.setTitle(title);
                news.setUrl(url);
                news.setSummary(summary);
                news.setSource(tengri);
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
        Elements allNews = new Elements();
        boolean keepLoading = true;

        try {
            driver.get(WebSiteConstants.TENGRI_NEWS.getLabel());

            while (keepLoading) {
                List<WebElement> newsItems = wait.until(driver -> driver.findElements(By.cssSelector(".content_main_item")));

                for (WebElement newsItem : newsItems) {
                    WebElement dateElement = newsItem.findElement(By.cssSelector(".content_main_item_meta > span:first-child"));
                    String dateText = dateElement.getText();
                    LocalDateTime articleDate = dateUtil.parseTengriTime(dateText);

                    if (articleDate.isBefore(LocalDateTime.now().minusDays(1))) {
                        keepLoading = false;
                        break;
                    }

                    String outerHtml = newsItem.getAttribute("outerHTML");
                    allNews.add(Jsoup.parse(outerHtml).body().child(0));
                }

                    List<WebElement> nextPageButtons = driver.findElements(By.cssSelector(".page-item .page-link"));
                if (!nextPageButtons.isEmpty() && keepLoading) {
                    nextPageButtons.get(nextPageButtons.size()-1).click();
                    System.out.println("Next page button clicked.");
                } else {
                    keepLoading = false;
                    System.out.println("No more news to load or 5-day condition reached.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Elements();
        }finally {
            driver.quit();
        }

        return allNews;
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
        List<Comment> uniqueComments = new ArrayList<>();

        try {
            driver.get(url);
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
                    By.xpath("//div[@class='tn-com ament-item']"));

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

            return uniqueComments;
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