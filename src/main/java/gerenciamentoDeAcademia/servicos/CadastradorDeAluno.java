package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeAluno implements ICadastradorDeAluno {

    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public AlunoCadastrado cadastrar(Aluno aluno) {
        if (aluno.getNome() == null)
            throw new RuntimeException("Nome é obrigatório!");

        if (aluno.getRg() == null)
            throw new RuntimeException("RG é obrigatório!");

        if (aluno.getCpf() == null)
            throw new RuntimeException("CPF é obrigatório!");

        if (aluno.getDataDeNascimento() == null)
            throw new RuntimeException("Data de nascimento é obrigatória!");

        if (aluno.getEndereco() == null)
            throw new RuntimeException("Endereço é obrigatório!");

        if (aluno.getTelefone() == null)
            throw new RuntimeException("Telefone é obrigatório!");

        if (aluno.getValorMensalidade() == null) {
            throw new RuntimeException("Valor da mensalidade é obrigatório!");
        }

        if (aluno.getDiaVencimentoMensalidade() == null) {
            throw new RuntimeException("Data de vencimento da mensalidade é obrigatório!");
        }

        if ((LocalDate.now().getYear() - aluno.getDataDeNascimento().getYear()) < 18 &&
                aluno.getNomeResponsavel() == null ||
                aluno.getTelefoneResponsavel() == null) {
            throw new RuntimeException("Dados do responsável são obrigatório!");
        }

        var alunoCadastrado = new AlunoCadastrado();
        alunoCadastrado.setNome(aluno.getNome());
        alunoCadastrado.setRg(aluno.getRg());
        alunoCadastrado.setCpf(aluno.getCpf());
        alunoCadastrado.setDataDeNascimento(aluno.getDataDeNascimento());
        alunoCadastrado.setEndereco(aluno.getEndereco());
        alunoCadastrado.setTelefone(aluno.getTelefone());
        alunoCadastrado.setValorMensalidade(aluno.getValorMensalidade());
        alunoCadastrado.setDiaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade());
        alunoCadastrado.setNomeResponsavel(aluno.getNomeResponsavel());
        alunoCadastrado.setTelefoneResponsavel(aluno.getTelefoneResponsavel());

        return alunoRepository.save(alunoCadastrado);
    }
}