package gerenciamentoDeAcademia.dto;

public record ProfessorResumoDto(String cpf, String nome) {
    public static ProfessorResumoDto of(gerenciamentoDeAcademia.entidades.Funcionario f) {
        return new ProfessorResumoDto(f.getCpf(), f.getNome());
    }
}
