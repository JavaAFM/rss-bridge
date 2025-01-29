package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> getNewsBySource(Source source);

    Optional<News> findByTitle(String title);
}
