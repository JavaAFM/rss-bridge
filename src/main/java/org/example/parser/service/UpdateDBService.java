package org.example.parser.service;

import java.util.List;
import org.example.parser.model.News;

public interface UpdateDBService{
    void deleteNews3Month();
    void insertNews(News news);
}
