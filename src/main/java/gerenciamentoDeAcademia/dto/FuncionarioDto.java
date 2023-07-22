package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Funcionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioDto extends PessoaDto {
    private String cargo;
    private String especializacao;
    private Boolean permitirGerenciarFuncoes;

    public FuncionarioDto(Funcionario funcionario) {
        super(funcionario.getNome(),
                funcionario.getRg(),
                funcionario.getCpf(),
                funcionario.getDataDeNascimento(),
                funcionario.getEndereco(),
                funcionario.getTelefone());
        this.cargo = funcionario.getCargo();
        this.especializacao = funcionario.getEspecializacao();
        this.permitirGerenciarFuncoes = funcionario.getPermitirGerenciarFuncoes();
    }
}