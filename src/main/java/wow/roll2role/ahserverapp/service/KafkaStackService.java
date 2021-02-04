package wow.roll2role.ahserverapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import wow.roll2role.ahserverapp.model.KafkaStack;
import wow.roll2role.ahserverapp.repository.KafkaStackRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
public class KafkaStackService {

    public static final int UUIDS = 30;
    public static final int ENTITIES_TO_INSERT = 150;
    public static final int ENTITIES_PROCESSED_PER_THREAD = 25;

    @Autowired
    private final KafkaStackRepository kafkaStackRepository;

    @Autowired
    private final KafkaStackOrchestratorService kafkaStackOrchestratorService;

    public KafkaStackService(final KafkaStackRepository kafkaStackRepository,
                             final KafkaStackOrchestratorService kafkaStackOrchestratorService) {
        this.kafkaStackRepository = kafkaStackRepository;
        this.kafkaStackOrchestratorService = kafkaStackOrchestratorService;
    }

    @Transactional
    public void insertData() {
        final List<UUID> uuids = new ArrayList<>();
        IntStream.range(0, UUIDS).forEach(index -> {
            uuids.add(UUID.randomUUID());
        });

        IntStream.range(0, ENTITIES_TO_INSERT).forEach(index -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final KafkaStack kafkaStack =
                    KafkaStack.builder()
                            .status("NEW")
                            .counter(0L)
                            .correlationId(uuids.get(ThreadLocalRandom.current().nextInt(0, UUIDS)).toString())
                            .build();
            kafkaStackRepository.save(kafkaStack);
        });
    }

    public void processData() {
        final List<String> correlationIds = kafkaStackOrchestratorService.findDistinctByStatus("NEW",  PageRequest.of(0, ENTITIES_PROCESSED_PER_THREAD));
        Collections.shuffle(correlationIds);
        final AtomicInteger tried = new AtomicInteger(0);
        correlationIds.forEach(correlationId -> {
            tried.incrementAndGet();
            if (kafkaStackOrchestratorService.lockCorrelationId(correlationId)) {
                final List<Long> kafkaStacks = kafkaStackOrchestratorService.findAllByCorrelationId(correlationId);
                kafkaStacks.forEach(kafkaStackOrchestratorService::updateStatusAndDateAndCounter);
                kafkaStackOrchestratorService.releaseLock(correlationId);
            } else {
                kafkaStackOrchestratorService.releaseLockIfTooOld(correlationId);
            }
        });
        System.out.println("Processed: " + tried.get());
    }

}
