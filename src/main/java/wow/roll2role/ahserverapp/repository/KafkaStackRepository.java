package wow.roll2role.ahserverapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import wow.roll2role.ahserverapp.model.KafkaStack;

import java.util.List;

@Repository
public interface KafkaStackRepository extends JpaRepository<KafkaStack, Long> {

    @Query("SELECT DISTINCT s.correlationId FROM KafkaStack s WHERE s.status = :status")
    List<String> findDistinctByStatus(final String status, final Pageable pageable);

    @Query("SELECT s.id FROM KafkaStack s WHERE s.correlationId = :correlationId AND status = 'NEW' ORDER BY id ASC")
    List<Long> findAllByCorrelationId(final String correlationId);

}