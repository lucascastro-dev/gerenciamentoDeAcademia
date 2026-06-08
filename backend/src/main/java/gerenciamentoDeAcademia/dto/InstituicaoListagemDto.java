package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InstituicaoListagemDto {
    private Long id;
    private String razaoSocial;
    private String cnpj;
    private String cnpjExibicao;
    private String plano;
    private String planoExibicao;
    private String statusFinanceiro;
    private String statusFinanceiroExibicao;
    private Boolean cadastroAtivo;
    private String statusCadastroExibicao;

    public static InstituicaoListagemDto de(Instituicao instituicao, AssinaturaPlataforma assinatura) {
        InstituicaoListagemDto dto = new InstituicaoListagemDto();
        dto.setId(instituicao.getId());
        dto.setRazaoSocial(instituicao.getRazaoSocial());
        dto.setCnpj(instituicao.getCnpj());
        dto.setCnpjExibicao(formatarCnpj(instituicao.getCnpj()));
        dto.setCadastroAtivo(Boolean.TRUE.equals(instituicao.getCadastroAtivo()));
        dto.setStatusCadastroExibicao(dto.getCadastroAtivo() ? "Ativo" : "Inativo");
        StatusFinanceiroInstituicao statusFin = instituicao.getStatusFinanceiro() != null
                ? instituicao.getStatusFinanceiro()
                : StatusFinanceiroInstituicao.NAO_APLICAVEL;
        dto.setStatusFinanceiro(statusFin.name());
        dto.setStatusFinanceiroExibicao(statusFin.getDescricao());
        if (assinatura != null && assinatura.getPlano() != null) {
            dto.setPlano(assinatura.getPlano().name());
            dto.setPlanoExibicao(assinatura.getPlano().getDescricao());
        } else {
            dto.setPlanoExibicao("—");
        }
        return dto;
    }

    private static String formatarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return cnpj;
        }
        String raw = cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (raw.length() != 14) {
            return cnpj;
        }
        return raw.substring(0, 2) + "." + raw.substring(2, 5) + "." + raw.substring(5, 8)
                + "/" + raw.substring(8, 12) + "-" + raw.substring(12);
    }
}
