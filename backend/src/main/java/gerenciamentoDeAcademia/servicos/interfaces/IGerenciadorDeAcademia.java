package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AcademiaDto;

import java.util.List;

public interface IGerenciadorDeAcademia {
    void cadastrar(AcademiaDto academiaDto);

    void desativarAcademia(String cnpjAcademia);

    void atualizarDados(AcademiaDto academiaDto);

    AcademiaDto consultarAcademiaCnpj(String cnpjAcademia);

    List<AcademiaDto> consultarTodasAcademias();

    void solicitarPrimeiroAcesso(String cpf, String cnpj);

    void ativarFuncionario(String cpf, String cnpj);

    void inativarFuncionario(String cpf, String cnpj);

    boolean verificarVinculo(String cpf, String vinculo);
}
