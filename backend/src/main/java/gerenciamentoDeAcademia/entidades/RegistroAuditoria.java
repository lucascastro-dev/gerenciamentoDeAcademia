package gerenciamentoDeAcademia.entidades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_auditoria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 100)
    private String usuarioLogin;

    @Column(nullable = false, length = 80)
    private String acao;

    @Column(nullable = false, length = 80)
    private String entidade;

    @Column(length = 100)
    private String identificador;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(length = 45)
    private String enderecoIp;
}
