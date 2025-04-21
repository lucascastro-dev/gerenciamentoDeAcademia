package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.Data;
import lombok.NoArgsConstructor;
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

@Data
@Entity
@NoArgsConstructor
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
        validar(alunoDto);
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

    private void validar(AlunoDto alunoDto) {
        ExcecaoDeDominio.quandoNulo(alunoDto, "Obrigatório preencher dados do aluno");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(alunoDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getValorMensalidade(), "Valor da mensalidade é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getDiaVencimentoMensalidade(), "Dia de vencimento da mensalidade é obrigatório!");

        if ((LocalDate.now().getYear() - alunoDto.getDataDeNascimento().getYear()) < 18) {
            ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getNomeResponsavel(), "Nome do responsável é obrigatório!");
            ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getTelefoneResponsavel(), "Telefone do responsável é obrigatório!");
        }
    }
}