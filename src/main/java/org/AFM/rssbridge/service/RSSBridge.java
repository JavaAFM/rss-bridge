package org.AFM.rssbridge.service;

import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface RSSBridge {

    List<News> toNews(Elements elements);
    Elements allNewsElements();
    String fetchMainText(String articleUrl);
    List<Comment> fetchComments(String articleUrl);
    List<String> fetchTags(String articleUrl);
    LocalDateTime fetchDate(String articleUrl);
}
