package teste.tgid_bruno.services;

import lombok.RequiredArgsConstructor;

import org.antlr.v4.runtime.InputMismatchException;
import org.springframework.stereotype.Service;
import teste.tgid_bruno.domain.entities.Empresa;
import teste.tgid_bruno.domain.repositories.EmpresaRepository;
import teste.tgid_bruno.dtos.EmpresaRequestDto;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public Optional<Empresa> findByCnpj(String cnpj) {
        return empresaRepository.findByCnpj(cnpj);
    }

    public boolean validateCnpj(String cnpj) {
        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("[^\\d]", "");

        // Verifica se o CNPJ tem 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }

        // Calcula os dígitos verificadores
        char dig13, dig14;
        int sm, i, r, num, peso;

        try {
            // Cálculo do primeiro dígito verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            // Cálculo do segundo dígito verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            // Verifica se os dígitos calculados são iguais aos dígitos informados
            if ((dig13 == cnpj.charAt(12)) && (dig14 == cnpj.charAt(13))) {
                return true;
            } else {
                return false;
            }
        } catch (InputMismatchException erro) {
            return false;
        }
    }

    public Empresa newEmpresa(EmpresaRequestDto data) {
        var possibleEmpresa = findByCnpj(data.cnpj());

        if (possibleEmpresa.isPresent()) {
            return null;
        }

        Empresa novaEmpresa = new Empresa(data.cnpj(), data.nome());
        novaEmpresa.setSaldo(0.0);
        novaEmpresa.setTaxaServico(0.05f);

        return empresaRepository.save(novaEmpresa);
    }

    public Optional<Empresa> findById(String id) {
        return empresaRepository.findById(id);
    }
}
