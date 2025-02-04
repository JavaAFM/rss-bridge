package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.dto.response.SourceDto;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.mapper.SourceMapper;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.repository.SourceRepository;
import org.AFM.rssbridge.service.SourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SourceServiceImpl implements SourceService {
    private final SourceRepository sourceRepository;
    private final NewsRepository newsRepository;
    private SourceMapper sourceMapper;

    @Override
    public List<SourceDto> getAllSources() {
        List<Source> sources = sourceRepository.findAll();
        List<String> lastTitles = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 1);
        for (Source source : sources) {
            Page<News> lastNewsPage = newsRepository.getLastNewsOfSource(source.getName(), pageable);

            if (!lastNewsPage.isEmpty()) {
                lastTitles.add(lastNewsPage.getContent().get(0).getTitle());
            } else {
                lastTitles.add("No news available");
            }
        }
        return sourceMapper.fromSourcesToDtos(sources, lastTitles);
    }

    @Override
    public Source getSourceByName(String name) throws NotFoundException {
        return sourceRepository.getSourceByName(name).orElseThrow(() -> new NotFoundException("No such source found..."));
    }

    @Override
    public List<String> allTypes() {
        return sourceRepository.findAll().stream().map(Source::getType).toList();
    }

    @Override
    public List<String> allNames() {
        return sourceRepository.findAll().stream().map(Source::getName).toList();
    }
}
