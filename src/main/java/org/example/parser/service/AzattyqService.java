package org.example.parser.service;

import java.time.LocalDateTime;

public interface AzattyqService extends WebSiteScrapper{
    String fetchMainText(String articleUrl);
    LocalDateTime fetchDate(String articleUrl);
}
