package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.AlunoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_aluno")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String rg;
    @CPF(message = "CPF inválido")
    private String cpf;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;

    @ManyToMany
    @JoinTable(name = "turma_aluno", joinColumns = @JoinColumn(name = "aluno_id"), inverseJoinColumns = @JoinColumn(name = "turma_id"))
    private Set<Turma> turma = new HashSet<>();

    public Aluno(AlunoDto alunoDto) {
        this.nome = alunoDto.getNome();
        this.rg = alunoDto.getRg();
        this.cpf = alunoDto.getCpf();
        this.dataDeNascimento = alunoDto.getDataDeNascimento();
        this.endereco = alunoDto.getEndereco();
        this.telefone = alunoDto.getTelefone();
        this.valorMensalidade = alunoDto.getValorMensalidade();
        this.diaVencimentoMensalidade = alunoDto.getDiaVencimentoMensalidade();
        this.nomeResponsavel = alunoDto.getNomeResponsavel();
        this.telefoneResponsavel = alunoDto.getTelefoneResponsavel();
    }
}