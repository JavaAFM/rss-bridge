package org.AFM.rssbridge.model;

import lombok.*;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(author, comment.author) && 
               Objects.equals(content, comment.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, content);
    }
}
