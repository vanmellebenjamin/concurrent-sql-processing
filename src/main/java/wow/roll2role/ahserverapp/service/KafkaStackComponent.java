package wow.roll2role.ahserverapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import wow.roll2role.ahserverapp.model.KafkaStack;
import wow.roll2role.ahserverapp.model.KafkaStackProcessing;
import wow.roll2role.ahserverapp.repository.KafkaStackProcessingRepository;
import wow.roll2role.ahserverapp.repository.KafkaStackRepository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class KafkaStackComponent {

    @Autowired
    private final KafkaStackProcessingRepository kafkaStackProcessingRepository;

    @Autowired
    private final KafkaStackRepository kafkaStackRepository;

    public KafkaStackComponent(final KafkaStackProcessingRepository kafkaStackProcessingRepository,
                               final KafkaStackRepository kafkaStackRepository) {
        this.kafkaStackProcessingRepository = kafkaStackProcessingRepository;
        this.kafkaStackRepository = kafkaStackRepository;
    }

    public boolean lockCorrelationId(final String correlationId) {
        try {
            kafkaStackProcessingRepository
                    .save(KafkaStackProcessing.builder().correlationId(correlationId).lastModification(ZonedDateTime.now()).build());
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            System.out.println(String.format("%s already locked", correlationId));
            return false;
        }
        return true;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateStatusAndDateAndCounter(final Long id) {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final KafkaStack kafkaStack = kafkaStackRepository.findById(id).orElseThrow();
        kafkaStack.setCounter(kafkaStack.getCounter() == null ? 1 : kafkaStack.getCounter() + 1);
        kafkaStack.setLastModification(ZonedDateTime.now());
        kafkaStack.setStatus("PROCESSED");
    }

    public void releaseLockIfTooOld(final String correlationId) {
        kafkaStackProcessingRepository.findByCorrelationId(correlationId).ifPresent(kafkaStackProcessing -> {
            if (kafkaStackProcessing.getLastModification().isBefore(ZonedDateTime.now().minusMinutes(1))) {
                try {
                    kafkaStackProcessingRepository.deleteById(kafkaStackProcessing.getId());
                } catch (ObjectOptimisticLockingFailureException ex) {
                    System.out.println("Already deleted");
                }
            }
        });
    }

    public void releaseLock(final String correlationId) {
        kafkaStackProcessingRepository.findByCorrelationId(correlationId)
                .ifPresent(kafkaStackProcessingRepository::delete);
    }

    public List<String> findDistinctByStatus(final String correlationId, final PageRequest pageRequest) {
        return kafkaStackRepository.findDistinctByStatus(correlationId, pageRequest);
    }

    public List<Long> findAllByCorrelationId(final String correlationId) {
        return kafkaStackRepository.findAllByCorrelationId(correlationId);
    }
}
