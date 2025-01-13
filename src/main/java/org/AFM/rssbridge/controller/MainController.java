package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.service.RSSBridge;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class MainController {
    private final RSSBridge rssBridge;

    @GetMapping("/rss")
    public ResponseEntity<List<News>> getAllNews(){
        Elements elements = rssBridge.allNewsElements();
        return ResponseEntity.ok(rssBridge.toNews(elements));
    }
}
