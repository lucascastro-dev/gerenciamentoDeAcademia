package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;

import java.util.List;
import java.util.Optional;

public interface IConsultaDeTurma {
    List<Turma> listarTurmas();
    List<Turma> listarTurmas(UsuarioAutenticado usuario, Long instituicaoId, String professorCpf, List<String> dias);
    Optional<Turma> buscarTurmaPorId(Long id);
    List<Turma> buscarTurmaPorModalidade(String modalidade);
}
