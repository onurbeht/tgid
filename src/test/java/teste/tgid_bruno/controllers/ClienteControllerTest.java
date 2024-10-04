package teste.tgid_bruno.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import teste.tgid_bruno.domain.entities.Cliente;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.dtos.ClienteRequestDto;
import teste.tgid_bruno.dtos.CreditRequestDto;
import teste.tgid_bruno.services.ClienteService;
import teste.tgid_bruno.services.EmpresaService;

@WebMvcTest(controllers = ClienteController.class)
@ExtendWith(MockitoExtension.class)
public class ClienteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ClienteService clienteService;

    @MockBean
    EmpresaService empresaService;

    ClienteRequestDto clienteRequestDto;
    CreditRequestDto creditRequestDto;
    Cliente cliente;
    Empresa empresa;
    String empresaUUID;
    String clienteUUID;

    @BeforeEach
    void setUp() {
        empresaUUID = UUID.randomUUID().toString();
        clienteUUID = UUID.randomUUID().toString();

        cliente = new Cliente();

        empresa = new Empresa("123456789101112", "Nome Empresa");
        empresa.setId(empresaUUID);
        empresa.setSaldo(0.0);
        empresa.setTaxaServico(0.05f);
        empresa.getClientes().add(cliente);

    }

    @Test
    @DisplayName("Should return a cliente by Id")
    void ClienteController_getCliente_returnCliente() throws Exception {
        String requestId = clienteUUID;

        cliente.setId(clienteUUID);
        cliente.setCpf("12345678910");
        cliente.setNome("Nome Cliente");
        cliente.setEmpresa(empresa);

        when(clienteService.findById(requestId)).thenReturn(Optional.of(cliente));

        ResultActions response = mockMvc.perform(
                get("/api/clientes/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON));

        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteUUID))
                .andExpect(jsonPath("$.cpf").value(cliente.getCpf()))
                .andExpect(jsonPath("$.nome").value(cliente.getNome()));

        verify(clienteService, times(1)).findById(requestId);

        verifyNoMoreInteractions(clienteService);

    }

    @Test
    @DisplayName("Should not return a cleinte by id, when id does not exist")
    void ClienteController_getCliente_returnNotFound() throws Exception {
        when(clienteService.findById("abc")).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(
                get("/api/clientes/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON));

        response
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(clienteService, times(1)).findById("abc");
        verifyNoMoreInteractions(clienteService);
    }

    @Test
    @DisplayName("Should not execute request, when id is empty in path variable")
    void ClienteController_getCliente_shouldNotExecute() throws Exception {
        ResultActions response = mockMvc.perform(
                get("/api/clientes/")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNotFound());

        verifyNoInteractions(clienteService);

    }

    @Test
    @DisplayName("Should create a new cliente, when data is valid")
    void ClienteController_newCliente_returnCliente() throws Exception {

        String validCpf = "123.456.789-10";

        clienteRequestDto = new ClienteRequestDto(validCpf, "Nome Cliente", empresaUUID);

        cliente.setId(clienteUUID);
        cliente.setCpf(validCpf);
        cliente.setNome("Nome Cliente");
        cliente.setEmpresa(empresa);

        when(clienteService.validateCpf(validCpf)).thenReturn(true);

        when(clienteService.findByCpf(validCpf)).thenReturn(Optional.empty());

        when(empresaService.findById(clienteRequestDto.id_empresa())).thenReturn(Optional.of(empresa));

        when(clienteService.newCliente(clienteRequestDto, empresa)).thenReturn(cliente);

        ResultActions response = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto)));

        response
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(redirectedUrl("/api/clientes/" + cliente.getId()))
                .andExpect(jsonPath("$.id").value(cliente.getId()))
                .andExpect(jsonPath("$.cpf").value(cliente.getCpf()))
                .andExpect(jsonPath("$.nome").value("Nome Cliente"));

        verify(clienteService, times(1)).validateCpf(validCpf);
        verify(clienteService, times(1)).findByCpf(validCpf);
        verify(empresaService, times(1)).findById(clienteRequestDto.id_empresa());
        verify(clienteService, times(1)).newCliente(clienteRequestDto, empresa);

        verifyNoMoreInteractions(clienteService);
        verifyNoMoreInteractions(empresaService);
    }

    @Test
    @DisplayName("Should not create a new cliente, when cpf is invalid")
    void ClienteController_newCliente_returnBadRequestWhenCpfIsInvalid() throws Exception {

        String invalidCpf = "123.456.789-10";
        String bodyExpected = "Informe um CPF válido!";

        clienteRequestDto = new ClienteRequestDto(invalidCpf, "Nome Cliente", empresaUUID);

        when(clienteService.validateCpf(invalidCpf)).thenReturn(false);

        ResultActions response = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto)));

        response
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$").value(bodyExpected));

        verify(clienteService, times(1)).validateCpf(invalidCpf);

        verifyNoMoreInteractions(clienteService);
        verifyNoInteractions(empresaService);
    }

    @Test
    @DisplayName("Should not create a new cliente, when cpf already exist")
    void ClienteController_newCliente_returnBadRequestWhenCpfAlreadyExist() throws Exception {

        String registeredCpf = "123.456.789-10";
        String bodyExpected = "CPF Inválido ou já cadastrado, verifique e tente novamente!";

        clienteRequestDto = new ClienteRequestDto(registeredCpf, "Nome Cliente", empresaUUID);

        when(clienteService.validateCpf(registeredCpf)).thenReturn(true);
        when(clienteService.findByCpf(clienteRequestDto.cpf())).thenReturn(Optional.of(cliente));

        ResultActions response = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto)));

        response
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$").value(bodyExpected));

        verify(clienteService, times(1)).validateCpf(registeredCpf);
        verify(clienteService, times(1)).findByCpf(clienteRequestDto.cpf());

        verifyNoMoreInteractions(clienteService);
        verifyNoInteractions(empresaService);
    }

    @Test
    @DisplayName("Should not create a new cliente, when Id Empresa is invalid")
    void ClienteController_newCliente_returnBadRequestWhenIdEmpresaIsInvalid() throws Exception {

        String cpf = "123.456.789-10";
        String invalidIdEmpresa = "abc";
        String bodyExpected = "Informe um Id de empresa válido";

        clienteRequestDto = new ClienteRequestDto(cpf, "Nome Cliente", invalidIdEmpresa);

        when(clienteService.validateCpf(cpf)).thenReturn(true);
        when(clienteService.findByCpf(clienteRequestDto.cpf())).thenReturn(Optional.empty());
        when(empresaService.findById(invalidIdEmpresa)).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto)));

        response
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$").value(bodyExpected));

        verify(clienteService, times(1)).validateCpf(cpf);
        verify(clienteService, times(1)).findByCpf(clienteRequestDto.cpf());
        verify(empresaService, times(1)).findById(invalidIdEmpresa);

        verifyNoMoreInteractions(clienteService);
        verifyNoMoreInteractions(empresaService);
    }

    @Test
    @DisplayName("Should not create a new cliente, when data is blank")
    void EmpresaController_newEmpresa_returnBadRequestWhenDataIsBlank() throws Exception {

        clienteRequestDto = new ClienteRequestDto("", "Nome empresa", "idEmpresa");
        ClienteRequestDto clienteRequestDto2 = new ClienteRequestDto("cpf", "", "idEmpresa");
        ClienteRequestDto clienteRequestDto3 = new ClienteRequestDto("cpf", "Nome Cliente", "");

        String expectedBodyResponse = "must not be blank";

        ResultActions response = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto)));
        ResultActions response2 = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto2)));

        ResultActions response3 = mockMvc.perform(
                post("/api/clientes/novo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(clienteRequestDto3)));

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));
        response2
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));
        response3
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));

        verifyNoInteractions(clienteService);
        verifyNoInteractions(empresaService);
    }

    @Test
    @DisplayName("Should credit a value in an empresa")
    void ClienteController_credit_returnEmpresa() throws JsonProcessingException, Exception {
        creditRequestDto = new CreditRequestDto(empresaUUID, 100.0);

        double updatedBalance = empresa.getSaldo()
                + (creditRequestDto.value() - (creditRequestDto.value() * empresa.getTaxaServico()));

        Empresa empresaResponse = new Empresa(empresaUUID, empresa.getCnpj(), empresa.getNome(), updatedBalance,
                empresa.getTaxaServico(), empresa.getClientes());

        when(empresaService.findById(creditRequestDto.id_empresa())).thenReturn(Optional.of(empresa));

        when(empresaService.credit(empresa, creditRequestDto.value())).thenReturn(empresaResponse);

        ResultActions response = mockMvc.perform(
                post("/api/clientes/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(updatedBalance));

        verify(empresaService, times(1)).findById(creditRequestDto.id_empresa());
        verify(empresaService, times(1)).credit(empresa, creditRequestDto.value());

        verifyNoMoreInteractions(empresaService);

    }

    @Test
    @DisplayName("Should not credit a value in an empresa when id is invalid")
    void ClienteController_credit_returnBadRequestWhenIdIsInvalid() throws JsonProcessingException, Exception {

        String invalidId = "abc";
        creditRequestDto = new CreditRequestDto(invalidId, 100.0);

        String expectedBodyResponse = "Empresa não encontrada, verifique o ID e tente novamente!";

        when(empresaService.findById(creditRequestDto.id_empresa())).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(
                post("/api/clientes/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));

        verify(empresaService, times(1)).findById(creditRequestDto.id_empresa());

        verifyNoMoreInteractions(empresaService);

    }

    @Test
    @DisplayName("Should not credit a value in an empresa when value is invalid")
    void ClienteController_credit_returnBadRequestWhenvalueIsInvalid() throws JsonProcessingException, Exception {

        double invalidValue = -1;
        creditRequestDto = new CreditRequestDto(empresaUUID, invalidValue);

        String expectedBodyResponse = "must be greater than 0";

        ResultActions response = mockMvc.perform(
                post("/api/clientes/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));

        verifyNoInteractions(empresaService);

    }

    @Test
    @DisplayName("Should debit a value in an empresa")
    void ClienteController_debit_returnEmpresa() throws JsonProcessingException, Exception {
        creditRequestDto = new CreditRequestDto(empresaUUID, 100.0);

        double updatedBalance = empresa.getSaldo() - creditRequestDto.value();

        Empresa empresaResponse = new Empresa(empresaUUID, empresa.getCnpj(), empresa.getNome(), updatedBalance,
                empresa.getTaxaServico(), empresa.getClientes());

        when(empresaService.findById(creditRequestDto.id_empresa())).thenReturn(Optional.of(empresa));

        when(empresaService.debit(empresa, creditRequestDto.value())).thenReturn(empresaResponse);

        ResultActions response = mockMvc.perform(
                post("/api/clientes/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(updatedBalance));

        verify(empresaService, times(1)).findById(creditRequestDto.id_empresa());
        verify(empresaService, times(1)).debit(empresa, creditRequestDto.value());

        verifyNoMoreInteractions(empresaService);

    }

    @Test
    @DisplayName("Should not debit a value in an empresa when id is invalid")
    void ClienteController_debit_returnBadRequestWhenIdIsInvalid() throws JsonProcessingException, Exception {

        String invalidId = "abc";
        creditRequestDto = new CreditRequestDto(invalidId, 100.0);

        String expectedBodyResponse = "Empresa não encontrada, verifique o ID e tente novamente!";

        when(empresaService.findById(creditRequestDto.id_empresa())).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(
                post("/api/clientes/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));

        verify(empresaService, times(1)).findById(creditRequestDto.id_empresa());

        verifyNoMoreInteractions(empresaService);

    }

    @Test
    @DisplayName("Should not debit a value in an empresa when value is invalid")
    void ClienteController_debit_returnBadRequestWhenValueIsInvalid() throws JsonProcessingException, Exception {

        double invalidValue = -1;

        creditRequestDto = new CreditRequestDto(empresaUUID, invalidValue);

        String expectedBodyResponse = "must be greater than 0";

        ResultActions response = mockMvc.perform(
                post("/api/clientes/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(creditRequestDto)));

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(expectedBodyResponse));

        verifyNoInteractions(empresaService);

    }

}
