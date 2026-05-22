package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;
import gerenciamentoDeAcademia.dto.InstituicaoDto;

import java.util.List;

public interface IGerenciadorDeInstituicao {
    void cadastrar(InstituicaoDto instituicaoDto);

    void desativarInstituicao(String cnpjInstituicao);

    void atualizarDados(InstituicaoDto instituicaoDto);

    InstituicaoDto consultarInstituicaoCnpj(String cnpjInstituicao);

    List<InstituicaoDto> consultarTodasInstituicoes();

    void solicitarPrimeiroAcesso(String cpf);

    void ativarFuncionario(String cpf, String cnpj);

    void ativarFuncionarioNaInstituicao(Long instituicaoId, String cpf, AtivacaoFuncionarioDto dados);

    void inativarFuncionario(String cpf, String cnpj);

    void inativarFuncionarioNaInstituicao(Long instituicaoId, String cpf);

    boolean verificarVinculo(String cpf, String vinculo);

    InstituicaoDto consultarInstituicaoId(Long codInstituicao);
}
