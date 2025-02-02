package org.AFM.rssbridge.repository.spec;

import org.AFM.rssbridge.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIin(String iin);

    Page<User> findAll(Pageable pageable);
}
