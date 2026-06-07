package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PresencaGradeDto {
    private Long turmaId;
    private String modalidade;
    private String sala;
    private String horario;
    private Integer ano;
    private Integer mes;
    private List<Integer> diasComAula = new ArrayList<>();
    private List<AlunoPresencaLinhaDto> alunos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AlunoPresencaLinhaDto {
        private String nome;
        private String cpf;
        private Map<Integer, String> registros = new LinkedHashMap<>();
        private Map<String, Integer> totais = new LinkedHashMap<>();
        private Map<String, Integer> percentuais = new LinkedHashMap<>();
    }
}
