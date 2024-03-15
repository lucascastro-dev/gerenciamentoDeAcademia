package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeAlunos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Service
public class ConsultaDeAlunos implements IConsultaDeAlunos {
    private final AlunoRepository alunoRepository;

    @Override
    public List<AlunoDto> listarAlunos() {
        return alunoRepository.findAll().stream().map(AlunoDto::new).collect(Collectors.toList());
    }

    @Override
    public AlunoDto consultaAlunoPorCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF obrigatório para consulta do aluno!");

        Aluno alunoEncontrado = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado na base!");

        return new AlunoDto(alunoEncontrado);
    }
}
