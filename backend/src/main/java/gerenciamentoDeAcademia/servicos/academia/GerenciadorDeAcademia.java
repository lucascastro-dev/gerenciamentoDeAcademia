package gerenciamentoDeAcademia.servicos.academia;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.entidades.Academia;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AcademiaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IGerenciadorDeAcademia;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GerenciadorDeAcademia implements IGerenciadorDeAcademia {

    private final AcademiaRepository academiaRepository;

    @Override
    public void cadastrar(AcademiaDto academiaDto) {
        academiaRepository.save(new Academia(academiaDto));
    }

    @Override
    public void desativarAcademia(String cnpjAcademia) {
        Academia academiaParaDesativar = academiaRepository.findByCnpj(cnpjAcademia);
        if (!academiaParaDesativar.getCadastroAtivo()) {
            throw new ApplicationException("Essa academia já está desativada!", HttpStatus.BAD_REQUEST);
        }

        academiaParaDesativar.setCadastroAtivo(false);
        academiaRepository.save(academiaParaDesativar);
    }

    @Override
    public void atualizarDados(AcademiaDto academiaDto) {
        Academia academia = academiaRepository.findByCnpj(academiaDto.getCnpj());
        ExcecaoDeDominio.quandoNulo(academia, "Academia não encontrada");

        academiaRepository.save(academia);
    }
}
