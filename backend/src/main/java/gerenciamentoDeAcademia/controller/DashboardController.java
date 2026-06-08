package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DashboardPlataformaDto;
import gerenciamentoDeAcademia.dto.DashboardResumoDto;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.instituicao.ServicoDashboardInstituicao;
import gerenciamentoDeAcademia.servicos.plataforma.ServicoDashboardPlataforma;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ServicoDashboardPlataforma servicoDashboardPlataforma;
    private final ServicoDashboardInstituicao servicoDashboardInstituicao;

    @GetMapping("/plataforma/resumo")
    @PreAuthorize("@permissaoEvaluator.possuiMaster(authentication)")
    public DashboardPlataformaDto resumoPlataforma() {
        return servicoDashboardPlataforma.resumoAdministrativo();
    }

    @GetMapping("/resumo")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'dashboard:visualizar')")
    public DashboardResumoDto resumo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoDashboardInstituicao.resumo(usuario != null ? usuario.getInstituicaoId() : null);
    }
}
