package org.AFM.rssbridge.service;

import org.AFM.rssbridge.model.News;

import java.util.List;

public interface NewsService {
    List<News> getAll();
    News findByText(String text);
}
