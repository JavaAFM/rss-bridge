package org.AFM.rssbridge.service.impl;

import lombok.AllArgsConstructor;
import org.AFM.rssbridge.exception.NotFoundException;
import org.AFM.rssbridge.model.Tag;
import org.AFM.rssbridge.repository.TagRepository;
import org.AFM.rssbridge.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    @Override
    public Tag getTagById(Long id) throws NotFoundException {
        return tagRepository.getTagById(id).orElseThrow(()-> new NotFoundException("No such tag found"));
    }

    @Override
    public Tag getTagByName(String name) throws NotFoundException {
        return tagRepository.getTagByName(name).orElseThrow(() -> new NotFoundException("No such tag found"));
    }

    @Override
    public List<Tag> getTagsByGroup(String group) {
        return tagRepository.getTagsByGroup(group);
    }
}
