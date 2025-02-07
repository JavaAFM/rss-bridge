package org.example.parser.service;


import org.example.parser.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface ZakonService extends WebSiteScrapper {
    String fetchMainText(String url);
    List<Comment> fetchComments(String url);
    List<String> fetchTags(String url);
    LocalDateTime fetchDate(String url);
    String fetchSummary(String url);
}
