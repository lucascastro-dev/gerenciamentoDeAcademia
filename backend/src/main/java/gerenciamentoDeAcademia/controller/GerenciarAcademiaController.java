package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.servicos.academia.GerenciadorDeAcademia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("academia")
public class GerenciarAcademiaController {

    @Autowired
    private GerenciadorDeAcademia gerenciadorDeAcademia;

    @PostMapping("/registrarAcademia")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarNovaAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.cadastrar(academiaDto);
    }

    @DeleteMapping("/desativarAcademia/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativarAcademia(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        gerenciadorDeAcademia.desativarAcademia(cnpjAcademia);
    }

    @PutMapping("/atualizarDadosAcademia")
    @ResponseStatus(HttpStatus.OK)
    public void atualizarDadosAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.atualizarDados(academiaDto);
    }

    @PutMapping("/solicitarPrimeiroAcesso/{cpf}/{cnpj}")
    @ResponseStatus(HttpStatus.OK)
    public void solicitarPrimeiroAcesso(@PathVariable("cpf") String cpf,
                                        @PathVariable("cnpj") String cnpj) {
        gerenciadorDeAcademia.solicitarPrimeiroAcesso(cpf, cnpj);
    }

    @PostMapping("/ativarFuncionario/{cpf}/{cnpj}")
    @ResponseStatus(HttpStatus.OK)
    public void ativarFuncionario(@PathVariable("cpf") String cpf,
                                  @PathVariable("cnpj") String cnpj) {
        gerenciadorDeAcademia.ativarFuncionario(cpf, cnpj);
    }

    @PostMapping("/inativarFuncionario/{cpf}/{cnpj}")
    @ResponseStatus(HttpStatus.OK)
    public void inativarFuncionario(@PathVariable("cpf") String cpf,
                                    @PathVariable("cnpj") String cnpj) {
        gerenciadorDeAcademia.inativarFuncionario(cpf, cnpj);
    }

    @GetMapping("/consultarAcademiaCnpj/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.OK)
    public AcademiaDto consultarAcademiaPorCnpj(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        return gerenciadorDeAcademia.consultarAcademiaCnpj(cnpjAcademia);
    }

    @GetMapping("/consultarAcademiaId/{codAcademia}")
    @ResponseStatus(HttpStatus.OK)
    public AcademiaDto consultarAcademiaPorId(@PathVariable("codAcademia") Long codAcademia) {
        return gerenciadorDeAcademia.consultarAcademiaId(codAcademia);
    }

    @GetMapping("/consultarTodasAcademias")
    @ResponseStatus(HttpStatus.OK)
    public List<AcademiaDto> consultarTodasAcademias() {
        return new ArrayList<AcademiaDto>(gerenciadorDeAcademia.consultarTodasAcademias());
    }
}

