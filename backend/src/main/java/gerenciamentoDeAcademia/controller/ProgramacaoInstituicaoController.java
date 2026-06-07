package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.ConflitoHorarioDto;
import gerenciamentoDeAcademia.dto.GradeHorariaEventoDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoFormDto;
import gerenciamentoDeAcademia.dto.SalaDto;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.programacao.ServicoGradeHoraria;
import gerenciamentoDeAcademia.servicos.programacao.ServicoProgramacaoInstituicao;
import gerenciamentoDeAcademia.servicos.programacao.ServicoSala;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({
        "/instituicao/{instituicaoId}/programacao",
        "/academia/{instituicaoId}/programacao"
})
public class ProgramacaoInstituicaoController {

    private final ServicoProgramacaoInstituicao servicoProgramacao;
    private final ServicoGradeHoraria servicoGradeHoraria;
    private final ServicoSala servicoSala;

    public ProgramacaoInstituicaoController(
            ServicoProgramacaoInstituicao servicoProgramacao,
            ServicoGradeHoraria servicoGradeHoraria,
            ServicoSala servicoSala) {
        this.servicoProgramacao = servicoProgramacao;
        this.servicoGradeHoraria = servicoGradeHoraria;
        this.servicoSala = servicoSala;
    }

    @GetMapping("/tipos")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:consultar')")
    public List<Map<String, String>> tipos() {
        return Arrays.stream(TipoItemProgramacao.values())
                .map(t -> Map.of("codigo", t.name(), "descricao", t.getDescricao()))
                .collect(Collectors.toList());
    }

    @GetMapping("/itens")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:consultar')")
    public List<ItemProgramacaoDto> listarItens(@PathVariable Long instituicaoId) {
        return servicoProgramacao.listarPorInstituicao(instituicaoId);
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possuiAlguma(authentication, 'programacao:gerenciar', 'programacao:gerenciar-itens')")
    public ItemProgramacaoDto criarItem(@PathVariable Long instituicaoId, @RequestBody ItemProgramacaoFormDto form) {
        return servicoProgramacao.criar(instituicaoId, form);
    }

    @PutMapping("/itens/{id}")
    @PreAuthorize("@permissaoEvaluator.possuiAlguma(authentication, 'programacao:gerenciar', 'programacao:gerenciar-itens')")
    public ItemProgramacaoDto atualizarItem(
            @PathVariable Long instituicaoId,
            @PathVariable Long id,
            @RequestBody ItemProgramacaoFormDto form) {
        return servicoProgramacao.atualizar(instituicaoId, id, form);
    }

    @DeleteMapping("/itens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possuiAlguma(authentication, 'programacao:gerenciar', 'programacao:gerenciar-itens')")
    public void excluirItem(@PathVariable Long instituicaoId, @PathVariable Long id) {
        servicoProgramacao.excluir(instituicaoId, id);
    }

    @PostMapping("/itens/validar-conflito")
    @PreAuthorize("@permissaoEvaluator.possuiAlguma(authentication, 'programacao:gerenciar', 'programacao:gerenciar-itens')")
    public List<ConflitoHorarioDto> validarConflito(
            @PathVariable Long instituicaoId,
            @RequestBody ItemProgramacaoFormDto form,
            @RequestParam(required = false) Long ignorarId) {
        return servicoProgramacao.validarFormulario(instituicaoId, form, ignorarId);
    }

    @GetMapping("/grade")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:consultar')")
    public List<GradeHorariaEventoDto> grade(
            @PathVariable Long instituicaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate semana) {
        return servicoGradeHoraria.montarGrade(instituicaoId, semana);
    }

    @GetMapping("/salas")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:consultar')")
    public List<SalaDto> listarSalas(@PathVariable Long instituicaoId) {
        return servicoSala.listar(instituicaoId);
    }

    @PostMapping("/salas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:gerenciar')")
    public SalaDto criarSala(@PathVariable Long instituicaoId, @RequestBody SalaDto dto) {
        return servicoSala.criar(instituicaoId, dto);
    }

    @DeleteMapping("/salas/{salaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'programacao:gerenciar')")
    public void excluirSala(@PathVariable Long instituicaoId, @PathVariable Long salaId) {
        servicoSala.excluir(instituicaoId, salaId);
    }
}
