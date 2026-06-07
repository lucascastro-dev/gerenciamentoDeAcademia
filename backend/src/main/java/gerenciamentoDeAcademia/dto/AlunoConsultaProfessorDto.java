package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.util.MascaramentoDadosUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlunoConsultaProfessorDto {
    private String nome;
    private String cpfMascarado;
    private String rgMascarado;
    private LocalDate dataDeNascimento;
    private String enderecoResumido;
    private String telefoneMascarado;
    private String emailMascarado;
    private String nomeResponsavel;
    private String telefoneResponsavelMascarado;
    private List<TurmaResumoDto> turmasInstituicao = new ArrayList<>();

    public static AlunoConsultaProfessorDto of(Aluno aluno, List<TurmaResumoDto> turmas) {
        AlunoConsultaProfessorDto dto = new AlunoConsultaProfessorDto();
        dto.setNome(aluno.getNome());
        dto.setCpfMascarado(MascaramentoDadosUtil.cpf(aluno.getCpf()));
        dto.setRgMascarado(MascaramentoDadosUtil.rg(aluno.getRg()));
        dto.setDataDeNascimento(aluno.getDataDeNascimento());
        dto.setEnderecoResumido(enderecoParaConsulta(aluno.getEndereco()));
        dto.setTelefoneMascarado(MascaramentoDadosUtil.telefone(aluno.getTelefone()));
        dto.setEmailMascarado(MascaramentoDadosUtil.email(aluno.getEmail()));
        dto.setNomeResponsavel(aluno.getNomeResponsavel());
        dto.setTelefoneResponsavelMascarado(MascaramentoDadosUtil.telefone(aluno.getTelefoneResponsavel()));
        dto.setTurmasInstituicao(turmas != null ? turmas : List.of());
        return dto;
    }

    /** Mantém JSON estruturado para o frontend preencher os campos; evita quebrar o parse. */
    private static String enderecoParaConsulta(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            return "";
        }
        return endereco.trim();
    }
}
