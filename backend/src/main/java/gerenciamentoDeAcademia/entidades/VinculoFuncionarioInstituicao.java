package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_vinculo_funcionario_instituicao", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"funcionario_id", "instituicao_id"})
})
public class VinculoFuncionarioInstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFuncionario tipoFuncionario;

    @Enumerated(EnumType.STRING)
    private AreaTerceirizado areaTerceirizado;

    private String especializacao;

    /** Data de início do vínculo na instituição — base para período aquisitivo de férias. */
    private LocalDate dataAdmissao;

    public void atualizarFuncao(TipoFuncionario tipo, AreaTerceirizado area, String especializacao) {
        ExcecaoDeDominio.quandoNulo(tipo, "Tipo de funcionário é obrigatório na instituição.");
        if (tipo == TipoFuncionario.DIRETOR) {
            throw new ExcecaoDeDominio("Perfil Diretor não pode ser atribuído por esta tela.");
        }
        if (tipo == TipoFuncionario.TERCEIRIZADO) {
            ExcecaoDeDominio.quandoNulo(area,
                    "Informe a área do terceirizado (RH, professor substituto ou TI).");
        }
        if (tipo == TipoFuncionario.PROFESSOR) {
            ExcecaoDeDominio.quandoNuloOuVazio(especializacao,
                    "Especialização é obrigatória para professores.");
        }
        this.tipoFuncionario = tipo;
        this.areaTerceirizado = tipo == TipoFuncionario.TERCEIRIZADO ? area : null;
        this.especializacao = tipo == TipoFuncionario.PROFESSOR ? especializacao : null;
    }

    public String descricaoCargo() {
        return tipoFuncionario != null ? tipoFuncionario.getDescricao() : "";
    }
}
