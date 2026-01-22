package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AcademiaDto;

public interface IGerenciadorDeAcademia {
    void cadastrar(AcademiaDto academiaDto);

    void desativarAcademia(String cnpjAcademia);

    void atualizarDados(AcademiaDto academiaDto);
}
