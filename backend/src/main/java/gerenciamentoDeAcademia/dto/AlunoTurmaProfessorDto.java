package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.util.MascaramentoDadosUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlunoTurmaProfessorDto {
    private String cpf;
    private String nome;
    private String cpfMascarado;
    private String telefoneMascarado;

    public static AlunoTurmaProfessorDto of(Aluno aluno) {
        AlunoTurmaProfessorDto dto = new AlunoTurmaProfessorDto();
        dto.setCpf(aluno.getCpf());
        dto.setNome(aluno.getNome());
        dto.setCpfMascarado(MascaramentoDadosUtil.cpf(aluno.getCpf()));
        dto.setTelefoneMascarado(MascaramentoDadosUtil.telefone(aluno.getTelefone()));
        return dto;
    }
}
