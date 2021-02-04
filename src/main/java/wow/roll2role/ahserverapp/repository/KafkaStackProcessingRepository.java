package wow.roll2role.ahserverapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wow.roll2role.ahserverapp.model.KafkaStackProcessing;

import java.util.Optional;


@Repository
public interface KafkaStackProcessingRepository extends JpaRepository<KafkaStackProcessing, Long> {

    Optional<KafkaStackProcessing> findByCorrelationId(String correlationId);

}