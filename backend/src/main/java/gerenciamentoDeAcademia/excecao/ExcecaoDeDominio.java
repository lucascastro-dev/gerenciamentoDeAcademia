package gerenciamentoDeAcademia.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ExcecaoDeDominio extends ApplicationException {

    public ExcecaoDeDominio(String mensagemDeErro) {
        super(mensagemDeErro, HttpStatus.BAD_REQUEST);
    }

    public static void quandoNuloOuVazio(String texto, String mensagemDeErro) {
        if (StringUtils.isEmpty(texto) || StringUtils.isEmpty(texto.trim()))
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoNuloOuVazio(Integer valor, String mensagemDeErro) {
        if (StringUtils.isEmpty(valor) || StringUtils.isEmpty(valor))
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoNuloOuVazio(Double valor, String mensagemDeErro) {
        if (StringUtils.isEmpty(valor) || StringUtils.isEmpty(valor))
            entaoDisparar(mensagemDeErro);
    }

    public static void quando(Boolean valor, String mensagemDeErro) {
        if (valor) {
            entaoDisparar(mensagemDeErro);
        }
    }

    public static void quandoValorIgualAZero(int valor, String mensagemDeErro) {
        if (valor == 0)
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoValorIgualAZero(BigDecimal valor, String mensagemDeErro) {
        if (valor.intValue() == 0)
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoValorIgualAZero(long valor, String mensagemDeErro) {
        if (valor == 0)
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoValorMenorQueUm(BigDecimal valor, String mensagemDeErro) {
        if (valor.intValue() < 1)
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoNulo(Object objeto, String mensagemDeErro) {
        if (Objects.isNull(objeto))
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoListaNulaOuVazia(List<?> lista, String mensagemDeErro) {
        if (Objects.isNull(lista)) {
            entaoDisparar(mensagemDeErro);
        } else if (lista.isEmpty()) {
            entaoDisparar(mensagemDeErro);
        }
    }

    public static void quandoDataNulaOuVazia(LocalDate data, String mensagemDeErro) {
        if (Objects.isNull(data))
            entaoDisparar(mensagemDeErro);
    }

    private static void entaoDisparar(String mensagemDeErro) {
        throw new ExcecaoDeDominio(mensagemDeErro);
    }
}
