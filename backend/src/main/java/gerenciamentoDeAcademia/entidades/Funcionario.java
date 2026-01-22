package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.br.CPF;

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
    @CPF(message = "CPF inválido")
    private String cpf;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;
    private String cargo;
    private String especializacao;
    private Boolean permitirGerenciarFuncoes;

    public Funcionario(FuncionarioDto funcionarioDto) {
        validar(funcionarioDto);
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

    private void validar(FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNulo(funcionarioDto, "Obrigatório preencher dados do funcionario");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(funcionarioDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCargo(), "Cargo é obrigatório!");
        if (funcionarioDto.getCargo() != null || funcionarioDto.getCargo().isEmpty())
            ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEspecializacao(), "Especialização é obrigatório!");
    }
}
