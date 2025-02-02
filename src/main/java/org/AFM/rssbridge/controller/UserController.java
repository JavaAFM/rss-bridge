package org.AFM.rssbridge.controller;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.model.User;
import org.AFM.rssbridge.service.RSSUserDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
public class UserController {

    private final RSSUserDetailService userDetailService;

    @GetMapping("/allUsers")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userDetailService.getAllUsers(pageable));
    }

}

