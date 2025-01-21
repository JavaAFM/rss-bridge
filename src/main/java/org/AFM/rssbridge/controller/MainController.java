package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.*;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class MainController {
    private final TengriService tengriService;
    private final KazTagService kaztagService;
    private final AzattyqService azattyqService;
    private final OrdaService ordaService;
    private final ZakonService zakonService;

    @GetMapping("/tengri")
    public ResponseEntity<List<News>> getTengriNews(){
        Elements elements = tengriService.allNewsElements();
        return ResponseEntity.ok(tengriService.toNews(elements));
    }

    @GetMapping("/kaztag")
    public ResponseEntity<List<News>> getKaztagNews(){
        Elements elements = kaztagService.allNewsElements();
        return ResponseEntity.ok(kaztagService.toNews(elements));
    }

    @GetMapping("/azattyq")
    public ResponseEntity<List<News>> getAzattyqNews(){
        Elements elements = azattyqService.allNewsElements();
        return ResponseEntity.ok(azattyqService.toNews(elements));
    }

    @GetMapping("/zakon")
    public ResponseEntity<List<News>> getZakonNews(){
        Elements elements = zakonService.allNewsElements();
        return ResponseEntity.ok(zakonService.toNews(elements));
    }

    @GetMapping("/orda")
    public ResponseEntity<List<News>> getOrdaNews(){
        Elements elements = ordaService.allNewsElements();
        return ResponseEntity.ok(ordaService.toNews(elements));
    }
}
