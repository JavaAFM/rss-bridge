package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.dto.response.SourceDto;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.mapper.SourceMapper;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.repository.SourceRepository;
import org.AFM.rssbridge.service.SourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SourceServiceImpl implements SourceService {
    private final SourceRepository sourceRepository;
    private final NewsRepository newsRepository;
    private SourceMapper sourceMapper;

    @Override
    public List<SourceDto> getAllSources() {
        List<String> lastTitles = newsRepository.getLastTitles();
        List<Source> sources = sourceRepository.findAll();
        return sourceMapper.fromSourcesToDtos(sources, lastTitles);
    }

    @Override
    public Source getSourceByName(String name) throws NotFoundException {
        return sourceRepository.getSourceByName(name).orElseThrow(() -> new NotFoundException("No such source found..."));
    }
}
