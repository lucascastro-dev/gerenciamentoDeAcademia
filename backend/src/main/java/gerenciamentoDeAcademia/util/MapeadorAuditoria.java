package gerenciamentoDeAcademia.util;

import org.springframework.data.history.RevisionMetadata;

public final class MapeadorAuditoria {

    private MapeadorAuditoria() {
    }

    public static String ajusteExibicao(String acao) {
        if (acao == null) {
            return "Alteração";
        }
        return switch (acao) {
            case "CADASTRO", "PRE_CADASTRO" -> "Criação";
            case "ALTERACAO" -> "Edição";
            case "EXCLUSAO" -> "Exclusão";
            default -> acao;
        };
    }

    public static String ajusteDeRevisao(RevisionMetadata.RevisionType tipo) {
        if (tipo == null) {
            return "Alteração";
        }
        return switch (tipo) {
            case INSERT -> "Criação";
            case UPDATE -> "Edição";
            case DELETE -> "Exclusão";
            default -> "Alteração";
        };
    }
}
