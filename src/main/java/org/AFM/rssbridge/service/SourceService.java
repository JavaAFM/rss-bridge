package org.AFM.rssbridge.service;

import org.AFM.rssbridge.dto.response.SourceDto;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;

import java.util.List;

public interface SourceService {
    List<SourceDto> getAllSources();
    Source getSourceByName(String name) throws NotFoundException;
    List<String> allTypes();
    List<String> allNames();
}
