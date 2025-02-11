package org.example.parser.service.impl;

import lombok.AllArgsConstructor;
import org.example.parser.model.News;
import org.example.parser.repository.CommentRepository;
import org.example.parser.repository.NewsRepository;
import org.example.parser.repository.NewsTagRepository;
import org.example.parser.service.UpdateDBService;
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

    @Override
    @Transactional
    public void deleteNews3Month() {
        newsRepository.findAll().stream()
                .filter(news -> news.getPublicationDate().isBefore(LocalDateTime.now().minusMonths(3)))
                .forEach(news -> {
                    commentRepository.deleteAll(news.getComments());
                    newsTagRepository.deleteAll(news.getTags());
                    newsRepository.delete(news);
                });
    }

    @Override
    public void insertNews(News news) {
        Optional<News> existingNews = newsRepository.findByTitle(news.getTitle());
        if (existingNews.isPresent()) {
            LOGGER.warn("Article already exists: {}", news.getTitle());
        } else {
            LOGGER.warn("New article saved: {}", news.getTitle());
            newsRepository.save(news);

            if (news.getTags() != null) {
                news.getTags().forEach(tag -> tag.setNews(news));
                newsTagRepository.saveAll(news.getTags());
            }
            if (news.getComments() != null) {
                news.getComments().forEach(comment -> comment.setNews(news));
                commentRepository.saveAll(news.getComments());
            }


        }
    }
}
