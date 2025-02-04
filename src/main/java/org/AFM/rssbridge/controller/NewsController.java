package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.dto.request.FilterRequest;
import org.AFM.rssbridge.dto.request.TagRequest;
import org.AFM.rssbridge.dto.response.ModelResponse;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.NewsService;
import org.checkerframework.checker.units.qual.N;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final RestTemplate restTemplate;

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

    @PostMapping("/filter")
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

    @PostMapping("/predict")
    public ResponseEntity<ModelResponse> predict(
            @RequestBody TagRequest request
    ) {
        String fastApiUrl = "http://localhost:8000/predict/";

        ResponseEntity<ModelResponse[]> response = restTemplate.postForEntity(fastApiUrl, request, ModelResponse[].class);
        ModelResponse[] modelResponseArray = response.getBody();

        if (modelResponseArray != null && modelResponseArray.length > 0) {
            return ResponseEntity.ok(modelResponseArray[0]);
        } else {
            return ResponseEntity.noContent().build();
        }
    }


}
