package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.Tag;
import org.AFM.rssbridge.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping("/allMainTags")
    public ResponseEntity<List<Tag>> getAllTags(){
        return ResponseEntity.ok(tagService.getAllTags());
    }

}
