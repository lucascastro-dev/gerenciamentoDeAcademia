package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    @Override
    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    @Override
    public Funcionario consultarFuncionarioPorCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF obrigatório para consultar funcionário!");

        return funcionarioRepository.findByCpf(cpf);
    }

    public List<String> listarLogs(Long id) {
        return funcionarioRepository.findRevisions(id)
                .stream().map(Object::toString).collect(Collectors.toList());
    }
}
