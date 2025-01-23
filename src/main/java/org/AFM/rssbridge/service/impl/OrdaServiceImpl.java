package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.OrdaService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class OrdaServiceImpl implements OrdaService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;


    @Override
    public List<News> toNews(Elements elements) {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + " ELEMENTS");
        for (Element element : elements) {
            try {
                String title = element.select("span").text();
                String url = element.select("a").attr("href");

                String mainText = fetchMainText(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                String imageUrl = fetchImage(WebSiteConstants.ORDA_MAIN.getLabel() + url);

                News news = new News();
                news.setTitle(title);
                news.setUrl(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                news.setImage_url(WebSiteConstants.ORDA_MAIN.getLabel() + imageUrl);
                news.setPublicationDate(publicationDate);
                news.setMainText(mainText);
                news.setComments(null);
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
        Elements allNewsElements = new Elements();
        boolean keepLoading = true;
        int daysFetched = 0;

        try {
            driver.get(WebSiteConstants.ORDA_NEWS.getLabel());
            acceptCookie();

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            while (keepLoading) {
                if (daysFetched >= 40) {
                    keepLoading = false;
                }

                String formattedDate = currentDate.minusDays(daysFetched).format(dateFormatter);

                String url = WebSiteConstants.ORDA_MAIN.getLabel() + formattedDate + "/";
                driver.get(url);

                wait.until(driver -> driver.findElement(By.cssSelector(".newslist3 li")));

                Document doc = Jsoup.parse(Objects.requireNonNull(driver.getPageSource()));
                Elements newsElements = doc.select(".newslist3 li");
                allNewsElements.addAll(newsElements);
                System.out.println("Elements added for: " + formattedDate);

                daysFetched++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Elements();
        } finally {
            driver.quit();
        }
        return allNewsElements;
    }

    @Override
    public void acceptCookie() {
        WebElement acceptCookieButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("accept-cookie")));
        acceptCookieButton.click();
    }

    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);

            Elements mainTextElements = articleDoc.select("p, blockquote");
            StringBuilder mainText = new StringBuilder();
            for (Element element : mainTextElements) {
                mainText.append(element.text());
            }

            return mainText.toString();
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

            Elements tagElements = articleDoc.select(".tags a");

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

            Element timeElement = doc.selectFirst("time");
            String dateTimeString = "";

            if (timeElement.hasAttr("datetime")) {
                dateTimeString = timeElement.attr("datetime");
                return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
            } else {
                Element linkElement = timeElement.selectFirst("a");
                dateTimeString = Objects.requireNonNullElse(linkElement, timeElement).text();
            }
            return dateUtil.parseOrdaTime(dateTimeString);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch or parse date from the article URL", e);
        }
    }


    @Override
    public String fetchImage(String url) {
        try {
            Document doc = connectToWebPage(url);
            Element imageElement = doc.select("div.postpic img").first();

            if (imageElement != null) {
                return imageElement.attr("src");
            } else {
                return "";
            }
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