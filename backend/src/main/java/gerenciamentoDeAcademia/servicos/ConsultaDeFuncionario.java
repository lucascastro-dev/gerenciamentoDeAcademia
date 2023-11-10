package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Service
public class ConsultaDeFuncionario implements IConsultaDeFuncionario {
    @Autowired
    private final FuncionarioRepository funcionarioRepository;

    @Override
    public List<FuncionarioDto> listarFuncionarios() {
        return funcionarioRepository.findAll().stream().map(FuncionarioDto::new).collect(Collectors.toList());
    }

    @Override
    public FuncionarioDto consultarFuncionarioPorCpf(String cpf) {
        return new FuncionarioDto(funcionarioRepository.findByCpf(cpf));
    }

    public List<String> listarLogs(Long id) {
        return funcionarioRepository.findRevisions(id)
                .stream().map(Object::toString).collect(Collectors.toList());
    }
}
