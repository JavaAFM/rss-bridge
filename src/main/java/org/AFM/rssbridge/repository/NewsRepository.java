package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.News;
import org.AFM.rssbridge.model.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    Page<News> findAll(Pageable pageable);

    Page<News> getNewsBySource(Source source, Pageable pageable);

    @Query("SELECT n FROM News n ORDER BY n.publicationDate DESC")
    Page<News> getLastNews(Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.source.name = :source ORDER BY n.publicationDate DESC")
    Page<News> getLastNewsOfSource(@Param("source") String source, Pageable pageable);

    @Query("SELECT n.title FROM News n " +
            "WHERE n.publicationDate = (SELECT MAX(n2.publicationDate) FROM News n2 WHERE n2.source.id = n.source.id) " +
            "ORDER BY n.source.name")
    List<String> getLastTitles();
}
