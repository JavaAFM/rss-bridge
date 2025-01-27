package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Source;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.scrapper.*;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@AllArgsConstructor
public class MainController {
    private final TengriService tengriService;
    private final KazTagService kaztagService;
    private final AzattyqService azattyqService;
    private final OrdaService ordaService;
    private final ZakonService zakonService;

    @GetMapping("/tengri")
    public ResponseEntity<List<News>> getTengriNews() throws NotFoundException {
        Elements elements = tengriService.allNewsElements();
        return ResponseEntity.ok(tengriService.toNews(elements));
    }

    @GetMapping("/kaztag")
    public ResponseEntity<List<News>> getKaztagNews() throws NotFoundException {
        Elements elements = kaztagService.allNewsElements();
        return ResponseEntity.ok(kaztagService.toNews(elements));
    }

    @GetMapping("/azattyq")
    public ResponseEntity<List<News>> getAzattyqNews() throws NotFoundException {
        Elements elements = azattyqService.allNewsElements();
        return ResponseEntity.ok(azattyqService.toNews(elements));
    }

    @GetMapping("/zakon")
    public ResponseEntity<List<News>> getZakonNews() throws NotFoundException {
        Elements elements = zakonService.allNewsElements();
        return ResponseEntity.ok(zakonService.toNews(elements));
    }

    @GetMapping("/orda")
    public ResponseEntity<List<News>> getOrdaNews() throws NotFoundException {
        Elements elements = ordaService.allNewsElements();
        return ResponseEntity.ok(ordaService.toNews(elements));
    }


    @GetMapping("/rss")
    public ResponseEntity<List<News>> getNews(){
        return null;
    }




}
