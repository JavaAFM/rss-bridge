package org.example.parser.service.impl;

import lombok.AllArgsConstructor;
import org.example.parser.constants.WebSiteConstants;
import org.example.parser.exception.NotFoundException;
import org.example.parser.mapper.TagMapper;
import org.example.parser.model.News;
import org.example.parser.model.Source;
import org.example.parser.repository.SourceRepository;
import org.example.parser.service.OrdaService;
import org.example.parser.service.UpdateDBService;
import org.example.parser.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;

    private final TagMapper tagMapper;
    private final SourceRepository sourceRepository;
    private final UpdateDBService updateDBService;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdaService.class);

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
        Source orda = sourceRepository.getSourceByName("Orda").orElseThrow(()-> new NotFoundException("Source not found."));
        int totalArticles = elements.size();
        int count = 0;
        for (Element element : elements) {
            try {
                String title = element.select("span").text();
                String url = element.select("a").attr("href");

                String mainText = fetchMainText(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                List<String> tags = fetchTags(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                LocalDateTime publicationDate = fetchDate(WebSiteConstants.ORDA_MAIN.getLabel() + url);

                News news = new News();
                news.setTitle(title);
                news.setUrl(WebSiteConstants.ORDA_MAIN.getLabel() + url);
                news.setPublicationDate(publicationDate);
                news.setMainText(mainText);
                news.setSource(orda);
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
        Elements allNewsElements = new Elements();
        boolean keepLoading = true;
        int daysFetched = 0;

        try {
            driver.get(WebSiteConstants.ORDA_NEWS.getLabel());
            acceptCookie();

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            while (keepLoading) {
                if (daysFetched >= 1) {
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
                mainText.append(element.text()).append(System.lineSeparator());
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
    public Document connectToWebPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Chrome")
                .header("Accept-Language", "en-US,en;q=0.9")
                .get();
    }
}