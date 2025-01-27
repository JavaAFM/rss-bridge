package org.AFM.rssbridge.service;

import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;

import java.util.List;

public interface NewsService {
    List<News> getAllNews();
    List<News> getAllNewsFromSource(String source) throws NotFoundException;

}
