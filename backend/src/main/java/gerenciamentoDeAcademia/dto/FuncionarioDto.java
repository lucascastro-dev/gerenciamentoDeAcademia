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
        this.setNome(funcionario.getNome());
        this.setRg(funcionario.getRg());
        this.setCpf(funcionario.getCpf());
        this.setDataDeNascimento(funcionario.getDataDeNascimento());
        this.setEndereco(funcionario.getEndereco());
        this.setTelefone(funcionario.getTelefone());

        this.cargo = funcionario.getCargo();
        this.especializacao = funcionario.getEspecializacao();
        this.permitirGerenciarFuncoes = funcionario.getPermitirGerenciarFuncoes();
    }
}