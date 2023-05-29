package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Component
@Service
public class ConsultaDeFuncionario implements IConsultaDeFuncionario {
    private final FuncionarioRepository funcionarioRepository;

    @Override
    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }
}
