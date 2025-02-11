package org.example.parser.service.impl;

import lombok.AllArgsConstructor;
import org.example.parser.constants.WebSiteConstants;
import org.example.parser.exception.NotFoundException;
import org.example.parser.mapper.TagMapper;
import org.example.parser.model.Comment;
import org.example.parser.model.News;
import org.example.parser.model.Source;
import org.example.parser.repository.SourceRepository;
import org.example.parser.service.ZakonService;
import org.example.parser.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ZakonServiceImpl implements ZakonService {
    private WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;

    private final TagMapper tagMapper;
    private final SourceRepository sourceRepository;
    private final UpdateDBImpl updateDBService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ZakonService.class);


    @Override
    public void parse() throws NotFoundException {
        Object proxySetting = ((ChromeDriver) driver).getCapabilities().getCapability("proxy");
        LOGGER.info("Parsing with proxy: {}", proxySetting);

        Elements allElements = allNewsElements();
        toNews(allElements);
    }

    @Override
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void toNews(Elements elements) throws NotFoundException {
        LOGGER.warn("THERE ARE " + elements.size() + " ELEMENTS");
        Source zakon = sourceRepository.getSourceByName("Zakon").orElseThrow(()-> new NotFoundException("Source not found."));
        int totalArticles = elements.size();
        int count = 0;
        for (Element element : elements) {
            try {
                String title = element.select(".newscard__title").text();

                String url = element.select(".newscard_link").attr("href");


                String mainText = fetchMainText(WebSiteConstants.ZAKON_MAIN.getLabel() + url);
                List<Comment> comments = fetchComments(WebSiteConstants.ZAKON_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.ZAKON_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.ZAKON_MAIN.getLabel() + url);
                String summary = fetchSummary(WebSiteConstants.ZAKON_MAIN.getLabel() + url);

                News news = new News();
                news.setTitle(title+"\n");
                news.setSummary(summary+"\n");
                news.setSource(zakon);
                news.setUrl(WebSiteConstants.ZAKON_MAIN.getLabel() + url);
                news.setPublicationDate(publicationDate);
                news.setMainText(mainText+"\n");
                news.setComments(comments);
                news.setTags(tagMapper.toListOfTags(tags, news));

                count++;

                updateDBService.insertNews(news);
                LOGGER.warn(count+"/"+totalArticles+" article processed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LOGGER.warn("Finished processing all articles.");
    }


    @Override
    public String fetchSummary(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);

            Element summaryElement = articleDoc.select(".description").first();
            if (summaryElement != null) {
                return summaryElement.text();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public Elements allNewsElements() {
        Elements allNews = new Elements();
        boolean keepLoading = true;

        try {
            driver.get(WebSiteConstants.ZAKON_NEWS.getLabel());

            while (keepLoading) {
                List<WebElement> newsItems = wait.until(driver -> driver.findElements(By.cssSelector(".news-item"))); // Update selector

                for (WebElement newsItem : newsItems) {
                    WebElement dateElement = newsItem.findElement(By.cssSelector(".newscard__date"));
                    String dateText = dateElement.getText();
                    LocalDateTime articleDate = dateUtil.parseZakonDate(dateText);

                    if (articleDate.isBefore(LocalDateTime.now().minusMinutes(3))) {
                        keepLoading = false;
                        break;
                    }

                    String outerHtml = newsItem.getAttribute("outerHTML");
                    allNews.add(Jsoup.parse(outerHtml).body().child(0));
                }

                if (keepLoading) {
                    ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    System.out.println("Scrolled to the bottom of the page.");

                    Thread.sleep(2000);

                    List<WebElement> newNewsItems = driver.findElements(By.cssSelector(".news-item"));
                    if (newNewsItems.size() == newsItems.size()) {
                        keepLoading = false;
                        System.out.println("No new content loaded. Stopping.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred during parsing.");
        }

        return allNews;
    }

    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);

            Element mainTextElement = articleDoc.select(".content").first();
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
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@class='zknc zknc-item']")));

            List<WebElement> commentElements = driver.findElements(By.xpath("//div[@class='zknc zknc-item']"));

            for (WebElement commentElement : commentElements) {
                String author = commentElement.findElement(By.xpath(".//a[@class='zknc zknc-author-name']")).getText().trim();
                String content = commentElement.findElement(By.xpath(".//div[@class='zknc zknc-message']")).getText().trim();
                String time = commentElement.findElement(By.xpath(".//span[contains(@class, 'zknc zknc-date')]")).getText().trim();

                int rating = 0;
                try {
                    WebElement ratingElement = commentElement.findElement(By.xpath(".//span[contains(@class, 'zknc zknc-total-value')]"));
                    String ratingText = ratingElement.getText().trim();
                    if (!ratingText.isEmpty() && ratingText.matches("-?\\d+")) {
                        rating = Integer.parseInt(ratingText);
                    }
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
        }
    }


    @Override
    public List<String> fetchTags(String articleUrl) {
        List<String> tagList = new ArrayList<>();
        try {
            Document articleDoc = connectToWebPage(articleUrl);

            Elements tagElements = articleDoc.select(".tags.newsTags .badge.large, .article__category .badge.large");
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

            Element dateTimeElement = doc.select(".date").first();
            String dateTimeString = "";
            if (dateTimeElement != null) {
                dateTimeString = dateTimeElement.text();
            }
            return dateUtil.parseZakonDate(dateTimeString);
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