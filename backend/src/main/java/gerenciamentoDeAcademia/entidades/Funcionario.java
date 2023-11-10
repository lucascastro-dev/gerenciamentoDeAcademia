package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Builder
@Audited
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_funcionario")
public class Funcionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String rg;
    private String cpf;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;
    private String cargo;
    private String especializacao;
    private Boolean permitirGerenciarFuncoes;

    public Funcionario(FuncionarioDto funcionarioDto) {
        this.nome = funcionarioDto.getNome();
        this.rg = funcionarioDto.getRg();
        this.cpf = funcionarioDto.getCpf();
        this.dataDeNascimento = funcionarioDto.getDataDeNascimento();
        this.endereco = funcionarioDto.getEndereco();
        this.telefone = funcionarioDto.getTelefone();
        this.cargo = funcionarioDto.getCargo();
        this.especializacao = funcionarioDto.getEspecializacao();
        this.permitirGerenciarFuncoes = funcionarioDto.getPermitirGerenciarFuncoes();
    }
}
