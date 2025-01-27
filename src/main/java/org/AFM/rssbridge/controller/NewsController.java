package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/allNews")
    private ResponseEntity<List<News>> getAllNews(){
        return ResponseEntity.ok(newsService.getAllNews());
    }

    @GetMapping("/getNewsBySource")
    private ResponseEntity<List<News>> getNewsBySource(@RequestParam String source) throws NotFoundException {
        return ResponseEntity.ok(newsService.getAllNewsFromSource(source));
    }


}
