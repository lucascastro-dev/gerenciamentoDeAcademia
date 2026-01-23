package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CadastradorDeFuncionario implements ICadastradorDeFuncionario {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public void cadastrar(FuncionarioDto funcionarioDto) {
        if (funcionarioRepository.findByCpf(funcionarioDto.getCpf()) != null) {
            throw new ApplicationException("Funcionário já cadastrado!", HttpStatus.BAD_REQUEST);
        }

        funcionarioRepository.save(new Funcionario(funcionarioDto));
    }
}