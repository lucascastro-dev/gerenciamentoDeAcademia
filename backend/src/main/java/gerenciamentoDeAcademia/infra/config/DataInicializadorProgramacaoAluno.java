package gerenciamentoDeAcademia.infra.config;



import gerenciamentoDeAcademia.entidades.Aluno;

import gerenciamentoDeAcademia.entidades.Instituicao;

import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;

import gerenciamentoDeAcademia.enums.TipoItemProgramacao;

import gerenciamentoDeAcademia.repositorios.AlunoRepository;

import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;

import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.annotation.Profile;

import org.springframework.context.event.EventListener;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;
import java.time.LocalTime;



@Component

@Profile({"docker", "local"})

@ConditionalOnProperty(name = "app.seed.demo-enabled", havingValue = "true", matchIfMissing = true)

@RequiredArgsConstructor

public class DataInicializadorProgramacaoAluno {



    private static final Logger log = LoggerFactory.getLogger(DataInicializadorProgramacaoAluno.class);



    private final ItemProgramacaoAlunoRepository repository;

    private final AlunoRepository alunoRepository;

    private final InstituicaoRepository instituicaoRepository;



    @EventListener(ApplicationReadyEvent.class)

    @Transactional

    public void seedExemplo() {

        Aluno aluno = alunoRepository.findByCpf(DataInicializadorAlunoTeste.CPF_ALUNO_TESTE);

        Instituicao master = instituicaoRepository.findByCnpj("00000000000191");

        if (aluno == null || master == null) {

            return;

        }

        boolean jaTemAula = repository.findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(
                aluno.getCpf(), master.getId()).stream()
                .anyMatch(i -> "Judô — aula extra (portal)".equals(i.getTitulo()));
        if (jaTemAula) {
            return;
        }

        repository.save(ItemProgramacaoAluno.builder()
                .instituicao(master)
                .aluno(aluno)
                .tipo(TipoItemProgramacao.AULA)
                .titulo("Judô — aula extra (portal)")
                .descricao("Aula avulsa na Minha programação — complementa os cenários da grade.")
                .dataPrevista(LocalDate.now().plusDays(7))
                .horario("18:00-19:30")
                .horaInicio(LocalTime.of(18, 0))
                .horaFim(LocalTime.of(19, 30))
                .sala("Dojo 1")
                .build());

        log.info("Item de programação de exemplo criado para aluno de teste.");

    }

}

