package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.dto.request.FilterRequest;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.NewsService;
import org.checkerframework.checker.units.qual.N;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/allNews")
    private ResponseEntity<Page<News>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.getAllNews(pageable));
    }

    @GetMapping("/getNewsBySource")
    private ResponseEntity<Page<News>> getNewsBySource(
            @RequestParam String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) throws NotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.getAllNewsFromSource(source, pageable));
    }

    @GetMapping("/filter")
    private ResponseEntity<Page<News>> filter(
            @RequestBody FilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.filter(filterRequest, pageable));
    }

    @GetMapping("/lastNews")
    private ResponseEntity<Page<News>> lastNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.lastNews(pageable));
    }

    @GetMapping("/lastNewsOfSource")
    private ResponseEntity<Page<News>> lastNewsOfSource(
            @RequestParam String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.lastNewsOfSource(source, pageable));
    }



}
