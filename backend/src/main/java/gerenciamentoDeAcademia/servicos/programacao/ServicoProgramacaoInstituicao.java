package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ConflitoHorarioDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoFormDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.enums.EscopoLancamentoProgramacao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.util.IntervaloHorario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoProgramacaoInstituicao {

    private final ItemProgramacaoAlunoRepository repository;
    private final InstituicaoRepository instituicaoRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final ServicoGradeHoraria servicoGradeHoraria;
    private final ServicoSala servicoSala;

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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ConflitoHorarioDto> validarFormulario(Long instituicaoId, ItemProgramacaoFormDto form, Long ignorarId) {
        ItemProgramacaoAluno item = montarItem(instituicaoId, form, null);
        if (ignorarId != null) {
            item.setId(ignorarId);
        }
        return servicoGradeHoraria.detectarConflitosItem(instituicaoId, item, ignorarId);
    }

    private void validarConflitos(Long instituicaoId, ItemProgramacaoAluno item, Long ignorarId) {
        List<ConflitoHorarioDto> conflitos = servicoGradeHoraria.detectarConflitosItem(instituicaoId, item, ignorarId);
        if (!conflitos.isEmpty()) {
            throw new ExcecaoDeDominio("Conflito de horário/sala: " + conflitos.get(0).mensagem());
        }
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
        ExcecaoDeDominio.quandoNulo(form.getDataPrevista(), "Informe a data de início.");

        EscopoLancamentoProgramacao escopo = form.getEscopoLancamento() != null
                ? form.getEscopoLancamento()
                : EscopoLancamentoProgramacao.ALUNO;

        if (form.getDataFim() != null && form.getDataFim().isBefore(form.getDataPrevista())) {
            throw new ExcecaoDeDominio("A data fim não pode ser anterior à data de início.");
        }

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        Aluno aluno = null;
        Turma turma = null;
        if (escopo == EscopoLancamentoProgramacao.TURMA) {
            ExcecaoDeDominio.quandoNulo(form.getTurmaId(), "Informe a turma.");
            turma = turmaRepository.findById(form.getTurmaId())
                    .orElseThrow(() -> new ExcecaoDeDominio("Turma não encontrada."));
            ExcecaoDeDominio.quando(
                    turma.getInstituicao() == null || !instituicaoId.equals(turma.getInstituicao().getId()),
                    "Turma não pertence a esta instituição.");
            if (turma.getAlunos() != null) {
                turma.getAlunos().size();
            }
        } else {
            ExcecaoDeDominio.quandoNuloOuVazio(form.getCpfAluno(), "Informe o CPF do aluno.");
            String cpf = form.getCpfAluno().replaceAll("\\D", "");
            aluno = alunoRepository.findByCpf(cpf);
            ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado.");
        }

        IntervaloHorario intervalo = resolverIntervalo(form);
        ExcecaoDeDominio.quandoNulo(intervalo, "Informe o horário de início e término.");

        String sala = normalizarSala(instituicaoId, form.getSala());
        ExcecaoDeDominio.quandoNuloOuVazio(sala, "Selecione uma sala cadastrada na instituição.");

        ItemProgramacaoAluno item = base != null ? base : new ItemProgramacaoAluno();
        item.setInstituicao(instituicao);
        if (escopo == EscopoLancamentoProgramacao.TURMA) {
            item.setTurma(turma);
            item.setAluno(null);
        } else {
            item.setAluno(aluno);
            item.setTurma(null);
        }
        item.setTipo(form.getTipo());
        item.setTitulo(form.getTitulo().trim());
        item.setDescricao(form.getDescricao());
        item.setDataPrevista(form.getDataPrevista());
        item.setDataFim(form.getDataFim());
        item.setHorario(intervalo.inicio().toString().substring(0, 5) + "-" + intervalo.fim().toString().substring(0, 5));
        item.setHoraInicio(intervalo.inicio());
        item.setHoraFim(intervalo.fim());
        item.setSala(sala);
        ExcecaoDeDominio.quando(item.getAluno() == null && item.getTurma() == null,
                "Informe o aluno ou a turma para o item de programação.");
        return item;
    }

    private IntervaloHorario resolverIntervalo(ItemProgramacaoFormDto form) {
        if (form.getHoraInicio() != null && !form.getHoraInicio().isBlank()
                && form.getHoraFim() != null && !form.getHoraFim().isBlank()) {
            return IntervaloHorario.parse(form.getHoraInicio().trim() + "-" + form.getHoraFim().trim());
        }
        if (form.getHorario() != null && !form.getHorario().isBlank()) {
            return IntervaloHorario.parse(form.getHorario());
        }
        return null;
    }

    private String normalizarSala(Long instituicaoId, String sala) {
        if (sala == null || sala.isBlank()) {
            return null;
        }
        String nome = sala.trim();
        boolean cadastrada = servicoSala.listar(instituicaoId).stream()
                .anyMatch(s -> s.getNome() != null && s.getNome().equalsIgnoreCase(nome));
        ExcecaoDeDominio.quando(!cadastrada, "Sala não cadastrada na instituição: " + nome);
        return nome;
    }
}
