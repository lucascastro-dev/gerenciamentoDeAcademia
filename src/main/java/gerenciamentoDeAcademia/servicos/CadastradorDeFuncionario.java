package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeFuncionario implements ICadastradorDeFuncionario {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Override
    public FuncionarioCadastrado cadastrar(Funcionario funcionario) {
        validar(funcionario);

        var funcionarioCadastrado = FuncionarioCadastrado.builder()
                .nome(funcionario.getNome())
                .rg(funcionario.getRg())
                .cpf(funcionario.getCpf())
                .dataDeNascimento(funcionario.getDataDeNascimento())
                .endereco(funcionario.getEndereco())
                .telefone(funcionario.getTelefone())
                .cargo(funcionario.getCargo())
                .especializacao(funcionario.getEspecializacao());

        return funcionarioRepository.save(funcionarioCadastrado.build());
    }

    public void validar(Funcionario funcionario) {
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(funcionario.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getCargo(), "Cargo é obrigatório!");
        if (funcionario.getCargo() != null || funcionario.getCargo().isEmpty())
            ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionario.getEspecializacao(), "Especialização é obrigatório!");
    }
}