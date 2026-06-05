package gerenciamentoDeAcademia.infra.seguranca;

/**
 * Identificador de vínculo usado no login do operador master da plataforma (sem instituição específica).
 */
public final class VinculoPlataforma {

    public static final String ID = "0";
    public static final Long ID_LONG = 0L;
    public static final String ROTULO = "Plataforma — Operação master";

    private VinculoPlataforma() {
    }

    public static boolean ehVinculoPlataforma(String vinculo) {
        return vinculo != null && (ID.equals(vinculo.trim()) || "0".equals(vinculo.replaceAll("\\D", "")));
    }
}
