package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("plano-instituicao")
public class PlanoInstituicaoController {

    @Autowired
    private ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @GetMapping("/tipos")
    public List<Map<String, String>> listarTiposPlano() {
        return Arrays.stream(PlanoInstituicaoTipo.values())
                .map(p -> Map.of(
                        "codigo", p.name(),
                        "descricao", p.getDescricao(),
                        "dias", String.valueOf(p.getDias())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{instituicaoId}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'plano:visualizar')")
    public AssinaturaPlataformaDto consultar(@PathVariable Long instituicaoId) {
        return servicoAssinaturaPlataforma.consultar(instituicaoId);
    }

    @PutMapping("/{instituicaoId}/ativar")
    @PreAuthorize("@permissaoEvaluator.possuiMaster(authentication)")
    public AssinaturaPlataformaDto ativar(
            @PathVariable Long instituicaoId,
            @RequestBody Map<String, String> body) {
        String codigo = body != null ? body.get("plano") : null;
        PlanoInstituicaoTipo plano = codigo != null
                ? PlanoInstituicaoTipo.valueOf(codigo)
                : null;
        return servicoAssinaturaPlataforma.ativarPlano(instituicaoId, plano);
    }
}
