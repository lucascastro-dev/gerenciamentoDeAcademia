package gerenciamentoDeAcademia.infra.config;



import gerenciamentoDeAcademia.entidades.Aluno;

import gerenciamentoDeAcademia.entidades.Instituicao;

import gerenciamentoDeAcademia.entidades.Turma;

import gerenciamentoDeAcademia.repositorios.AlunoRepository;

import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;

import gerenciamentoDeAcademia.repositorios.TurmaRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.annotation.Profile;

import org.springframework.context.event.EventListener;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;



import java.util.List;



/**

 * Associa turmas existentes sem instituição à instituição master e garante turma do aluno de teste do portal.

 */

@Component

@Profile({"docker", "local"})

@RequiredArgsConstructor

public class DataInicializadorVinculoTurmas {



    private static final Logger log = LoggerFactory.getLogger(DataInicializadorVinculoTurmas.class);

    private static final String CNPJ_MASTER = "00000000000191";



    private final TurmaRepository turmaRepository;

    private final InstituicaoRepository instituicaoRepository;

    private final AlunoRepository alunoRepository;



    @EventListener(ApplicationReadyEvent.class)

    @Transactional

    public void vincularTurmasEAlunoTeste() {

        Instituicao master = instituicaoRepository.findByCnpj(CNPJ_MASTER);

        if (master == null) {

            return;

        }



        List<Turma> semInstituicao = turmaRepository.findAll().stream()

                .filter(t -> t.getInstituicao() == null)

                .toList();

        for (Turma turma : semInstituicao) {

            turma.setInstituicao(master);

            turmaRepository.save(turma);

        }

        if (!semInstituicao.isEmpty()) {

            log.info("Vinculadas {} turma(s) à instituição master.", semInstituicao.size());

        }



        Aluno alunoTeste = alunoRepository.findByCpf(DataInicializadorAlunoTeste.CPF_ALUNO_TESTE);

        if (alunoTeste == null) {

            return;

        }



        boolean jaVinculado = turmaRepository.findByAlunos_CpfAndInstituicao_Id(

                alunoTeste.getCpf(), master.getId()).stream().findAny().isPresent();

        if (jaVinculado) {

            return;

        }



        Turma turmaPortal = turmaRepository.findAll().stream()

                .filter(t -> master.equals(t.getInstituicao()))

                .findFirst()

                .orElseGet(() -> {

                    Turma t = new Turma();

                    t.setInstituicao(master);

                    t.setHorario("18:00-19:30");
                    t.setHoraInicio(java.time.LocalTime.of(18, 0));
                    t.setHoraFim(java.time.LocalTime.of(19, 30));
                    t.setSala("Dojo 1");
                    t.setDias(List.of("Segunda", "Quarta"));
                    t.setModalidade("Judô");

                    return turmaRepository.save(t);

                });



        if (!turmaPortal.getAlunos().contains(alunoTeste)) {
            turmaPortal.getAlunos().add(alunoTeste);
            turmaRepository.save(turmaPortal);
        }

        log.info("Aluno de teste {} vinculado à turma {} da instituição master.", alunoTeste.getCpf(), turmaPortal.getId());

    }

}

