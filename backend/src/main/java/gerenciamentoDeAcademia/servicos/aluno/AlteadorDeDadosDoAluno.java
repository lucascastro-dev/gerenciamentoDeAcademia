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

@RequiredArgsConstructor
@Component
@Service
public class AlteadorDeDadosDoAluno implements IAlteradorDeDadosDoAluno {
    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public Aluno alterarAluno(AlunoDto alunoDto) {
        Aluno alunoEncontrado = alunoRepository.findByCpf(alunoDto.getCpf());
        ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado!");

        if (alunoEncontrado.getCpf() != alunoDto.getCpf())
            throw new ExcecaoDeDominio("Não é possível alterar o CPF do aluno!");

        alunoEncontrado.setNome(alunoDto.getNome());
        alunoEncontrado.setRg(alunoDto.getRg());
        alunoEncontrado.setDataDeNascimento(alunoDto.getDataDeNascimento());
        alunoEncontrado.setEndereco(alunoDto.getEndereco());
        alunoEncontrado.setTelefone(alunoDto.getTelefone());
        alunoEncontrado.setValorMensalidade(alunoDto.getValorMensalidade());
        alunoEncontrado.setDiaVencimentoMensalidade(alunoDto.getDiaVencimentoMensalidade());
        alunoEncontrado.setNomeResponsavel(alunoDto.getNomeResponsavel());
        alunoEncontrado.setTelefoneResponsavel(alunoDto.getTelefoneResponsavel());

        return alunoRepository.save(alunoEncontrado);
    }
}
