package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class GeradorDeCertificadosResumoFaixasTest {

    @Test
    @DisplayName("Dado professor sem projeto, Quando gerar nome do arquivo, Então inclui data e hora")
    void deveGerarNomeDeArquivoComDataHoraSemProjeto() {
        DadosCertificadoDto dados = new DadosCertificadoDto();
        dados.setProfessor("Joao Silva");
        dados.setPersonalizado(false);
        LocalDateTime momento = LocalDateTime.of(2026, 6, 5, 14, 30, 22);

        Assertions.assertEquals(
                "pedido_faixas_Joao Silva_20260605_143022.txt",
                GeradorDeCertificados.nomeArquivoResumoDeFaixas(dados, momento));
    }

    @Test
    @DisplayName("Dado certificado personalizado, Quando gerar nome do arquivo, Então inclui projeto")
    void deveGerarNomeDeArquivoComDataHoraEProjeto() {
        DadosCertificadoDto dados = new DadosCertificadoDto();
        dados.setProfessor("Joao Silva");
        dados.setPersonalizado(true);
        dados.setProjeto("SCTJ");
        LocalDateTime momento = LocalDateTime.of(2026, 6, 5, 14, 30, 22);

        Assertions.assertEquals(
                "pedido_faixas_Joao Silva_SCTJ_20260605_143022.txt",
                GeradorDeCertificados.nomeArquivoResumoDeFaixas(dados, momento));
    }

    @Test
    @DisplayName("Dado lote de alunos, Quando montar conteúdo, Então inclui cabeçalho alunos e quantidades")
    void deveMontarConteudoComCabecalhoAlunosEQuantidades() {
        DadosCertificadoDto dados = new DadosCertificadoDto();
        dados.setProfessor("Joao Silva");
        dados.setPersonalizado(true);
        dados.setProjeto("SCTJ");
        dados.setDataEvento(LocalDate.of(2026, 6, 5));

        AlunoDto aluno1 = new AlunoDto();
        aluno1.setNome("Lucas de Castro Ribeiro Ferreira");
        aluno1.setFaixa("Preta");
        aluno1.setMedida("A2");
        AlunoDto aluno2 = new AlunoDto();
        aluno2.setNome("Maria Silva");
        aluno2.setFaixa("Branca");
        aluno2.setMedida("M00");
        dados.setAlunos(List.of(aluno1, aluno2));

        Map<String, Map<String, Integer>> resumo = new LinkedHashMap<>();
        resumo.put("Preta", Map.of("A2", 1));
        resumo.put("Branca", Map.of("M00", 3, "M1", 1));

        String conteudo = GeradorDeCertificados.montarConteudoResumoDeFaixas(
                dados, resumo, LocalDateTime.of(2026, 6, 5, 14, 30, 22));

        Assertions.assertTrue(conteudo.contains("Resumo exame de graduação"));
        Assertions.assertTrue(conteudo.contains("Lucas de Castro Ribeiro Ferreira - Preta - A2"));
        Assertions.assertTrue(conteudo.contains("Faixa Preta:"));
        Assertions.assertTrue(conteudo.contains("A2 - 1 unidades"));
    }
}
