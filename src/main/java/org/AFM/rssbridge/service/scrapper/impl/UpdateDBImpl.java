package org.AFM.rssbridge.service.scrapper.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.repository.CommentRepository;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.repository.NewsTagRepository;
import org.AFM.rssbridge.service.scrapper.UpdateDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UpdateDBImpl implements UpdateDBService {
    private final NewsRepository newsRepository;
    private final NewsTagRepository newsTagRepository;
    private final CommentRepository commentRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDBImpl.class);

    @Transactional
    @Override
    public void UpdateDB(List<News> parsedNews) {
        parsedNews.forEach(news -> {
            Optional<News> existingNews = newsRepository.findByTitle(news.getTitle());
            if (existingNews.isPresent()) {
                LOGGER.warn("Article already exists: {}", news.getTitle());
            } else {
                LOGGER.warn("New article saved: {}", news.getTitle());
                news.getTags().forEach(tag -> tag.setNews(news));
                news.getComments().forEach(comment -> comment.setNews(news));

                newsRepository.save(news);

                newsTagRepository.saveAll(news.getTags());
                commentRepository.saveAll(news.getComments());
            }
        });

        newsRepository.findAll().stream()
                .filter(news -> news.getPublicationDate().isBefore(LocalDateTime.now().minusMonths(3)))
                .forEach(news -> {
                    commentRepository.deleteAll(news.getComments());
                    newsTagRepository.deleteAll(news.getTags());
                    newsRepository.delete(news);
                });
    }
}
