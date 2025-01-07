package tipah_apps.product_service.restfull.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tipah_apps.product_service.restfull.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    Optional<User> findByusername(String username);
    Optional<User> findFirstByToken(String token);
    
}
