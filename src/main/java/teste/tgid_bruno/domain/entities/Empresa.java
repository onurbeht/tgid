package teste.tgid_bruno.domain.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "Empresas")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "cnpj", nullable = false, unique = true)
    private String cnpj;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "taxa_servico")
    private float taxaServico;

    @OneToMany(mappedBy = "empresa")
    private List<Cliente> clientes = new ArrayList<>();

    public Empresa(String cnpj, String nome) {
        this.cnpj = cnpj;
        this.nome = nome;
    }

}
