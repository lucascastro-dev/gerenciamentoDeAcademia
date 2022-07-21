package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeAluno implements ICadastradorDeAluno {

    @Override
    public AlunoCadastrado cadastrar(Aluno aluno) {

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

        return alunoCadastrado;
    }
}