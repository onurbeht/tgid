package teste.tgid_bruno.domain.entities;

import jakarta.persistence.GenerationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaTest {

    Empresa empresaTeste;
    String uuidGenerated;
    List<Cliente> clientes;

    @BeforeEach
    void setUp() {
        clientes = List.of(
                new Cliente("12345678910", "Cliente 1", empresaTeste),
                new Cliente("12345678911", "Cliente 2", empresaTeste));

        uuidGenerated = GenerationType.UUID.toString();

    }

    @Test
    void shouldCreateAnEmpresaWithAllArgs() {
        empresaTeste = new Empresa(
                uuidGenerated,
                "1234567891011",
                "Teste Empresa",
                100.00,
                0.05f,
                clientes);

        assertEquals(uuidGenerated, empresaTeste.getId());
        assertEquals("1234567891011", empresaTeste.getCnpj());
        assertEquals("Teste Empresa", empresaTeste.getNome());
        assertEquals(100.00, empresaTeste.getSaldo());
        assertEquals(0.05f, empresaTeste.getTaxaServico());
        assertEquals(2, empresaTeste.getClientes().size());
    }

    @Test
    void shouldCreateAnEmpresaWithCnpjAndName() {
        empresaTeste = new Empresa(
                "1234567891011",
                "Teste Empresa");
        assertEquals("1234567891011", empresaTeste.getCnpj());
        assertEquals("Teste Empresa", empresaTeste.getNome());
    }

}