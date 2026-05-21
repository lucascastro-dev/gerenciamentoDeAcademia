package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PortalAlunoDadosDto {
    private String nome;
    private String cpf;
    private String rg;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private LocalDate dataUltimoPagamentoMensalidade;

    public static PortalAlunoDadosDto of(Aluno aluno) {
        PortalAlunoDadosDto dto = new PortalAlunoDadosDto();
        dto.setNome(aluno.getNome());
        dto.setCpf(aluno.getCpf());
        dto.setRg(aluno.getRg());
        dto.setDataDeNascimento(aluno.getDataDeNascimento());
        dto.setEndereco(aluno.getEndereco());
        dto.setTelefone(aluno.getTelefone());
        dto.setValorMensalidade(aluno.getValorMensalidade());
        dto.setDiaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade());
        dto.setDataUltimoPagamentoMensalidade(aluno.getDataUltimoPagamentoMensalidade());
        return dto;
    }
}
