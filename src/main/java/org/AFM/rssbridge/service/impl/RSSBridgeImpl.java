package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.RSSBridge;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RSSBridgeImpl implements RSSBridge {
    private final NewsRepository newsRepository;


}
