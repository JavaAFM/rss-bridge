package org.AFM.rssbridge.service;

import net.bytebuddy.asm.Advice;
import org.AFM.rssbridge.model.News;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.List;

public interface KazTagService extends WebSiteScrapper{
    String fetchMainText(String articleUrl);
    List<String> fetchTags(String articleUrl);
    LocalDateTime fetchDate(String articleUrl);



}
