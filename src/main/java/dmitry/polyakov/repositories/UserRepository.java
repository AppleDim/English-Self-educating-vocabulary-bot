package dmitry.polyakov.repositories;

import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
