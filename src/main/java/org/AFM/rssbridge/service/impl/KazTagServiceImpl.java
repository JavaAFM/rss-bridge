package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.constants.WebSiteConstants;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.KazTagService;
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
public class KazTagServiceImpl implements KazTagService {
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
                String mainText = fetchMainText(WebSiteConstants.KAZTAG_MAIN.getLabel()+ url);
                List<String> tags = fetchTags(WebSiteConstants.KAZTAG_MAIN.getLabel()+url);

                LocalDateTime publicationDate = fetchDate(WebSiteConstants.KAZTAG_MAIN.getLabel()+ url);
                News news = new News();
                news.setTitle(title);
                news.setUrl(WebSiteConstants.KAZTAG_MAIN.getLabel()+url);
                news.setImage_url(WebSiteConstants.KAZTAG_MAIN.getLabel()+imageUrl);
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
        try {
            Document doc = connectToWebPage(WebSiteConstants.KAZTAG_NEWS.getLabel());
            return doc.select(".loop-container .post");
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }
    }

    @Override
    public String fetchMainText(String articleUrl) {
        try {
            Document articleDoc = connectToWebPage(articleUrl);
            Element mainTextElement = articleDoc.select("div.post-content").first();
            String mainText = "";
            if(mainTextElement != null){
                mainText = mainTextElement.text();
            }
            return mainText;
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
            String dateTimeString = "";
            if (dateTimeElement != null) {
                dateTimeString = dateTimeElement.text();
            }
            return dateUtil.parseKazTagTime(dateTimeString);
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
