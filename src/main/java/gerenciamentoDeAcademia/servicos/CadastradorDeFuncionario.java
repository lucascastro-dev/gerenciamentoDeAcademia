package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeFuncionario implements ICadastradorDeFuncionario {

    @Override
    public FuncionarioCadastrado cadastrar(Funcionario funcionario) {
        var funcionarioCadastrado = new FuncionarioCadastrado();
        funcionarioCadastrado.setNome(funcionario.getNome());
        funcionarioCadastrado.setRg(funcionario.getRg());
        funcionarioCadastrado.setCpf(funcionario.getCpf());
        funcionarioCadastrado.setDataDeNascimento(funcionario.getDataDeNascimento());
        funcionarioCadastrado.setEndereco(funcionario.getEndereco());
        funcionarioCadastrado.setTelefone(funcionario.getTelefone());
        funcionarioCadastrado.setCargo(funcionario.getCargo());
        funcionarioCadastrado.setEspecializacao(funcionario.getEspecializacao());

        return funcionarioCadastrado;
    }
}