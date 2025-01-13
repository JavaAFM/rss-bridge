package org.AFM.rssbridge.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "news")
public class News {

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("url")
    private String url;

    @Field("img_url")
    private String image_url;

    @Field("summary")
    private String summary;

    @Field("publishedDate")
    private LocalDate publicationDate;

}
