package org.AFM.rssbridge.service;

import org.AFM.rssbridge.model.News;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public interface WebSiteScrapper {
    List<News> toNews(Elements elements);
    Elements allNewsElements();
    Document connectToWebPage(String url) throws IOException;
}
