package org.AFM.rssbridge.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment {

    private String id;

    private String author;

    private String time;

    private int likes;

    private String content;

}
