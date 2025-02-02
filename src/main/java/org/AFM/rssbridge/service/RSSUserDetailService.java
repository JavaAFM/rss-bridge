package org.AFM.rssbridge.service;

import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface RSSUserDetailService extends UserDetailsService {
    void saveUser(User user) throws NotFoundException;
    Page<User> getAllUsers(Pageable pageable);
}
