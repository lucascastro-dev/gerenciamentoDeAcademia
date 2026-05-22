package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;
import gerenciamentoDeAcademia.dto.InstituicaoDto;
import gerenciamentoDeAcademia.servicos.instituicao.GerenciadorDeInstituicao;
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
@RequestMapping({"/instituicao", "/academia"})
public class GerenciarInstituicaoController {

    @Autowired
    private GerenciadorDeInstituicao gerenciadorDeInstituicao;

    @PostMapping("/registrarAcademia")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possuiMaster(authentication)")
    public void registrarNovaAcademia(@RequestBody InstituicaoDto instituicaoDto) {
        gerenciadorDeInstituicao.cadastrar(instituicaoDto);
    }

    @DeleteMapping("/desativarAcademia/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:ativar-inativar')")
    public void desativarAcademia(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        gerenciadorDeInstituicao.desativarInstituicao(cnpjAcademia);
    }

    @PutMapping("/atualizarDadosAcademia")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:gerenciar')")
    public void atualizarDadosAcademia(@RequestBody InstituicaoDto instituicaoDto) {
        gerenciadorDeInstituicao.atualizarDados(instituicaoDto);
    }

    @PutMapping("/solicitarPrimeiroAcesso/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public void solicitarPrimeiroAcesso(@PathVariable("cpf") String cpf) {
        gerenciadorDeInstituicao.solicitarPrimeiroAcesso(cpf.replaceAll("\\D", ""));
    }

    @PostMapping("/instituicao/{instituicaoId}/ativarFuncionario/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:ativar')")
    public void ativarFuncionarioNaInstituicao(
            @PathVariable Long instituicaoId,
            @PathVariable("cpf") String cpf,
            @RequestBody AtivacaoFuncionarioDto dados) {
        gerenciadorDeInstituicao.ativarFuncionarioNaInstituicao(instituicaoId, cpf.replaceAll("\\D", ""), dados);
    }

    @PostMapping("/instituicao/{instituicaoId}/inativarFuncionario/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:ativar')")
    public void inativarFuncionarioNaInstituicao(
            @PathVariable Long instituicaoId,
            @PathVariable("cpf") String cpf) {
        gerenciadorDeInstituicao.inativarFuncionarioNaInstituicao(instituicaoId, cpf.replaceAll("\\D", ""));
    }

    @GetMapping("/consultarAcademiaCnpj/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public InstituicaoDto consultarAcademiaPorCnpj(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        return gerenciadorDeInstituicao.consultarInstituicaoCnpj(cnpjAcademia);
    }

    @GetMapping("/consultarAcademiaId/{codAcademia}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public InstituicaoDto consultarAcademiaPorId(@PathVariable("codAcademia") Long codAcademia) {
        return gerenciadorDeInstituicao.consultarInstituicaoId(codAcademia);
    }

    @GetMapping("/consultarTodasAcademias")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'academia:consultar')")
    public List<InstituicaoDto> consultarTodasAcademias() {
        return new ArrayList<>(gerenciadorDeInstituicao.consultarTodasInstituicoes());
    }
}
