package wow.roll2role.ahserverapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kafka_stack", indexes = {
        @Index(name = "tsm_kks_xid_index",  columnList="correlationId"),
        @Index(name = "tsm_kks_status_index",  columnList="status"),
})
public class KafkaStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    private String status;

    private Long counter;

    private String correlationId;

    private ZonedDateTime lastModification;

}
