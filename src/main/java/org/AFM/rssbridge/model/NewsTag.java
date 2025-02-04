package org.AFM.rssbridge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"news"})
@Entity
@Table(name = "news_tags")
public class NewsTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_id", referencedColumnName = "id")
    @JsonIgnore
    private News news;

    @Column(name = "tag", nullable = false)
    private String tag;
}