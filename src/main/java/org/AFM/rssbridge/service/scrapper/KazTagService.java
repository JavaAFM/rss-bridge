package org.AFM.rssbridge.service.scrapper;

import java.time.LocalDateTime;
import java.util.List;

public interface KazTagService extends WebSiteScrapper{
    String fetchMainText(String articleUrl);
    List<String> fetchTags(String articleUrl);
    LocalDateTime fetchDate(String articleUrl);



}
