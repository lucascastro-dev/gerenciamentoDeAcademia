package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.SalaDto;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Sala;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoSala {

    private final SalaRepository salaRepository;
    private final InstituicaoRepository instituicaoRepository;

    public List<SalaDto> listar(Long instituicaoId) {
        return salaRepository.findByInstituicao_IdOrderByNomeAsc(instituicaoId).stream()
                .map(SalaDto::of)
                .toList();
    }

    @Transactional
    public SalaDto criar(Long instituicaoId, SalaDto dto) {
        ExcecaoDeDominio.quandoNuloOuVazio(dto.getNome(), "Nome da sala é obrigatório.");
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
        Sala sala = Sala.builder()
                .instituicao(instituicao)
                .nome(dto.getNome().trim())
                .capacidade(dto.getCapacidade())
                .ativa(dto.getAtiva() != null ? dto.getAtiva() : true)
                .build();
        return SalaDto.of(salaRepository.save(sala));
    }

    @Transactional
    public void excluir(Long instituicaoId, Long salaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ExcecaoDeDominio("Sala não encontrada."));
        ExcecaoDeDominio.quando(
                sala.getInstituicao() == null || !instituicaoId.equals(sala.getInstituicao().getId()),
                "Sala não pertence a esta instituição.");
        salaRepository.delete(sala);
    }
}
