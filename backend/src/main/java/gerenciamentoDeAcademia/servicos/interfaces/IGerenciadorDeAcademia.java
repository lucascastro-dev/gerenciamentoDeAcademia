package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.dto.AtivacaoFuncionarioDto;

import java.util.List;

public interface IGerenciadorDeAcademia {
    void cadastrar(AcademiaDto academiaDto);

    void desativarAcademia(String cnpjAcademia);

    void atualizarDados(AcademiaDto academiaDto);

    AcademiaDto consultarAcademiaCnpj(String cnpjAcademia);

    List<AcademiaDto> consultarTodasAcademias();

    void solicitarPrimeiroAcesso(String cpf);

    void ativarFuncionario(String cpf, String cnpj);

    void ativarFuncionarioNaInstituicao(Long instituicaoId, String cpf, AtivacaoFuncionarioDto dados);

    void inativarFuncionario(String cpf, String cnpj);

    void inativarFuncionarioNaInstituicao(Long instituicaoId, String cpf);

    boolean verificarVinculo(String cpf, String vinculo);

    AcademiaDto consultarAcademiaId(Long codAcademia);
}
