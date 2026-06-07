package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeAcesso;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoEscopoProfessor {

    private final TurmaRepository turmaRepository;

    public Turma exigirTurmaDoProfessor(Long turmaId, UsuarioAutenticado usuario) {
        Turma turma = turmaRepository.findById(turmaId).orElse(null);
        if (turma == null) {
            ExcecaoDeAcesso.naoEncontrado("Turma não encontrada.");
        }

        if (usuario != null && usuario.isOperadorPlataforma()) {
            return turma;
        }

        String cpfProfessor = usuario != null ? CpfUtil.somenteDigitos(usuario.getUsername()) : "";
        Funcionario professor = turma.getProfessor();
        String cpfVinculado = professor != null ? CpfUtil.somenteDigitos(professor.getCpf()) : "";

        if (cpfProfessor.isBlank() || !cpfProfessor.equals(cpfVinculado)) {
            ExcecaoDeAcesso.acessoNegado("Turma não pertence ao seu perfil.");
        }
        return turma;
    }
}
