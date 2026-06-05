package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.HttpStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    private String email;
    private String cargo;

    @Enumerated(EnumType.STRING)
    private TipoFuncionario tipoFuncionario;

    /** Obrigatório quando {@link #tipoFuncionario} = TERCEIRIZADO. */
    @Enumerated(EnumType.STRING)
    private AreaTerceirizado areaTerceirizado;

    private String especializacao;
    private Boolean permitirGerenciarFuncoes;
    private String senha;
    private Boolean cadastroAtivo;

    public Funcionario(FuncionarioDto funcionarioDto) {
        validar(funcionarioDto);
        this.nome = funcionarioDto.getNome();
        this.rg = funcionarioDto.getRg();
        this.cpf = funcionarioDto.getCpf();
        this.dataDeNascimento = funcionarioDto.getDataDeNascimento();
        this.endereco = funcionarioDto.getEndereco();
        this.telefone = funcionarioDto.getTelefone();
        this.email = funcionarioDto.getEmail();
        this.tipoFuncionario = resolverTipo(funcionarioDto);
        this.cargo = this.tipoFuncionario.getDescricao();
        this.areaTerceirizado = funcionarioDto.getAreaTerceirizado();
        this.especializacao = funcionarioDto.getEspecializacao();
        this.permitirGerenciarFuncoes = Boolean.TRUE.equals(funcionarioDto.getPermitirGerenciarFuncoes());
        this.senha = funcionarioDto.getSenha();
        this.cadastroAtivo = false;
    }

    private void validar(FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNulo(funcionarioDto, "Obrigatório preencher dados do funcionario");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(funcionarioDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getTelefone(), "Telefone é obrigatório!");
        TipoFuncionario tipo = resolverTipo(funcionarioDto);
        ExcecaoDeDominio.quandoNulo(tipo, "Tipo de funcionário é obrigatório!");
        if (tipo == TipoFuncionario.PROFESSOR) {
            ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getEspecializacao(), "Especialização é obrigatória para professores!");
        }
        if (tipo == TipoFuncionario.TERCEIRIZADO) {
            ExcecaoDeDominio.quandoNulo(funcionarioDto.getAreaTerceirizado(),
                    "Informe a área do terceirizado (RH, professor substituto ou TI).");
        }
    }

    private TipoFuncionario resolverTipo(FuncionarioDto dto) {
        if (dto.getTipoFuncionario() != null) {
            return dto.getTipoFuncionario();
        }
        return TipoFuncionario.fromCargo(dto.getCargo());
    }

    public void atualizarDadosPessoais(FuncionarioDto funcionarioDto) {
        this.nome = funcionarioDto.getNome();
        this.rg = funcionarioDto.getRg();
        this.dataDeNascimento = funcionarioDto.getDataDeNascimento();
        this.endereco = funcionarioDto.getEndereco();
        this.telefone = funcionarioDto.getTelefone();
        if (funcionarioDto.getEmail() != null) {
            this.email = funcionarioDto.getEmail().isBlank() ? null : funcionarioDto.getEmail().trim();
        }
        if (tipoFuncionario == TipoFuncionario.PROFESSOR) {
            this.especializacao = funcionarioDto.getEspecializacao();
        }
    }

    public void atualizar(FuncionarioDto funcionarioDto) {
        this.nome = funcionarioDto.getNome();
        this.rg = funcionarioDto.getRg();
        this.dataDeNascimento = funcionarioDto.getDataDeNascimento();
        this.endereco = funcionarioDto.getEndereco();
        this.telefone = funcionarioDto.getTelefone();
        if (funcionarioDto.getEmail() != null) {
            this.email = funcionarioDto.getEmail().isBlank() ? null : funcionarioDto.getEmail().trim();
        }
        TipoFuncionario tipo = resolverTipo(funcionarioDto);
        if (tipo != null) {
            this.tipoFuncionario = tipo;
            this.cargo = tipo.getDescricao();
            if (tipo == TipoFuncionario.TERCEIRIZADO) {
                ExcecaoDeDominio.quandoNulo(funcionarioDto.getAreaTerceirizado(),
                        "Informe a área do terceirizado (RH, professor substituto ou TI).");
                this.areaTerceirizado = funcionarioDto.getAreaTerceirizado();
            } else {
                this.areaTerceirizado = null;
            }
        }
        this.especializacao = funcionarioDto.getEspecializacao();
        if (funcionarioDto.getPermitirGerenciarFuncoes() != null) {
            this.permitirGerenciarFuncoes = funcionarioDto.getPermitirGerenciarFuncoes();
        }
        this.cadastroAtivo = funcionarioDto.getCadastroAtivo();
    }

    /** @deprecated Use {@link gerenciamentoDeAcademia.servicos.master.ServicoMasterPlataforma#ehOperadorPlataforma}. */
    @Deprecated
    public boolean isUsuarioMaster() {
        return Boolean.TRUE.equals(permitirGerenciarFuncoes);
    }

    public void ativar() {
        if (Boolean.TRUE.equals(this.cadastroAtivo)) {
            throw new ApplicationException("Funcionário já possui cadastro ativo", HttpStatus.BAD_REQUEST);
        }
        this.cadastroAtivo = true;
    }

    public void inativar() {
        if (Boolean.FALSE.equals(this.cadastroAtivo)) {
            throw new ApplicationException("Funcionário já está inativo", HttpStatus.BAD_REQUEST);
        }
        this.cadastroAtivo = false;
    }
}
