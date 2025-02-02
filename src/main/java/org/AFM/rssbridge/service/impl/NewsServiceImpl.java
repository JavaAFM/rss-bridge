package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.dto.request.FilterRequest;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.repository.spec.NewsSpecification;
import org.AFM.rssbridge.service.NewsService;
import org.AFM.rssbridge.service.SourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final SourceService sourceService;

    @Override
    public Page<News> getAllNews(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    public Page<News> getAllNewsFromSource(String targetSource, Pageable pageable) throws NotFoundException {
        Source source = sourceService.getSourceByName(targetSource);
        return newsRepository.getNewsBySource(source, pageable);
    }

    @Override
    public Page<News> filter(FilterRequest filterRequest, Pageable pageable) {
        return newsRepository.findAll(NewsSpecification.filterByCriteria(filterRequest), pageable);
    }

    @Override
    public Page<News> lastNews(Pageable pageable) {
        return newsRepository.getLastNews(pageable);
    }

    @Override
    public Page<News> lastNewsOfSource(String source, Pageable pageable) {
        return newsRepository.getLastNewsOfSource(source, pageable);
    }
}
