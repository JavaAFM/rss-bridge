package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.News;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NewsRepository extends MongoRepository<String, News> {
    List<News> getAll();
}
