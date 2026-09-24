package ms.auth.Repository;

import ms.auth.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Spring Data JPA crea la query de SQL automáticamente con solo nombrar bien el método
    Optional<Usuario> findByEmail(String email);
    
}