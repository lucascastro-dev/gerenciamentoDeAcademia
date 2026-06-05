package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.InstituicaoDto;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_instituicao")
public class Instituicao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String razaoSocial;
    private String cnpj;
    private Boolean cadastroAtivo;
    private String endereco;
    private String telefone;
    private Boolean possuiCadastrosParaAprovar;
    private String email;

    @Enumerated(EnumType.STRING)
    private StatusFinanceiroInstituicao statusFinanceiro;

    private Boolean trialUtilizado;

    @ManyToMany
    @JoinTable(name = "instituicao_funcionario", joinColumns = @JoinColumn(name = "instituicao_id"), inverseJoinColumns = @JoinColumn(name = "funcionario_id"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Funcionario> funcionarios = new HashSet<>();

    public Instituicao(InstituicaoDto instituicaoDto) {
        this.razaoSocial = instituicaoDto.getRazaoSocial();
        this.cnpj = instituicaoDto.getCnpj();
        this.cadastroAtivo = Boolean.TRUE.equals(instituicaoDto.getCadastroAtivo());
        this.endereco = instituicaoDto.getEndereco();
        this.telefone = instituicaoDto.getTelefone();
        this.email = instituicaoDto.getEmail();
        this.statusFinanceiro = instituicaoDto.getStatusFinanceiro() != null
                ? instituicaoDto.getStatusFinanceiro()
                : StatusFinanceiroInstituicao.NAO_APLICAVEL;
        this.trialUtilizado = Boolean.TRUE.equals(instituicaoDto.getTrialUtilizado());
    }

    public void validarVinculo(Funcionario funcionario) {
        if (!this.getFuncionarios().contains(funcionario)) {
            throw new ApplicationException("Funcionário não cadastrado nesta instituição", HttpStatus.BAD_REQUEST);
        }
    }

    public void atualizarStatusPendencias() {
        boolean pendente = this.funcionarios.stream()
                .anyMatch(f -> f.getCadastroAtivo() == null || !f.getCadastroAtivo());
        this.setPossuiCadastrosParaAprovar(pendente);
    }

    public void atualizarCadastro(InstituicaoDto instituicaoDto) {
        this.razaoSocial = instituicaoDto.getRazaoSocial();
        this.endereco = instituicaoDto.getEndereco();
        this.telefone = instituicaoDto.getTelefone();
        if (instituicaoDto.getCadastroAtivo() != null) {
            this.cadastroAtivo = instituicaoDto.getCadastroAtivo();
        }
        if (instituicaoDto.getEmail() != null) {
            this.email = instituicaoDto.getEmail();
        }
        if (instituicaoDto.getStatusFinanceiro() != null) {
            this.statusFinanceiro = instituicaoDto.getStatusFinanceiro();
        }
    }
}
