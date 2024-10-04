package teste.tgid_bruno.controllers;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import teste.tgid_bruno.domain.entities.Cliente;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.dtos.EmpresaRequestDto;
import teste.tgid_bruno.services.EmpresaService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(controllers = EmpresaController.class)
@ExtendWith(MockitoExtension.class)
public class EmpresaControllerTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper mapper;

        @MockBean
        EmpresaService empresaService;

        private EmpresaRequestDto empresaRequestDto;
        private Empresa empresa;
        private Empresa empresaTestGet;
        List<Cliente> clientes;
        String uuidGenerated;

        @BeforeEach
        void setUp() {
                clientes = List.of(
                                new Cliente("12345678910", "Cliente 1", empresaTestGet),
                                new Cliente("12345678911", "Cliente 2", empresaTestGet));

                uuidGenerated = UUID.randomUUID().toString();

                empresaTestGet = new Empresa(uuidGenerated, "123456789101112", "Empresa Teste", 0.0, 0.05f, clientes);

                empresa = new Empresa("12.345.678/0001-95", "Nome empresa");
                empresa.setId(uuidGenerated);
                empresa.setSaldo(0.0);
                empresa.setTaxaServico(0.05f);

        }

        @Test
        @DisplayName("Should return an empresa by id, when id exist")
        void EmpresaController_getEmpresa_returnEmpresa() throws Exception {
                when(empresaService.findById(uuidGenerated)).thenReturn(Optional.of(empresaTestGet));

                ResultActions response = mockMvc.perform(
                                get("/api/empresas/{id}", uuidGenerated)
                                                .contentType(MediaType.APPLICATION_JSON));

                response
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(uuidGenerated))
                                .andExpect(jsonPath("$.clientes").isArray())
                                .andExpect(jsonPath("$.clientes.length()").value(clientes.size()))
                                .andExpect(jsonPath("$.clientes[0]").value(
                                                allOf(
                                                                hasEntry("cpf", clientes.get(0).getCpf()),
                                                                hasEntry("nome", clientes.get(0).getNome()))))
                                .andExpect(jsonPath("$.clientes[1]").value(
                                                allOf(
                                                                hasEntry("cpf", clientes.get(1).getCpf()),
                                                                hasEntry("nome", clientes.get(1).getNome()))));

                verify(empresaService, times(1)).findById(uuidGenerated);
                verifyNoMoreInteractions(empresaService);
        }

        @Test
        @DisplayName("Should not return an empresa by id, when id does not exist")
        void EmpresaController_getEmpresa_returnNotFound() throws Exception {
                when(empresaService.findById(anyString())).thenReturn(Optional.empty());

                ResultActions response = mockMvc.perform(
                                get("/api/empresas/{id}", "abc")
                                                .contentType(MediaType.APPLICATION_JSON));

                response
                                .andExpect(MockMvcResultMatchers.status().isNotFound());

                verify(empresaService, times(1)).findById(anyString());
                verifyNoMoreInteractions(empresaService);
        }

        @Test
        @DisplayName("Should not execute request, when id is empty in path variable")
        void EmpresaController_getEmpresa_shouldNotExecute() throws Exception {
                ResultActions response = mockMvc.perform(
                                get("/api/empresas/")
                                                .contentType(MediaType.APPLICATION_JSON));

                response.andExpect(MockMvcResultMatchers.status().isNotFound());

        }

        @Test
        @DisplayName("Should create a new empresa, when data is valid")
        void EmpresaController_newEmpresa_returnEmpresa() throws Exception {

                String validCnpj = "12.345.678/0001-95";

                empresaRequestDto = new EmpresaRequestDto(validCnpj, "Nome empresa");

                when(empresaService.validateCnpj(validCnpj)).thenReturn(true);

                when(empresaService.newEmpresa(empresaRequestDto)).thenReturn(empresa);

                ResultActions response = mockMvc.perform(
                                post("/api/empresas/nova")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(mapper.writeValueAsString(empresaRequestDto)));

                response
                                .andExpect(MockMvcResultMatchers.status().isCreated())
                                .andExpect(redirectedUrl("/api/empresas/" + uuidGenerated))
                                .andExpect(jsonPath("$.id").value(uuidGenerated))
                                .andExpect(jsonPath("$.cnpj").value(validCnpj))
                                .andExpect(jsonPath("$.nome").value("Nome empresa"))
                                .andExpect(jsonPath("$.saldo").value(0.0))
                                .andExpect(jsonPath("$.taxaServico").value(0.05f))
                                .andExpect(jsonPath("$.clientes").isArray())
                                .andExpect(jsonPath("$.clientes.length()").value(empresa.getClientes().size()));

                verify(empresaService, times(1)).validateCnpj(validCnpj);
                verify(empresaService, times(1)).newEmpresa(empresaRequestDto);

                verifyNoMoreInteractions(empresaService);
        }

        @Test
        @DisplayName("Should not create a new empresa, when cnpj is invalid")
        void EmpresaController_newEmpresa_returnBadRequestWhenCnpjIsInvalid() throws Exception {

                String invalidCnpj = "12.345.678/91011";
                String bodyExpected = "O CNPJ, deve ser um valor valido!";

                empresaRequestDto = new EmpresaRequestDto(invalidCnpj, "Nome empresa");

                when(empresaService.validateCnpj(invalidCnpj)).thenReturn(false);

                ResultActions response = mockMvc.perform(
                                post("/api/empresas/nova")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(mapper.writeValueAsString(empresaRequestDto)));

                response
                                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(jsonPath("$").value(bodyExpected));

                verify(empresaService, times(1)).validateCnpj(invalidCnpj);

                verifyNoMoreInteractions(empresaService);
        }

        @Test
        @DisplayName("Should not create a new empresa, when cnpj already exist")
        void EmpresaController_newEmpresa_returnBadRequestWhenCnpjExist() throws Exception {

                String invalidCnpj = "12.345.678/91011";
                String bodyExpected = "CNPJ já cadastrado!";

                empresaRequestDto = new EmpresaRequestDto(invalidCnpj, "Nome empresa");

                when(empresaService.validateCnpj(invalidCnpj)).thenReturn(true);

                when(empresaService.newEmpresa(empresaRequestDto)).thenReturn(null);

                ResultActions response = mockMvc.perform(
                                post("/api/empresas/nova")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(mapper.writeValueAsString(empresaRequestDto)));

                response
                                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(jsonPath("$").value(bodyExpected));

                verify(empresaService, times(1)).validateCnpj(invalidCnpj);
                verify(empresaService, times(1)).newEmpresa(empresaRequestDto);

                verifyNoMoreInteractions(empresaService);
        }

        @Test
        @DisplayName("Should not create a new empresa, when data is blank")
        void EmpresaController_newEmpresa_returnBadRequestWhenDataIsBlank() throws Exception {

                EmpresaRequestDto empresaRequestDto = new EmpresaRequestDto("", "Nome empresa");
                EmpresaRequestDto empresaRequestDto2 = new EmpresaRequestDto("123456789101112", "");

                String expectedBodyResponse = "must not be blank";

                ResultActions response = mockMvc.perform(
                                post("/api/empresas/nova")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(mapper.writeValueAsString(empresaRequestDto)));
                ResultActions response2 = mockMvc.perform(
                                post("/api/empresas/nova")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(mapper.writeValueAsString(empresaRequestDto2)));

                response
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$").value(expectedBodyResponse));
                response2
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$").value(expectedBodyResponse));

                verifyNoInteractions(empresaService);
        }

}