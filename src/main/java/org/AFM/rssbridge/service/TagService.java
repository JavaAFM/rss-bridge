package org.AFM.rssbridge.service;

import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    Tag getTagById(Long id) throws NotFoundException;
    Tag getTagByName(String name) throws NotFoundException;
    List<Tag> getTagsByGroup(String group);
}
