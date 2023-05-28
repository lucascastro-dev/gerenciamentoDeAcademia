package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeAlunos;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Component
@Service
public class ConsultaDeAlunos implements IConsultaDeAlunos {
    private final AlunoRepository alunoRepository;

    @Override
    public List<Aluno> listarAlunos() {
        return alunoRepository.findAll();
    }

    @Override
    public Aluno consultaAlunoPorCpf(String cpf) {
        if (cpf.isEmpty() || cpf == null)
            ExcecaoDeDominio.quandoTextoVazioOuNulo(cpf, "CPF obrigatório para consulta do aluno!");

        return alunoRepository.findByCpf(cpf);
    }
}
