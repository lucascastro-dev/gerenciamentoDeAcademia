package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoProgramacaoAluno {

    private final ItemProgramacaoAlunoRepository repository;

    public List<ItemProgramacaoDto> listarPorAlunoEInstituicao(Aluno aluno, Long instituicaoId) {
        if (aluno == null || instituicaoId == null) {
            return List.of();
        }
        return repository.findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(aluno.getCpf(), instituicaoId)
                .stream()
                .map(ItemProgramacaoDto::of)
                .toList();
    }
}
