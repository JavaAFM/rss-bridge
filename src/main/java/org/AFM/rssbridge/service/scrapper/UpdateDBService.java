package org.AFM.rssbridge.service.scrapper;

import java.util.List;
import org.AFM.rssbridge.model.News;

public interface UpdateDBService extends WebSiteScrapper{
    void UpdateDB(List<News> parsedNews);
}
