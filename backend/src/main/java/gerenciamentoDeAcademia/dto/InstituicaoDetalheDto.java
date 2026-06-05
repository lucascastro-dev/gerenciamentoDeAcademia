package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InstituicaoDetalheDto extends InstituicaoDto {
    private AssinaturaPlataformaDto assinatura;
    private StatusFinanceiroInstituicao statusFinanceiro;
    private Boolean trialUtilizado;
    private String email;
    private String cpfAdministrador;
    private String nomeAdministrador;

    public static InstituicaoDetalheDto of(Instituicao instituicao, AssinaturaPlataformaDto assinatura) {
        InstituicaoDetalheDto dto = new InstituicaoDetalheDto();
        if (instituicao == null) {
            return dto;
        }
        dto.setRegistroInstituicao(instituicao.getId());
        dto.setRazaoSocial(instituicao.getRazaoSocial());
        dto.setCnpj(instituicao.getCnpj());
        dto.setCadastroAtivo(Boolean.TRUE.equals(instituicao.getCadastroAtivo()));
        dto.setEndereco(instituicao.getEndereco());
        dto.setTelefone(instituicao.getTelefone());
        dto.setPossuiCadastroPendentes(instituicao.getPossuiCadastrosParaAprovar());
        dto.setStatusFinanceiro(instituicao.getStatusFinanceiro());
        dto.setTrialUtilizado(instituicao.getTrialUtilizado());
        dto.setEmail(instituicao.getEmail());
        dto.setAssinatura(assinatura);
        if (instituicao.getFuncionarios() != null) {
            instituicao.getFuncionarios().stream()
                    .filter(f -> f.getTipoFuncionario() == TipoFuncionario.ADMINISTRADOR)
                    .findFirst()
                    .ifPresent(admin -> preencherAdministrador(dto, admin));
        }
        return dto;
    }

    private static void preencherAdministrador(InstituicaoDetalheDto dto, Funcionario admin) {
        dto.setCpfAdministrador(admin.getCpf());
        dto.setNomeAdministrador(admin.getNome());
    }
}
