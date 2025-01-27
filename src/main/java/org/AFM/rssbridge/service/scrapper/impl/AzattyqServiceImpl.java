package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.SourceService;
import org.AFM.rssbridge.service.scrapper.AzattyqService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AzattyqServiceImpl implements AzattyqService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;

    private final SourceService sourceService;

    @Override
    public List<News> toNews(Elements elements) throws NotFoundException {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + " ELEMENTS");
        Source azattyq = sourceService.getSourceByName("Azattyq");
        for (Element element : elements) {
            try {
                String title = element.select(".media-block__title").text();
                String url = element.select("a[href]").attr("href");
                String imageUrl = element.select(".thumb img").attr("src");
                if (imageUrl.isEmpty()) {
                    imageUrl = "";
                }
                String mainText = fetchMainText(WebSiteConstants.AZATTYQ_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.AZATTYQ_MAIN.getLabel()+ url);

                News news = new News();
                news.setTitle(title);
                news.setUrl(WebSiteConstants.AZATTYQ_MAIN.getLabel() + url);
                news.setImage_url(imageUrl);
                news.setMainText(mainText);
                news.setSummary("");
                news.setSource(azattyq);
                news.setPublicationDate(publicationDate);

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
            driver.get(WebSiteConstants.AZATTYQ_NEWS.getLabel());

            while (keepLoading) {
                WebElement newsContainer = wait.until(driver -> driver.findElement(By.id("ordinaryItems")));

                List<WebElement> newsItems = newsContainer.findElements(By.cssSelector("li"));

                for (WebElement newsItem : newsItems) {
                    WebElement dateElement = newsItem.findElement(By.cssSelector(".date"));
                    String dateText = dateElement.getText();

                    LocalDateTime articleDate = dateUtil.parseAzattyqTime(dateText);

                    if (articleDate.isBefore(LocalDateTime.now().minusMonths(3))) {
                        keepLoading = false;
                        break;
                    }

                    String outerHtml = newsItem.getAttribute("outerHTML");
                    allNews.add(Jsoup.parse(outerHtml).body().child(0));
                }

                List<WebElement> loadMoreButtons = driver.findElements(By.cssSelector(".btn--load-more"));
                if (!loadMoreButtons.isEmpty() && keepLoading) {
                    loadMoreButtons.get(0).click();
                    System.out.println("Load More button clicked.");


                    int previousCount = newsItems.size();
                    wait.until(driver -> newsContainer.findElements(By.cssSelector("li")).size() > previousCount);
                    System.out.println("New news items loaded.");
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

            Elements mainTextElements = articleDoc.select("div#article-content p");
            StringBuilder mainText = new StringBuilder();
            for(Element element : mainTextElements){
                mainText.append(element.text());
            }

            return mainText.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    @Override
    public LocalDateTime fetchDate(String articleUrl) {
        try {
            Document doc = connectToWebPage(articleUrl);

            Element dateTimeElement = doc.select("time[pubdate]").first();
            String dateTimeString = "";
            if (dateTimeElement != null) {
                dateTimeString = dateTimeElement.text();
            }
            return dateUtil.parseAzattyqTime(dateTimeString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public Document connectToWebPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(60000)
                .get();
    }
}
