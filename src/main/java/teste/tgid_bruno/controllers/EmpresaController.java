package teste.tgid_bruno.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.dtos.EmpresaRequestDto;
import teste.tgid_bruno.services.EmpresaService;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // GET
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmpresa(@PathVariable("id") String id) {
        var possibleEmpresa = empresaService.findById(id);

        if (possibleEmpresa.isPresent()) {
            Empresa empresa = possibleEmpresa.get();
            System.out.println(empresa);
            return ResponseEntity.ok(empresa);
        }

        return ResponseEntity.notFound().build();
    }

    // POST
    @PostMapping("/nova")
    public ResponseEntity<?> newEmpresa(@RequestBody @Valid EmpresaRequestDto data) throws URISyntaxException {
        boolean cnpj = empresaService.validateCnpj(data.cnpj());

        if (!cnpj) {
            return ResponseEntity.badRequest().body("O CNPJ, deve ser um valor valido!");
        }

        Empresa novaEmpresa = empresaService.newEmpresa(data);

        if (novaEmpresa != null) {
            return ResponseEntity.created(new URI("/api/empresas/" + novaEmpresa.getId())).body(novaEmpresa);
        }

        return ResponseEntity.badRequest().body("CNPJ já cadastrado!");

    }
}
