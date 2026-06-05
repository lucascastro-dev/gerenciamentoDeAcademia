package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class InstituicaoDto {
    private Long registroInstituicao;
    private String razaoSocial;
    private String cnpj;
    private Boolean cadastroAtivo;
    private String endereco;
    private String telefone;
    private List<FuncionarioDto> funcionarios = new ArrayList<>();
    private Boolean possuiCadastroPendentes;
    private String email;
    private StatusFinanceiroInstituicao statusFinanceiro;
    private Boolean trialUtilizado;

    /** Plano inicial informado no cadastro da instituição (opcional no JSON, validado no serviço). */
    private PlanoInstituicaoTipo plano;

    /** Alias JSON {@code id} para o frontend. */
    public Long getId() {
        return registroInstituicao;
    }

    public InstituicaoDto(Instituicao instituicao) {
        this.registroInstituicao = instituicao.getId();
        this.razaoSocial = instituicao.getRazaoSocial();
        this.cnpj = instituicao.getCnpj();
        this.cadastroAtivo = Boolean.TRUE.equals(instituicao.getCadastroAtivo());
        this.endereco = instituicao.getEndereco();
        this.telefone = instituicao.getTelefone();
        this.possuiCadastroPendentes = instituicao.getPossuiCadastrosParaAprovar();
        this.email = instituicao.getEmail();
        this.statusFinanceiro = instituicao.getStatusFinanceiro();
        this.trialUtilizado = instituicao.getTrialUtilizado();

        this.funcionarios = Optional.ofNullable(instituicao.getFuncionarios())
                .orElse(Collections.emptySet())
                .stream()
                .map(FuncionarioDto::new)
                .collect(Collectors.toList());
    }
}
