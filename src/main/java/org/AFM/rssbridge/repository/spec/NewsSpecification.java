package org.AFM.rssbridge.repository.spec;

import org.AFM.rssbridge.dto.request.FilterRequest;
import org.AFM.rssbridge.model.News;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;

public class NewsSpecification {
    public static Specification<News> filterByCriteria(FilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (filterRequest.getSource_name() != null && !filterRequest.getSource_name().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("source").get("name"), filterRequest.getSource_name()));
            }

            if (filterRequest.getSource_type() != null && !filterRequest.getSource_type().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("source").get("type"), filterRequest.getSource_type()));
            }

            if (filterRequest.getTitle() != null && !filterRequest.getTitle().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + filterRequest.getTitle().toLowerCase() + "%"));
            }

            if (filterRequest.getTags() != null && !filterRequest.getTags().isEmpty()) {
                Join<Object, Object> tagJoin = root.join("tags");
                predicates = criteriaBuilder.and(predicates,
                        tagJoin.get("tag").in(filterRequest.getTags()));
            }

            if (filterRequest.getFrom() != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("publicationDate"), filterRequest.getFrom()));
            }
            if (filterRequest.getTo() != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("publicationDate"), filterRequest.getTo()));
            }

            return predicates;
        };
    }
}
