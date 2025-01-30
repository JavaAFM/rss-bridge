package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.scrapper.UpdateDBService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
            } else {
                System.out.println("New article saved: " + news.getTitle());
                newsRepository.save(news);
            }
        }
        List<News> savedNews = newsRepository.findAll();
        for (News news : savedNews) {
            if (news.getPublicationDate().isBefore(LocalDateTime.now().minusMonths(3))){
                newsRepository.delete(news);
            }
        }
    }
}
