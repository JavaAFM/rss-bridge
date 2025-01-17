package org.AFM.rssbridge.service.impl;

import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.KazTagService;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KazTagServiceImpl implements KazTagService {
    @Override
    public List<News> toNews(Elements elements) {
        return null;
    }

    @Override
    public Elements allNewsElements() {
       return null;
    }
}
