package gerenciamentoDeAcademia.servicos.colaborador;

import gerenciamentoDeAcademia.entidades.ConferenciaPontoMensal;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.RegistroDiaPonto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.ConferenciaPontoMensalRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.RegistroDiaPontoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class ServicoFolhaPontoTest {

    private static final Long INSTITUICAO_ID = 1L;
    private static final String CPF = "71428793860";
    private static final String NOME = "Teste RH";

    @InjectMocks
    ServicoFolhaPonto servico;

    @Mock
    RegistroDiaPontoRepository registroRepository;
    @Mock
    ConferenciaPontoMensalRepository conferenciaRepository;
    @Mock
    VinculoFuncionarioInstituicaoRepository vinculoRepository;
    @Mock
    InstituicaoRepository instituicaoRepository;

    @Test
    @DisplayName("Dado dia sem registro, Quando marcar ponto, Então registra entrada")
    void deveRegistrarEntrada() {
        Instituicao inst = new Instituicao();
        inst.setId(INSTITUICAO_ID);
        Mockito.when(instituicaoRepository.findById(INSTITUICAO_ID)).thenReturn(Optional.of(inst));
        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.empty());
        Mockito.when(registroRepository.findByInstituicao_IdAndCpfColaboradorAndDataRegistro(
                INSTITUICAO_ID, CPF, LocalDate.now())).thenReturn(Optional.empty());
        Mockito.when(registroRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        var status = servico.marcarPonto(INSTITUICAO_ID, CPF, NOME);

        Assertions.assertEquals("SAIDA", status.proximaAcao());
        ArgumentCaptor<RegistroDiaPonto> captor = ArgumentCaptor.forClass(RegistroDiaPonto.class);
        Mockito.verify(registroRepository).save(captor.capture());
        Assertions.assertNotNull(captor.getValue().getHoraEntrada());
        Assertions.assertNull(captor.getValue().getHoraSaida());
    }

    @Test
    @DisplayName("Dado entrada registrada, Quando marcar saída, Então completa o dia")
    void deveRegistrarSaida() {
        LocalDateTime entrada = LocalDateTime.now().minusHours(8);
        RegistroDiaPonto registro = RegistroDiaPonto.builder()
                .instituicao(new Instituicao())
                .cpfColaborador(CPF)
                .nomeColaborador(NOME)
                .dataRegistro(LocalDate.now())
                .horaEntrada(entrada)
                .build();

        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.empty());
        Mockito.when(registroRepository.findByInstituicao_IdAndCpfColaboradorAndDataRegistro(
                INSTITUICAO_ID, CPF, LocalDate.now())).thenReturn(Optional.of(registro));
        Mockito.when(registroRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        var status = servico.marcarPonto(INSTITUICAO_ID, CPF, NOME);

        Assertions.assertEquals("COMPLETO", status.proximaAcao());
        Assertions.assertNotNull(registro.getHoraSaida());
    }

    @Test
    @DisplayName("Dado mês anterior não conferido, Quando colaborador marcar ponto, Então bloqueia")
    void deveBloquearMarcacaoQuandoMesAnteriorNaoConferido() {
        LocalDate hoje = LocalDate.now();
        LocalDate mesAnterior = hoje.minusMonths(1);
        Mockito.when(conferenciaRepository.existsByInstituicao_Id(INSTITUICAO_ID)).thenReturn(true);
        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                INSTITUICAO_ID, hoje.getMonthValue(), hoje.getYear())).thenReturn(Optional.empty());
        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                INSTITUICAO_ID, mesAnterior.getMonthValue(), mesAnterior.getYear())).thenReturn(Optional.empty());

        Assertions.assertThrows(ExcecaoDeDominio.class,
                () -> servico.marcarPonto(INSTITUICAO_ID, CPF, NOME));
    }

    @Test
    void deveBloquearMarcacaoQuandoMesConferido() {
        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                INSTITUICAO_ID, LocalDate.now().getMonthValue(), LocalDate.now().getYear()))
                .thenReturn(Optional.of(new ConferenciaPontoMensal()));

        Assertions.assertThrows(ExcecaoDeDominio.class,
                () -> servico.marcarPonto(INSTITUICAO_ID, CPF, NOME));
    }

    @Test
    @DisplayName("Dado competência do mês atual, Quando RH conferir, Então bloqueia")
    void deveImpedirConferenciaDoMesAtual() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();
        Mockito.when(vinculoRepository.findByInstituicaoIdComDetalhes(INSTITUICAO_ID)).thenReturn(List.of());
        Mockito.when(registroRepository.findByInstituicao_IdAndDataRegistroBetweenOrderByNomeColaboradorAscDataRegistroAsc(
                Mockito.eq(INSTITUICAO_ID), Mockito.any(), Mockito.any())).thenReturn(List.of());

        Assertions.assertThrows(ExcecaoDeDominio.class,
                () -> servico.conferirMesRh(INSTITUICAO_ID, CPF, mes, ano));
    }

    @Test
    @DisplayName("Dado competência conferida sem integração financeira, Quando reabrir, Então remove conferência")
    void deveReabrirConferencia() {
        int mes = LocalDate.now().minusMonths(1).getMonthValue();
        int ano = LocalDate.now().minusMonths(1).getYear();
        ConferenciaPontoMensal conferencia = ConferenciaPontoMensal.builder()
                .mesCompetencia(mes)
                .anoCompetencia(ano)
                .build();
        Mockito.when(conferenciaRepository.findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
                INSTITUICAO_ID, mes, ano)).thenReturn(Optional.of(conferencia));
        Mockito.when(vinculoRepository.findByInstituicaoIdComDetalhes(INSTITUICAO_ID)).thenReturn(List.of());
        Mockito.when(registroRepository.findByInstituicao_IdAndDataRegistroBetweenOrderByNomeColaboradorAscDataRegistroAsc(
                Mockito.eq(INSTITUICAO_ID), Mockito.any(), Mockito.any())).thenReturn(List.of());

        servico.reabrirConferenciaRh(INSTITUICAO_ID, mes, ano);

        Mockito.verify(conferenciaRepository).delete(conferencia);
    }

    @Test
    @DisplayName("Dado registros em aberto, Quando RH conferir mês, Então falha")
    void deveImpedirConferenciaComRegistroAberto() {
        LocalDate mesPassado = LocalDate.now().minusMonths(1);
        int mes = mesPassado.getMonthValue();
        int ano = mesPassado.getYear();
        RegistroDiaPonto aberto = RegistroDiaPonto.builder()
                .cpfColaborador(CPF)
                .nomeColaborador(NOME)
                .dataRegistro(LocalDate.now())
                .horaEntrada(LocalDateTime.now().minusHours(2))
                .build();

        Mockito.when(vinculoRepository.findByInstituicaoIdComDetalhes(INSTITUICAO_ID)).thenReturn(List.of());
        Mockito.when(registroRepository.findByInstituicao_IdAndDataRegistroBetweenOrderByNomeColaboradorAscDataRegistroAsc(
                Mockito.eq(INSTITUICAO_ID), Mockito.any(), Mockito.any())).thenReturn(List.of(aberto));

        Assertions.assertThrows(ExcecaoDeDominio.class,
                () -> servico.conferirMesRh(INSTITUICAO_ID, CPF, mes, ano));
    }
}
