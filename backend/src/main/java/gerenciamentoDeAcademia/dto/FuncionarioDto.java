package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioDto extends PessoaDto {
    private String cargo;
    private TipoFuncionario tipoFuncionario;
    private AreaTerceirizado areaTerceirizado;
    private String especializacao;
    private Boolean permitirGerenciarFuncoes;
    private Boolean cadastroAtivo;

    public FuncionarioDto(Funcionario funcionario) {
        this.setNome(funcionario.getNome());
        this.setRg(funcionario.getRg());
        this.setCpf(funcionario.getCpf());
        this.setDataDeNascimento(funcionario.getDataDeNascimento());
        this.setEndereco(funcionario.getEndereco());
        this.setTelefone(funcionario.getTelefone());
        this.setEmail(funcionario.getEmail());
        this.setSenha(funcionario.getSenha());

        this.cargo = funcionario.getCargo();
        this.tipoFuncionario = funcionario.getTipoFuncionario();
        this.areaTerceirizado = funcionario.getAreaTerceirizado();
        this.especializacao = funcionario.getEspecializacao();
        this.permitirGerenciarFuncoes = funcionario.getPermitirGerenciarFuncoes();
        this.cadastroAtivo = funcionario.getCadastroAtivo();
    }
}