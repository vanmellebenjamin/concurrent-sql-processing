package wow.roll2role.ahserverapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import wow.roll2role.ahserverapp.service.KafkaStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class AhServerAppApplication {

	public static void main(final String[] args) throws InterruptedException {
		if (args.length != 0 && args[0].equalsIgnoreCase("worker")) {
			worker(args);
			System.exit(0);
		} else if (args.length != 0 && args[0].equalsIgnoreCase("insert")){
			insert(args);
			System.exit(0);
		} else {
			final List<Thread> threads = new ArrayList<>();
			IntStream.range(0, 3).forEach(index -> {
				threads.add(new Thread(() -> insert(args)));
			});
			threads.forEach(Thread::start);
			Thread.sleep(30_000);
			worker(args);
			for (Thread thread : threads) {
				thread.join();
			}
			Thread.sleep(120_000);
			System.exit(0);
		}
	}

	private static void insert(String[] args) {
		final ConfigurableApplicationContext applicationContext = SpringApplication.run(AhServerAppApplication.class, args);
		final KafkaStackService kafkaStackService = applicationContext.getBean(KafkaStackService.class);
		kafkaStackService.insertData();
	}

	private static void worker(String[] args) {
		int threads = 8;
		if (args.length > 1) {
			threads = Integer.parseInt(args[1]);
		}
		final List<Thread> threadsList = new ArrayList<>();

		IntStream.range(0, threads).forEach(index -> {
			threadsList.add(new Thread(() -> {
				final ConfigurableApplicationContext applicationContext = SpringApplication.run(AhServerAppApplication.class, args);
				final KafkaStackService kafkaStackService = applicationContext.getBean(KafkaStackService.class);
				kafkaStackService.processData();
			}));
		});
		threadsList.forEach(Thread::start);

		threadsList.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		});
	}

}
