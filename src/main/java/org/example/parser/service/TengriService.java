package org.example.parser.service;

import org.example.parser.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface TengriService extends WebSiteScrapper{

    String fetchMainText(String articleUrl);
    List<Comment> fetchComments(String articleUrl);
    List<String> fetchTags(String articleUrl);
    LocalDateTime fetchDate(String articleUrl);

}
