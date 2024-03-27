package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExluirTurma implements IExcluirTurma {
    private TurmaRepository turmaRepository;

    public void excluir(Long idTurma) {
        ExcecaoDeDominio.quandoNulo(idTurma, "ID obrigatório para excluir uma turma");

        Optional<Turma> turmaParaExcluir = turmaRepository.findById(idTurma);
        ExcecaoDeDominio.quandoNulo(turmaParaExcluir, "Turma não encontrada na base");

        turmaRepository.delete(turmaParaExcluir.get());
    }
}

