package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.ArquivoPdfDto;
import gerenciamentoDeAcademia.dto.DocumentoRemuneracaoDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.colaborador.ServicoDocumentoRemuneracaoColaborador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("colaborador/documentos-remuneracao")
@RequiredArgsConstructor
public class ColaboradorRemuneracaoController {

    private final ServicoDocumentoRemuneracaoColaborador servico;

    @GetMapping
    public List<DocumentoRemuneracaoDto> meusDocumentos(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        return servico.listarMeusDocumentos(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                mes,
                ano);
    }

    @GetMapping("/{id}")
    public DocumentoRemuneracaoDto meuDocumento(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        return servico.obterMeuDocumento(instituicaoDaSessao(usuario), cpfDaSessao(usuario), id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<org.springframework.core.io.Resource> meuDocumentoPdf(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        ArquivoPdfDto arquivo = servico.baixarMeuDocumentoPdf(
                instituicaoDaSessao(usuario),
                cpfDaSessao(usuario),
                id);
        String nomeCodificado = URLEncoder.encode(arquivo.getNomeArquivo(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + nomeCodificado)
                .body(arquivo.getResource());
    }

    private Long instituicaoDaSessao(UsuarioAutenticado usuario) {
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
        return instituicaoId;
    }

    private String cpfDaSessao(UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quando(usuario == null || usuario.getFuncionario() == null,
                "Sessão de colaborador inválida.");
        return usuario.getFuncionario().getCpf();
    }
}
