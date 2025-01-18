package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.AzattyqService;
import org.AFM.rssbridge.uitl.DateTimeFormatterUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AzattyqServiceImpl implements AzattyqService {
    private final DateTimeFormatterUtil dateUtil;

    @Override
    public List<News> toNews(Elements elements) {
        List<News> newsList = new ArrayList<>();
        System.out.println("THERE ARE " + elements.size() + " ELEMENTS");
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
        try {
            Document doc = connectToWebPage(WebSiteConstants.AZATTYQ_NEWS.getLabel());
            return doc.select(".row ul > li");
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }
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
                .get();
    }
}
