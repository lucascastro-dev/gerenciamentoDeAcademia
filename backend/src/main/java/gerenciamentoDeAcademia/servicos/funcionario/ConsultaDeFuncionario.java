package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeFuncionario implements IConsultaDeFuncionario {
    private final FuncionarioRepository funcionarioRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<FuncionarioDto> listarFuncionarios() {
        List<Funcionario> listaDeFuncionario = funcionarioRepository.findAll();

        return modelMapper.map(listaDeFuncionario, new TypeToken<List<FuncionarioDto>>() {}.getType());
    }

    @Override
    public Funcionario consultarFuncionarioPorCpf(String cpf) {
        return funcionarioRepository.findByCpf(cpf);
    }

    public List<String> listarLogs(Long id) {
        return funcionarioRepository.findRevisions(id)
                .stream().map(Object::toString).collect(Collectors.toList());
    }
}
