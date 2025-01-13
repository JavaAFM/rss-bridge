package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.NewsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;

    @Override
    public List<News> getAll() {
        return newsRepository.getAll();
    }

    @Override
    public News findByText(String text) {
        return null;
    }

}
