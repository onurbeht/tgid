package teste.tgid_bruno.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teste.tgid_bruno.domain.entities.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {

    public Optional<Cliente> findByCpf(String cpf);
}
