package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeTurma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Service
public class ConsultaDeTurma implements IConsultaDeTurma {
    private final TurmaRepository turmaRepository;

    @Override
    public List<Turma> listarTurmas() {
        return turmaRepository.findAll();
    }

    @Override
    public Optional<Turma> buscarTurmaPorId(Long id) {
        if (id == null)
            ExcecaoDeDominio.quandoValorIgualAZero(id, "ID obrigatório para consulta da turma!");

        return turmaRepository.findById(id);
    }

    @Override
    public List<Turma> buscarTurmaPorModalidade(String modalidade) {
        if (modalidade.isEmpty() || modalidade == null)
            ExcecaoDeDominio.quandoNuloOuVazio(modalidade, "Para consultar é obrigatório informar a modalidade!");

        return turmaRepository.findByModalidade(modalidade);
    }
}
