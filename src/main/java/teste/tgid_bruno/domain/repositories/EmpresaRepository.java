package teste.tgid_bruno.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teste.tgid_bruno.domain.entities.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {
}
