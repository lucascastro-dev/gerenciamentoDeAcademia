package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeTurma implements IConsultaDeTurma {
    private final TurmaRepository turmaRepository;

    @Override
    public List<Turma> listarTurmas() {
        return turmaRepository.findAll();
    }

    @Override
    public Optional<Turma> buscarTurmaPorId(Long id) {
        ExcecaoDeDominio.quandoNulo(id, "ID obrigatório para consulta da turma");

        return turmaRepository.findById(id);
    }

    @Override
    public List<Turma> buscarTurmaPorModalidade(String modalidade) {
        ExcecaoDeDominio.quandoNuloOuVazio(modalidade, "Modalidade obrigatória para consulta da turma");

        return turmaRepository.findByModalidade(modalidade);
    }
}
