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
@Table(name = "kafka_stack_processing", indexes = {
        @Index(name = "tsm_kksp_xid_index",  columnList="correlationId", unique = true),
})
public class KafkaStackProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    private String correlationId;

    private ZonedDateTime lastModification;

}
