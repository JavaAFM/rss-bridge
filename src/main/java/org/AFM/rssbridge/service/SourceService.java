package org.AFM.rssbridge.service;

import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;

import java.util.List;

public interface SourceService {
    List<Source> getAllSources();
    Source getSourceByName(String name) throws NotFoundException;
}
