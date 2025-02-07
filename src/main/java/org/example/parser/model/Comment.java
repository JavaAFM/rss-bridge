package org.example.parser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"news"})
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;

    @Column(name = "publication_time")
    private String time;

    private int likes;

    private String content;

    @ManyToOne
    @JoinColumn(name = "news_id", referencedColumnName = "id")
    @JsonIgnore
    private News news;

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
