package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.repository.NewsRepository;
import org.AFM.rssbridge.service.scrapper.*;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class MainController {
    private final TengriService tengriService;
    private final KazTagService kaztagService;
    private final AzattyqService azattyqService;
    private final OrdaService ordaService;
    private final ZakonService zakonService;
    private final NewsRepository newsRepository;

    @Scheduled(fixedRate = 120000)
    @GetMapping("/tengri")
    public ResponseEntity<List<News>> getTengriNews() throws NotFoundException {
        Elements elements = tengriService.allNewsElements();
        List<News> parsedNews = tengriService.toNews(elements);
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
        return ResponseEntity.ok(tengriService.toNews(elements));
    }

    @Scheduled(fixedRate = 120000)
    @GetMapping("/kaztag")
    public ResponseEntity<List<News>> getKaztagNews() throws NotFoundException {
        Elements elements = kaztagService.allNewsElements();
        List<News> parsedNews = kaztagService.toNews(elements);
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
        return ResponseEntity.ok(kaztagService.toNews(elements));
    }

    @Scheduled(fixedRate = 120000)
    @GetMapping("/azattyq")
    public ResponseEntity<List<News>> getAzattyqNews() throws NotFoundException {
        Elements elements = azattyqService.allNewsElements();
        List<News> parsedNews = azattyqService.toNews(elements);
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
        return ResponseEntity.ok(azattyqService.toNews(elements));
    }

    @Scheduled(fixedRate = 120000)
    @GetMapping("/zakon")
    public ResponseEntity<List<News>> getZakonNews() throws NotFoundException {
        Elements elements = zakonService.allNewsElements();
        List<News> parsedNews = zakonService.toNews(elements);
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
        return ResponseEntity.ok(zakonService.toNews(elements));
    }

    @Scheduled(fixedRate = 120000)
    @GetMapping("/orda")
    public ResponseEntity<List<News>> getOrdaNews() throws NotFoundException {
        Elements elements = ordaService.allNewsElements();
        List<News> parsedNews = ordaService.toNews(elements);
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
        return ResponseEntity.ok(ordaService.toNews(elements));
    }


    @GetMapping("/rss")
    public ResponseEntity<List<News>> getNews(){
        return null;
    }




}
