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

        if (funcionario.getNome() == null)
            throw new RuntimeException("Nome é obrigatório!");

        if (funcionario.getRg() == null)
            throw new RuntimeException("RG é obrigatório!");

        if (funcionario.getCpf() == null)
            throw new RuntimeException("CPF é obrigatório!");

        if (funcionario.getDataDeNascimento() == null)
            throw new RuntimeException("Data de nascimento é obrigatória!");

        if (funcionario.getEndereco() == null)
            throw new RuntimeException("Endereço é obrigatório!");

        if (funcionario.getTelefone() == null)
            throw new RuntimeException("Telefone é obrigatório!");

        if (funcionario.getCargo() == null)
            throw new RuntimeException("Cargo é obrigatório!");

        if (funcionario.getCargo() == "Professor")
            if (funcionario.getEspecializacao() == null)
                throw new RuntimeException("Especialização é obrigatória para professor!");

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