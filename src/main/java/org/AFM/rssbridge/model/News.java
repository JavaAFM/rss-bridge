package org.AFM.rssbridge.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class News {

    private String id;

    private String title;

    private String url;

    private String image_url;

    private String summary;

    private List<String> tags;

    private String mainText;

    private List<Comment> comments;

    private LocalDateTime publicationDate;

}
