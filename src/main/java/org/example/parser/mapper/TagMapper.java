package org.example.parser.mapper;

import org.example.parser.model.News;
import org.example.parser.model.NewsTag;

import java.util.List;

public interface TagMapper {
    NewsTag toTag(String tag, News news);

    List<NewsTag> toListOfTags(List<String> tags, News news);
}
