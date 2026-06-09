package gerenciamentoDeAcademia.auditoria;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditoriaRevisionListener.class)
@Getter
@Setter
public class RevisaoAuditoria {

    @Id
    @GeneratedValue
    @RevisionNumber
    @Column(name = "rev")
    private Long rev;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private Long revtstmp;

    @Column(name = "usuario_login", length = 100)
    private String usuarioLogin;
}
