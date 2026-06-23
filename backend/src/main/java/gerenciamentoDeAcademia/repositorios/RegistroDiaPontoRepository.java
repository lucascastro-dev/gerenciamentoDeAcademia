package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.RegistroDiaPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroDiaPontoRepository extends JpaRepository<RegistroDiaPonto, Long> {

    Optional<RegistroDiaPonto> findByInstituicao_IdAndCpfColaboradorAndDataRegistro(
            Long instituicaoId, String cpfColaborador, LocalDate dataRegistro);

    List<RegistroDiaPonto> findByInstituicao_IdAndCpfColaboradorAndDataRegistroBetweenOrderByDataRegistroAsc(
            Long instituicaoId, String cpfColaborador, LocalDate inicio, LocalDate fim);

    List<RegistroDiaPonto> findByInstituicao_IdAndDataRegistroBetweenOrderByNomeColaboradorAscDataRegistroAsc(
            Long instituicaoId, LocalDate inicio, LocalDate fim);
}
