package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.NewsService;
import org.AFM.rssbridge.service.SourceService;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final SourceService sourceService;

    @Override
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @Override
    public List<News> getAllNewsFromSource(String targetSource) throws NotFoundException {
        Source source = sourceService.getSourceByName(targetSource);
        return newsRepository.getNewsBySource(source);
    }

    public boolean findByTitle(String title) {
        Optional<News> optionalNews = newsRepository.findByTitle(title);

        if (optionalNews.isPresent()) {
            return false;
        } else {
            return true;
        }
    }
}
