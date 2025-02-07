package org.example.parser.service;

import org.example.parser.exception.NotFoundException;
import org.example.parser.model.News;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public interface WebSiteScrapper {
    void toNews(Elements elements) throws NotFoundException;
    Elements allNewsElements();
    Document connectToWebPage(String url) throws IOException;
    void parse() throws NotFoundException;
}
