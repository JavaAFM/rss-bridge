package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.repository.SourceRepository;
import org.AFM.rssbridge.service.SourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SourceServiceImpl implements SourceService {
    private final SourceRepository sourceRepository;

    @Override
    public List<Source> getAllSources() {
        return sourceRepository.findAll();
    }

    @Override
    public Source getSourceByName(String name) throws NotFoundException {
        return sourceRepository.getSourceByName(name).orElseThrow(() -> new NotFoundException("No such source found..."));
    }
}
