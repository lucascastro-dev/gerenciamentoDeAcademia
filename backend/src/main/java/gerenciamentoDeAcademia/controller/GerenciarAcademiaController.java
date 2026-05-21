package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;
import gerenciamentoDeAcademia.servicos.academia.GerenciadorDeAcademia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@permissaoEvaluator.possuiMaster(authentication)")
    public void registrarNovaAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.cadastrar(academiaDto);
    }

    @DeleteMapping("/desativarAcademia/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:ativar-inativar')")
    public void desativarAcademia(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        gerenciadorDeAcademia.desativarAcademia(cnpjAcademia);
    }

    @PutMapping("/atualizarDadosAcademia")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:gerenciar')")
    public void atualizarDadosAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.atualizarDados(academiaDto);
    }

    @PutMapping("/solicitarPrimeiroAcesso/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public void solicitarPrimeiroAcesso(@PathVariable("cpf") String cpf) {
        gerenciadorDeAcademia.solicitarPrimeiroAcesso(cpf.replaceAll("\\D", ""));
    }

    @PostMapping("/instituicao/{instituicaoId}/ativarFuncionario/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:ativar')")
    public void ativarFuncionarioNaInstituicao(
            @PathVariable Long instituicaoId,
            @PathVariable("cpf") String cpf,
            @RequestBody AtivacaoFuncionarioDto dados) {
        gerenciadorDeAcademia.ativarFuncionarioNaInstituicao(instituicaoId, cpf.replaceAll("\\D", ""), dados);
    }

    @PostMapping("/instituicao/{instituicaoId}/inativarFuncionario/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:ativar')")
    public void inativarFuncionarioNaInstituicao(
            @PathVariable Long instituicaoId,
            @PathVariable("cpf") String cpf) {
        gerenciadorDeAcademia.inativarFuncionarioNaInstituicao(instituicaoId, cpf.replaceAll("\\D", ""));
    }

    @GetMapping("/consultarAcademiaCnpj/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public AcademiaDto consultarAcademiaPorCnpj(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        return gerenciadorDeAcademia.consultarAcademiaCnpj(cnpjAcademia);
    }

    @GetMapping("/consultarAcademiaId/{codAcademia}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public AcademiaDto consultarAcademiaPorId(@PathVariable("codAcademia") Long codAcademia) {
        return gerenciadorDeAcademia.consultarAcademiaId(codAcademia);
    }

    @GetMapping("/consultarTodasAcademias")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public List<AcademiaDto> consultarTodasAcademias() {
        return new ArrayList<AcademiaDto>(gerenciadorDeAcademia.consultarTodasAcademias());
    }
}

