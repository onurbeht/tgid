package teste.tgid_bruno.controllers;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import teste.tgid_bruno.domain.entities.Cliente;
import teste.tgid_bruno.dtos.ClienteRequestDto;
import teste.tgid_bruno.dtos.CreditRequestDto;
import teste.tgid_bruno.services.ClienteService;
import teste.tgid_bruno.services.EmpresaService;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final EmpresaService empresaService;

    // GET
    @GetMapping("/{id}")
    public ResponseEntity<?> getCliente(@PathVariable("id") String id) {
        var cliente = clienteService.findById(id);

        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        }

        return ResponseEntity.notFound().build();
    }

    // POST
    @PostMapping("/novo")
    public ResponseEntity<?> newCliente(@RequestBody @Valid ClienteRequestDto data) throws URISyntaxException {
        boolean validateCpf = clienteService.validateCpf(data.cpf());

        if (!validateCpf) {
            return ResponseEntity.badRequest().body("Informe um CPF válido!");
        }

        var possibleCliente = clienteService.findByCpf(data.cpf());

        if (possibleCliente.isPresent()) {
            return ResponseEntity.badRequest().body("CPF Inválido ou já cadastrado, verifique e tente novamente!");
        }

        var possibleEmpresa = empresaService.findById(data.id_empresa());

        if (possibleEmpresa.isEmpty()) {
            return ResponseEntity.badRequest().body("Informe um Id de empresa válido");
        }

        Cliente newCliente = clienteService.newCliente(data, possibleEmpresa.get());

        return ResponseEntity.created(new URI("/api/clientes/" + newCliente.getId())).body(newCliente);

    }

    @PostMapping("/deposito")
    public ResponseEntity<?> credit(@RequestBody @Valid CreditRequestDto data) {
        var possibleEmpresa = empresaService.findById(data.id_empresa());

        if (possibleEmpresa.isEmpty()) {
            return ResponseEntity.badRequest().body("Empresa não encontrada, verifique o ID e tente novamente!");
        }

        return ResponseEntity.ok(empresaService.credit(possibleEmpresa.get(), data.value()));
    }

    @PostMapping("/saque")
    public ResponseEntity<?> debit(@RequestBody @Valid CreditRequestDto data) {
        var possibleEmpresa = empresaService.findById(data.id_empresa());

        if (possibleEmpresa.isEmpty()) {
            return ResponseEntity.badRequest().body("Empresa não encontrada, verifique o ID e tente novamente!");
        }

        return ResponseEntity.ok(empresaService.debit(possibleEmpresa.get(), data.value()));
    }
}
