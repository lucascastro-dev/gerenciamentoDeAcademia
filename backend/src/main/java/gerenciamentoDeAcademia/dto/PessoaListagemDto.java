package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.util.MascaramentoDadosUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PessoaListagemDto {
    private Long id;
    private String nome;
    private String cpf;
    private String cpfExibicao;
    private LocalDate dataDeNascimento;
    private String cargo;
    private Long instituicaoId;
    private String instituicaoNome;
    private Long vinculoId;

    public static PessoaListagemDto deAluno(Aluno aluno, boolean mascararCpf) {
        PessoaListagemDto dto = new PessoaListagemDto();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setDataDeNascimento(aluno.getDataDeNascimento());
        if (mascararCpf) {
            dto.setCpfExibicao(MascaramentoDadosUtil.cpf(aluno.getCpf()));
        } else {
            dto.setCpf(aluno.getCpf());
            dto.setCpfExibicao(formatarCpf(aluno.getCpf()));
        }
        return dto;
    }

    public static PessoaListagemDto deFuncionario(Funcionario funcionario) {
        PessoaListagemDto dto = new PessoaListagemDto();
        dto.setId(funcionario.getId());
        dto.setNome(funcionario.getNome());
        dto.setCpf(funcionario.getCpf());
        dto.setCpfExibicao(formatarCpf(funcionario.getCpf()));
        dto.setDataDeNascimento(funcionario.getDataDeNascimento());
        String cargo = funcionario.getCargo();
        if (cargo == null || cargo.isBlank()) {
            cargo = funcionario.getTipoFuncionario() != null
                    ? funcionario.getTipoFuncionario().getDescricao()
                    : "";
        }
        dto.setCargo(cargo);
        return dto;
    }

    public static PessoaListagemDto deVinculoFuncionario(VinculoFuncionarioInstituicao vinculo) {
        Funcionario funcionario = vinculo.getFuncionario();
        PessoaListagemDto dto = new PessoaListagemDto();
        dto.setVinculoId(vinculo.getId());
        dto.setId(funcionario.getId());
        dto.setNome(funcionario.getNome());
        dto.setCpf(funcionario.getCpf());
        dto.setCpfExibicao(formatarCpf(funcionario.getCpf()));
        dto.setDataDeNascimento(funcionario.getDataDeNascimento());
        dto.setCargo(vinculo.descricaoCargo());
        dto.setInstituicaoId(vinculo.getInstituicao().getId());
        dto.setInstituicaoNome(vinculo.getInstituicao().getRazaoSocial());
        return dto;
    }

    private static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
