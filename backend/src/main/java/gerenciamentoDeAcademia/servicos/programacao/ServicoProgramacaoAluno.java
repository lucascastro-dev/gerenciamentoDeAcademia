package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServicoProgramacaoAluno {

    private final ItemProgramacaoAlunoRepository repository;

    @Transactional(readOnly = true)
    public List<ItemProgramacaoDto> listarPorAlunoEInstituicao(Aluno aluno, Long instituicaoId) {
        if (aluno == null || instituicaoId == null) {
            return List.of();
        }
        String cpf = CpfUtil.somenteDigitos(aluno.getCpf());
        Map<Long, ItemProgramacaoAluno> itens = new LinkedHashMap<>();

        repository.findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(cpf, instituicaoId)
                .forEach(item -> itens.put(item.getId(), item));
        repository.findProgramacaoDasTurmasDoAluno(cpf, instituicaoId)
                .forEach(item -> itens.putIfAbsent(item.getId(), item));

        return itens.values().stream()
                .sorted(Comparator
                        .comparing(ItemProgramacaoAluno::getDataPrevista, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ItemProgramacaoAluno::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ItemProgramacaoDto::of)
                .toList();
    }
}
