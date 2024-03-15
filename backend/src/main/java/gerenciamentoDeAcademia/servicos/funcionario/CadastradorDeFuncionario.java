package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeFuncionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeFuncionario implements ICadastradorDeFuncionario {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Override
    public Funcionario cadastrar(FuncionarioDto funcionarioDto) {
        validar(funcionarioDto);

        var funcionarioCadastrado = Funcionario.builder()
                .nome(funcionarioDto.getNome())
                .rg(funcionarioDto.getRg())
                .cpf(funcionarioDto.getCpf())
                .dataDeNascimento(funcionarioDto.getDataDeNascimento())
                .endereco(funcionarioDto.getEndereco())
                .telefone(funcionarioDto.getTelefone())
                .cargo(funcionarioDto.getCargo())
                .especializacao(funcionarioDto.getEspecializacao());

        return funcionarioRepository.save(funcionarioCadastrado.build());
    }

    public void validar(FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(funcionarioDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCargo(), "Cargo é obrigatório!");
        if (funcionarioDto.getCargo() != null || funcionarioDto.getCargo().isEmpty())
            ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEspecializacao(), "Especialização é obrigatório!");
    }
}