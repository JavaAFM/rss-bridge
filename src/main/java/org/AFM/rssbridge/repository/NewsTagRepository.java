package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.NewsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsTagRepository extends JpaRepository<NewsTag, Long> {
}
