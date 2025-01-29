package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.scrapper.UpdateDBService;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UpdateDBImpl implements UpdateDBService {
    private final NewsRepository newsRepository;

    @Override
    public void UpdateDB(List<News> parsedNews) {
        for (News news : parsedNews) {
            Optional<News> existingNews = newsRepository.findByTitle(news.getTitle());
            if (existingNews.isPresent()) {
                System.out.println("Article already exists: " + news.getTitle());
                continue;
            } else {
                System.out.println("New article saved: " + news.getTitle());
                newsRepository.save(news);
            }
        }
    }

    @Override
    public List<News> toNews(Elements elements) throws NotFoundException {
        return List.of();
    }

    @Override
    public Elements allNewsElements() {
        return null;
    }

    @Override
    public Document connectToWebPage(String url) throws IOException {
        return null;
    }
}
