package org.AFM.rssbridge.repository;

import org.AFM.rssbridge.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> getSourceByName(String name);
}
