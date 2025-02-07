package org.example.parser.service.impl;

import lombok.AllArgsConstructor;
import org.example.parser.constants.WebSiteConstants;
import org.example.parser.exception.NotFoundException;
import org.example.parser.mapper.TagMapper;
import org.example.parser.model.Comment;
import org.example.parser.model.News;
import org.example.parser.model.Source;
import org.example.parser.repository.SourceRepository;
import org.example.parser.service.TengriService;
import org.example.parser.service.UpdateDBService;
import org.example.parser.uitl.DateTimeFormatterUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final TagMapper tagMapper;
    private final SourceRepository sourceRepository;
    private final UpdateDBService updateDBService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TengriServiceImpl.class);
    @Override
    public void parse() throws NotFoundException {
        Elements allElements = allNewsElements();
        toNews(allElements);
    }


    @Override
    public void toNews(Elements elements) throws NotFoundException {
        LOGGER.warn("THERE ARE " + elements.size() + "ELEMENTS");
        Source tengri = sourceRepository.getSourceByName("Tengri").orElseThrow(()-> new NotFoundException("Source not found."));
        int totalArticles = elements.size();
        int count = 0;
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

                    if (articleDate.isBefore(LocalDateTime.now().minusMonths(3))){
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

            Element dateTimeElement = doc.select("ol.date-time li").last();
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