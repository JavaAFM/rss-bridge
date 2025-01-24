package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.KazTagService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;

import org.openqa.selenium.devtools.v131.emulation.Emulation;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class KazTagServiceImpl implements KazTagService {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final DateTimeFormatterUtil dateUtil;

    @Override
    public List<News> toNews(Elements elements) {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + " ELEMENTS");
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
                handleCaptcha();

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


    private void handleCaptcha() {
        try {
            String pythonCode = """
            import sys
            import requests

            def solve_captcha(api_key, site_key, page_url):
                # Send request to 2Captcha
                payload = {
                    'key': api_key,
                    'method': 'userrecaptcha',
                    'googlekey': site_key,
                    'pageurl': page_url,
                    'json': 1
                }
                response = requests.post('http://2captcha.com/in.php', data=payload)
                request_result = response.json()

                if request_result.get('status') != 1:
                    print("Error: Unable to request CAPTCHA solution.")
                    return None

                # Poll for the solution
                captcha_id = request_result.get('request')
                fetch_url = f"http://2captcha.com/res.php?key={api_key}&action=get&id={captcha_id}&json=1"
                for _ in range(30):  # Wait up to 30 seconds
                    result = requests.get(fetch_url).json()
                    if result.get('status') == 1:
                        return result.get('request')
                    time.sleep(5)  # Wait for 5 seconds

                print("Error: CAPTCHA solution timeout.")
                return None

            # Retrieve arguments
            api_key = sys.argv[1]
            site_key = sys.argv[2]
            page_url = sys.argv[3]

            # Solve the CAPTCHA
            solution = solve_captcha(api_key, site_key, page_url)
            if solution:
                print(solution)
            else:
                print("Error: Unable to solve CAPTCHA.")
        """;

            File tempScript = File.createTempFile("captcha_solver", ".py");
            FileWriter writer = new FileWriter(tempScript);
            writer.write(pythonCode);
            writer.close();

            String apiKey = "your_2captcha_api_key";
            String siteKey = driver.findElement(By.cssSelector("iframe[src*='captcha']")).getAttribute("data-sitekey");
            String pageUrl = driver.getCurrentUrl();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3", tempScript.getAbsolutePath(), apiKey, siteKey, pageUrl
            );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String captchaToken = reader.readLine();

            ((JavascriptExecutor) driver).executeScript(
                    "document.getElementById('g-recaptcha-response').innerHTML='" + captchaToken + "';"
            );

            WebElement submitButton = driver.findElement(By.id("captcha-form"));
            submitButton.click();

            System.out.println("CAPTCHA solved and submitted successfully.");

            tempScript.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to solve CAPTCHA.");
        }
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
