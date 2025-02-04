package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.NewsTag;
import org.AFM.rssbridge.repository.NewsTagRepository;
import org.AFM.rssbridge.service.NewsTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NewsTagServiceImpl implements NewsTagService {
    private final NewsTagRepository newsTagRepository;
    @Override
    public List<NewsTag> getAll() {
        return newsTagRepository.findAll();
    }
}
