package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeAlunos;
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
public class ConsultaDeAlunos implements IConsultaDeAlunos {

    private final AlunoRepository alunoRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<AlunoDto> listarAlunos() {
        List<Aluno> listaDeAlunos = alunoRepository.findAll();

        return modelMapper.map(listaDeAlunos, new TypeToken<List<AlunoDto>>() {}.getType());
    }

    @Override
    public Aluno consultaAlunoPorCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF obrigatório para consulta do aluno!");

        Aluno alunoEncontrado = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado na base!");

        return alunoEncontrado;
    }
}
