package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.SourceService;
import org.AFM.rssbridge.service.scrapper.KazTagService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class KazTagServiceImpl implements KazTagService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;

    private final SourceService sourceService;

    @Override
    public List<News> toNews(Elements elements) throws NotFoundException {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + " ELEMENTS");
        Source kaztag = sourceService.getSourceByName("Kaztag");
        for (Element element : elements) {
            try {
                String title = element.select(".post-title").text();

                String url = element.select(".post-title a").attr("href");
                String imageUrl = element.select(".featured-image img").attr("src");
                if (imageUrl.isEmpty()) {
                    imageUrl = "";
                }
                String mainText = fetchMainText(WebSiteConstants.KAZTAG_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.KAZTAG_MAIN.getLabel() + url);

                LocalDateTime publicationDate = fetchDate(WebSiteConstants.KAZTAG_MAIN.getLabel() + url);
                News news = new News();
                news.setTitle(title);
                news.setUrl(WebSiteConstants.KAZTAG_MAIN.getLabel() + url);
                news.setImage_url(WebSiteConstants.KAZTAG_MAIN.getLabel() + imageUrl);
                news.setSummary("");
                news.setSource(kaztag);
                news.setPublicationDate(publicationDate);
                news.setMainText(mainText);
                news.setComments(new ArrayList<>());
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
            driver.get(WebSiteConstants.KAZTAG_NEWS.getLabel());
            while (keepLoading) {
                try {
                    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                    String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
                    File destination = new File(fileName);
                    FileUtils.copyFile(screenshot, destination);
                    System.out.println("Screenshot saved: " + destination.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to capture screenshot.");
                }
                handleCaptcha(driver.getCurrentUrl());

                WebElement newsContainer = wait.until(driver -> driver.findElement(By.cssSelector(".layout-container")));

                List<WebElement> newsItems = newsContainer.findElements(By.cssSelector(".post"));

                for (WebElement newsItem : newsItems) {
                    WebElement dateElement = newsItem.findElement(By.cssSelector(".post-byline"));
                    String dateText = dateElement.getText();
                    LocalDateTime articleDate = dateUtil.parseKazTagTime(dateText);

                    if (articleDate.isBefore(LocalDateTime.now().minusMonths(3))) {
                        keepLoading = false;
                        break;
                    }

                    String outerHtml = newsItem.getAttribute("outerHTML");
                    allNews.add(Jsoup.parse(outerHtml).body().child(0));
                }
                System.out.println("New news items loaded.");

                List<WebElement> nextPageButtons = driver.findElements(By.cssSelector(".modern-page-next"));
                if (!nextPageButtons.isEmpty() && keepLoading) {
                    nextPageButtons.get(0).click();
                    System.out.println("Next page button clicked.");
                } else {
                    keepLoading = false;
                    System.out.println("No more pages to load or 5-day condition reached.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Elements();
        }
        return allNews;
    }


    private void handleCaptcha(String url) {
        // I can't do it(
    }



    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);
            Element mainTextElement = articleDoc.select("div.post-content").first();
            return mainTextElement != null ? mainTextElement.text() : "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public List<String> fetchTags(String articleUrl) {
        List<String> tagList = new ArrayList<>();
        try {
            Document articleDoc = connectToWebPage(articleUrl);

            Elements tagElements = articleDoc.select(".post-tags ul li a");

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

            Element dateTimeElement = doc.select(".post-byline").first();
            return dateTimeElement != null ? dateUtil.parseKazTagTime(dateTimeElement.text()) : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document connectToWebPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get();
    }
}
