package org.AFM.rssbridge.service.scrapper;

import org.AFM.rssbridge.model.Comment;
import org.AFM.rssbridge.model.News;

import java.time.LocalDateTime;
import java.util.List;

public interface ZakonService extends WebSiteScrapper {
    String fetchMainText(String url);
    List<Comment> fetchComments(String url);
    List<String> fetchTags(String url);
    LocalDateTime fetchDate(String url);
    String fetchSummary(String url);
}
