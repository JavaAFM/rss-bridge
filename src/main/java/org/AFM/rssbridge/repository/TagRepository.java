package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> getTagById(Long id);
    Optional<Tag> getTagByName(String name);
    List<Tag> getTagsByGroup(String group);
}
