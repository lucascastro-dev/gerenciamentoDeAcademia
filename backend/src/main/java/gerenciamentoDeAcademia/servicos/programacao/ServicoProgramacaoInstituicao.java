package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ConflitoHorarioDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoFormDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.util.IntervaloHorario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoProgramacaoInstituicao {

    private final ItemProgramacaoAlunoRepository repository;
    private final InstituicaoRepository instituicaoRepository;
    private final AlunoRepository alunoRepository;
    private final ServicoGradeHoraria servicoGradeHoraria;

    public List<ItemProgramacaoDto> listarPorInstituicao(Long instituicaoId) {
        return repository.findByInstituicao_IdOrderByDataPrevistaAscIdAsc(instituicaoId).stream()
                .map(ItemProgramacaoDto::of)
                .toList();
    }

    @Transactional
    public ItemProgramacaoDto criar(Long instituicaoId, ItemProgramacaoFormDto form) {
        ItemProgramacaoAluno item = montarItem(instituicaoId, form, null);
        validarConflitos(instituicaoId, item, null);
        return ItemProgramacaoDto.of(repository.save(item));
    }

    @Transactional
    public ItemProgramacaoDto atualizar(Long instituicaoId, Long id, ItemProgramacaoFormDto form) {
        ItemProgramacaoAluno existente = buscarDaInstituicao(instituicaoId, id);
        ItemProgramacaoAluno item = montarItem(instituicaoId, form, existente);
        validarConflitos(instituicaoId, item, id);
        return ItemProgramacaoDto.of(repository.save(item));
    }

    @Transactional
    public void excluir(Long instituicaoId, Long id) {
        ItemProgramacaoAluno item = buscarDaInstituicao(instituicaoId, id);
        repository.delete(item);
    }

    public List<ConflitoHorarioDto> validarFormulario(Long instituicaoId, ItemProgramacaoFormDto form, Long ignorarId) {
        ItemProgramacaoAluno item = montarItem(instituicaoId, form, null);
        if (ignorarId != null) {
            item.setId(ignorarId);
        }
        return servicoGradeHoraria.detectarConflitosItem(instituicaoId, item, ignorarId);
    }

    private void validarConflitos(Long instituicaoId, ItemProgramacaoAluno item, Long ignorarId) {
        List<ConflitoHorarioDto> conflitos = servicoGradeHoraria.detectarConflitosItem(instituicaoId, item, ignorarId);
        ExcecaoDeDominio.quando(!conflitos.isEmpty(),
                "Conflito de horário/sala: " + conflitos.get(0).mensagem());
    }

    private ItemProgramacaoAluno buscarDaInstituicao(Long instituicaoId, Long id) {
        ItemProgramacaoAluno item = repository.findById(id)
                .orElseThrow(() -> new ExcecaoDeDominio("Item de programação não encontrado."));
        ExcecaoDeDominio.quando(
                item.getInstituicao() == null || !instituicaoId.equals(item.getInstituicao().getId()),
                "Item não pertence a esta instituição.");
        return item;
    }

    private ItemProgramacaoAluno montarItem(Long instituicaoId, ItemProgramacaoFormDto form, ItemProgramacaoAluno base) {
        ExcecaoDeDominio.quandoNulo(form, "Dados obrigatórios.");
        ExcecaoDeDominio.quandoNulo(form.getTipo(), "Informe o tipo.");
        ExcecaoDeDominio.quandoNuloOuVazio(form.getTitulo(), "Informe o título.");
        ExcecaoDeDominio.quandoNulo(form.getDataPrevista(), "Informe a data.");
        ExcecaoDeDominio.quandoNuloOuVazio(form.getCpfAluno(), "Informe o CPF do aluno.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
        String cpf = form.getCpfAluno().replaceAll("\\D", "");
        Aluno aluno = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado.");

        IntervaloHorario intervalo = IntervaloHorario.parse(
                form.getHorario() != null ? form.getHorario() : "08:00");

        ItemProgramacaoAluno item = base != null ? base : new ItemProgramacaoAluno();
        item.setInstituicao(instituicao);
        item.setAluno(aluno);
        item.setTipo(form.getTipo());
        item.setTitulo(form.getTitulo().trim());
        item.setDescricao(form.getDescricao());
        item.setDataPrevista(form.getDataPrevista());
        item.setHorario(form.getHorario());
        item.setHoraInicio(intervalo.inicio());
        item.setHoraFim(intervalo.fim());
        item.setSala(normalizarSala(form.getSala()));
        return item;
    }

    private String normalizarSala(String sala) {
        if (sala == null || sala.isBlank()) {
            return null;
        }
        return sala.trim();
    }
}
