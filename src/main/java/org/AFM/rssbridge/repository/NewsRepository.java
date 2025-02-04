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
    @Query("SELECT n FROM News n ORDER BY n.publicationDate DESC")
    Page<News> findAll(Pageable pageable);
    Optional<News> findByTitle(String title);

    @Query("SELECT n FROM News n WHERE n.source = :source ORDER BY n.publicationDate DESC")
    Page<News> getNewsBySource(@Param("source") Source source, Pageable pageable);

    @Query("SELECT n FROM News n ORDER BY n.publicationDate DESC")
    Page<News> getLastNews(Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.source.name = :source ORDER BY n.publicationDate DESC")
    Page<News> getLastNewsOfSource(@Param("source") String source, Pageable pageable);
}
