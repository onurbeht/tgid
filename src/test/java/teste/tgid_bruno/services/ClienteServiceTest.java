package teste.tgid_bruno.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.GenerationType;
import teste.tgid_bruno.domain.entities.Cliente;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.domain.repositories.ClienteRepository;
import teste.tgid_bruno.dtos.ClienteRequestDto;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Should validate a cpf")
    void shouldValidateACpf() {
        String cpfValido = "482.472818-56";
        String cpfInvalido = "123.456.789-10";
        String cpfInvalido2 = "123.456.789";
        String cpfInvalido3 = "11111111111";

        assertTrue(clienteService.validateCpf(cpfValido));
        assertFalse(clienteService.validateCpf(cpfInvalido));
        assertFalse(clienteService.validateCpf(cpfInvalido2));
        assertFalse(clienteService.validateCpf(cpfInvalido3));
    }

    @Test
    @DisplayName("Should create a new cliente")
    void shouldCreateANewCliente() {
        Empresa empresa = new Empresa("1234567891011", "Empresa");
        ClienteRequestDto clienteRequestDto = new ClienteRequestDto("12345678910", "Teste Cliente", "idEmpresa");

        Cliente newCliente = new Cliente(clienteRequestDto.cpf(), clienteRequestDto.nome(), empresa);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(newCliente);

        Cliente response = clienteService.newCliente(clienteRequestDto, empresa);

        assertEquals(response, newCliente);
    }

    @Test
    @DisplayName("Should find a cliente by cpf")
    void shouldFindAClienteByCpf() {
        Empresa empresa = new Empresa("1234567891011", "Empresa");
        String uuidGenerated = GenerationType.UUID.toString();
        Cliente cliente = new Cliente(uuidGenerated, "12345678910", "Cliente teste", empresa);

        when(clienteRepository.findByCpf(anyString())).thenReturn(Optional.of(cliente));

        Optional<Cliente> response = clienteService.findByCpf("12345678910");

        assertEquals(response.get(), cliente);
        assertEquals(response.get().getId(), uuidGenerated);
    }

    @Test
    @DisplayName("Should not find a cliente by cpf if Cpf is blank or null")
    void shouldNotFindAClienteByCpfIfCpfIsBlankOrNull() {

        when(clienteRepository.findByCpf(anyString())).thenReturn(Optional.empty());

        Optional<Cliente> cpfIsBlank = clienteService.findByCpf("");
        Optional<Cliente> cpfIsNull = clienteService.findByCpf(null);

        assertTrue(cpfIsBlank.isEmpty());
        assertTrue(cpfIsNull.isEmpty());
    }

    @Test
    @DisplayName("Should find a cliente by id")
    void shouldFindAClienteById() {
        Empresa empresa = new Empresa("1234567891011", "Empresa");
        String uuidGenerated = GenerationType.UUID.toString();
        Cliente cliente = new Cliente(uuidGenerated, "12345678910", "Cliente teste", empresa);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(cliente));

        Optional<Cliente> response = clienteService.findById("12345678910");

        assertEquals(response.get(), cliente);
        assertEquals(response.get().getId(), uuidGenerated);
        assertEquals(response.get().getEmpresa(), empresa);
    }

    @Test
    @DisplayName("Should not find a cliente by id if id is blank or null")
    void shouldNotFindAClienteByIdIfIdIsBlankOrNull() {

        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());

        Optional<Cliente> idIsBlank = clienteService.findById("");
        Optional<Cliente> idIsNull = clienteService.findById(null);

        assertTrue(idIsBlank.isEmpty());
        assertTrue(idIsNull.isEmpty());
    }

}
