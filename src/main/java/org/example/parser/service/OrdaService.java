package org.example.parser.service;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdaService extends WebSiteScrapper {
    String fetchMainText(String url);
    List<String> fetchTags(String url);
    LocalDateTime fetchDate(String url);
    void acceptCookie();
}