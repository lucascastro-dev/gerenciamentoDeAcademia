package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteradorDeFuncionario implements IAlteradorDeFuncionario {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public void alterarFuncionario(FuncionarioDto funcionarioDto) {
        Funcionario funcionario = funcionarioRepository.findByCpf(funcionarioDto.getCpf());
        ExcecaoDeDominio.quandoNulo(funcionario, "Funcionário não encontrado na base de dados!");

        funcionario.atualizar(funcionarioDto);
        funcionarioRepository.save(funcionario);
    }
}
