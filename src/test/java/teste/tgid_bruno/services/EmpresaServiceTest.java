package teste.tgid_bruno.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.GenerationType;
import teste.tgid_bruno.domain.entities.Cliente;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.domain.repositories.EmpresaRepository;
import teste.tgid_bruno.dtos.EmpresaRequestDto;
import teste.tgid_bruno.exceptions.SaldoException;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    Empresa empresa;
    List<Cliente> clientes;
    String uuidGenerated;

    @BeforeEach
    void setUp() {
        clientes = List.of(
                new Cliente("12345678910", "Cliente 1", empresa),
                new Cliente("12345678911", "Cliente 2", empresa));

        uuidGenerated = GenerationType.UUID.toString();

    }

    @Test
    @DisplayName("Should validate a Cnpj")
    void shouldValidateCnpj() {
        String cnpjValido = "12.345.678/0001-95";
        String cnpjInvalido = "123456789101213";
        String cnpjInvalido2 = "12345678910121";

        assertTrue(empresaService.validateCnpj(cnpjValido));
        assertFalse(empresaService.validateCnpj(cnpjInvalido));
        assertFalse(empresaService.validateCnpj(cnpjInvalido2));
    }

    @Test
    @DisplayName("Should find empresa by Cnpj")
    void shouldFindEmpresaByCnpj() {

        empresa = new Empresa(
                uuidGenerated,
                "123456789101112",
                "Teste Empresa",
                100.00,
                0.05f,
                clientes);

        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.of(empresa));

        Optional<Empresa> response = empresaService.findByCnpj("123456789101112");

        assertEquals(empresa, response.get());
        assertEquals(2, response.get().getClientes().size());
        assertEquals(100, response.get().getSaldo());

    }

    @Test
    @DisplayName("Should not find empresa by Cnpj when cnpj is blank or null")
    void shouldNotFindEmpresaByCnpjWhenCnpjIsBlankOrNull() {

        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.empty());

        Optional<Empresa> cnpjBlank = empresaService.findByCnpj("");
        Optional<Empresa> cnpjNull = empresaService.findByCnpj(null);

        assertTrue(cnpjBlank.isEmpty());
        assertTrue(cnpjNull.isEmpty());

    }

    @Test
    @DisplayName("Should find empresa by Id")
    void shouldFindEmpresaById() {

        empresa = new Empresa(
                uuidGenerated,
                "123456789101112",
                "Teste Empresa",
                100.00,
                0.05f,
                clientes);

        when(empresaRepository.findById(anyString())).thenReturn(Optional.of(empresa));

        Optional<Empresa> response = empresaService.findById(uuidGenerated);

        assertEquals(empresa, response.get());
        assertEquals(uuidGenerated, response.get().getId());
        assertEquals(2, response.get().getClientes().size());
        assertEquals(100, response.get().getSaldo());

    }

    @Test
    @DisplayName("Should not find empresa by Id when Id is blank or null")
    void shouldNotFindEmpresaByIdWhenIdIsBlankOrNull() {

        when(empresaRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Empresa> idBlank = empresaService.findById("");
        Optional<Empresa> idNull = empresaService.findById(null);

        assertTrue(idBlank.isEmpty());
        assertTrue(idNull.isEmpty());

    }

    @Test
    @DisplayName("Should create a new empresa")
    void shouldCreateANewEmpresa() {

        // dados que 'vieram do controller'
        EmpresaRequestDto empresaRequestDto = new EmpresaRequestDto("123456789101112", "Nova empresa");

        // objeto que vai ser enviado para o repositorio
        Empresa novaEmpresa = new Empresa(empresaRequestDto.cnpj(), empresaRequestDto.nome());
        novaEmpresa.setSaldo(0.0);
        novaEmpresa.setTaxaServico(0.05f);

        // objeto que vai voltar do repositorio
        Empresa empresaResponse = new Empresa(novaEmpresa.getCnpj(), novaEmpresa.getNome());
        empresaResponse.setId(uuidGenerated);
        empresaResponse.setSaldo(0.0);
        empresaResponse.setTaxaServico(0.05f);

        when(empresaService.findByCnpj(empresaRequestDto.cnpj())).thenReturn(Optional.empty());

        when(empresaRepository.save(novaEmpresa)).thenReturn(empresaResponse);

        Empresa response = empresaService.newEmpresa(empresaRequestDto);

        assertEquals(response, empresaResponse);
        assertEquals(uuidGenerated, response.getId());
        assertEquals(0, response.getClientes().size());
        assertEquals(0, response.getSaldo());

        verify(empresaRepository, times(1)).save(novaEmpresa);
    }

    @Test
    @DisplayName("Should not create a new empresa when cnpj already exists")
    void shouldNotCreateANewEmpresaWhehnCnpjAlreadyExists() {

        // dados que 'vieram do controller'
        EmpresaRequestDto empresaRequestDto = new EmpresaRequestDto("123456789101112", "Nova empresa");

        // objeto que vai voltar do repositorio
        Empresa empresaResponse = new Empresa(empresaRequestDto.cnpj(), empresaRequestDto.nome());
        empresaResponse.setId(uuidGenerated);
        empresaResponse.setSaldo(0.0);
        empresaResponse.setTaxaServico(0.05f);

        when(empresaService.findByCnpj(empresaRequestDto.cnpj())).thenReturn(Optional.of(empresaResponse));

        Empresa response = empresaService.newEmpresa(empresaRequestDto);

        assertNull(response);

        verify(empresaRepository, times(0)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("should credit amount and update balance correctly")
    void shouldCreditAmountAndUpdateBalanceCorrectly() {
        empresa = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", 0.00, 0.05f, clientes);

        // Cálculo do saldo esperado após o crédito de 100.0
        double valorAdicionar = 100.0;
        double saldoEsperado = empresa.getSaldo() + (valorAdicionar - (valorAdicionar * empresa.getTaxaServico())); // 95.0

        Empresa expectedResponse = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", saldoEsperado, 0.05f,
                clientes);

        when(empresaRepository.save(any(Empresa.class))).thenReturn(expectedResponse);

        empresaService.credit(empresa, valorAdicionar);

        assertEquals(expectedResponse.getSaldo(), empresa.getSaldo(), "Saldo should match expectedResponse");
        assertEquals(expectedResponse, empresa, "Empresa should be equal to expectedResponse");

        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("should debit amount and update balance correctly")
    void shouldDebitAmountAndUpdateBalanceCorrectly() {
        empresa = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", 100.00, 0.05f, clientes);

        // Cálculo do saldo esperado após o debito de 45.0
        double valorDebitar = 45.0;
        double saldoEsperado = empresa.getSaldo() - valorDebitar; // 55.0

        Empresa expectedResponse = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", saldoEsperado, 0.05f,
                clientes);

        when(empresaRepository.save(any(Empresa.class))).thenReturn(expectedResponse);

        empresaService.debit(empresa, valorDebitar);

        assertEquals(expectedResponse.getSaldo(), empresa.getSaldo(), "Saldo should match expectedResponse");
        assertEquals(expectedResponse, empresa, "Empresa should be equal to expectedResponse");

        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("should not debit amount and update balance when value is invalid")
    void shouldNotDebitAmountAndUpdateBalanceWhenValueIsInvalid() {
        empresa = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", 100.00, 0.05f, clientes);

        // do saldo esperado após o debito de 145.0
        double valorDebitar = 145.0;

        assertThrows(SaldoException.class, () -> {
            empresaService.debit(empresa, valorDebitar);
        }, "Saldo insuficiente para saque! verifique o valor e tente novamente.");

        verify(empresaRepository, times(0)).save(any(Empresa.class));
    }
}
