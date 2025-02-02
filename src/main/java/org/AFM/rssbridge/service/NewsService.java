package org.AFM.rssbridge.service;

import org.AFM.rssbridge.dto.request.FilterRequest;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NewsService {
    Page<News> getAllNews(Pageable pageable);
    Page<News> getAllNewsFromSource(String source, Pageable pageable) throws NotFoundException;
    Page<News> filter(FilterRequest filterRequest, Pageable pageable);
    Page<News> lastNews(Pageable pageable);
    Page<News> lastNewsOfSource(String source, Pageable pageable);

}
