package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
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
    public FuncionarioCadastrado cadastrar(FuncionarioDto funcionarioDto) {
        validar(funcionarioDto);

        var funcionarioCadastrado = FuncionarioCadastrado.builder()
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
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(funcionarioDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getCargo(), "Cargo é obrigatório!");
        if (funcionarioDto.getCargo() != null || funcionarioDto.getCargo().isEmpty())
            ExcecaoDeDominio.quandoTextoVazioOuNulo(funcionarioDto.getEspecializacao(), "Especialização é obrigatório!");
    }
}