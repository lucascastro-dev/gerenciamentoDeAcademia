package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeDadosDoAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteadorDeDadosDoAluno implements IAlteradorDeDadosDoAluno {

    private final AlunoRepository alunoRepository;

    @Override
    public void alterarAluno(AlunoDto alunoDto) {
        Aluno aluno = alunoRepository.findByCpf(alunoDto.getCpf());

        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado!");

        aluno.setNome(alunoDto.getNome());
        aluno.setRg(alunoDto.getRg());
        aluno.setDataDeNascimento(alunoDto.getDataDeNascimento());
        aluno.setEndereco(alunoDto.getEndereco());
        aluno.setTelefone(alunoDto.getTelefone());
        aluno.setValorMensalidade(alunoDto.getValorMensalidade());
        aluno.setDiaVencimentoMensalidade(alunoDto.getDiaVencimentoMensalidade());
        aluno.setNomeResponsavel(alunoDto.getNomeResponsavel());
        aluno.setTelefoneResponsavel(alunoDto.getTelefoneResponsavel());

        alunoRepository.save(aluno);
    }
}
