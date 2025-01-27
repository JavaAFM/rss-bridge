package org.AFM.rssbridge.service.scrapper;

import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public interface WebSiteScrapper {
    List<News> toNews(Elements elements) throws NotFoundException;
    Elements allNewsElements();
    Document connectToWebPage(String url) throws IOException;
}
