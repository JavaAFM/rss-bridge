package org.example.parser;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.parser.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@AllArgsConstructor
@Slf4j
public class ParserApplication {
	private final TengriService tengriService;
	// private final KazTagService kaztagService;
	private final AzattyqService azattyqService;
	private final OrdaService ordaService;
	private final ZakonService zakonService;
	private final UpdateDBService updateDBService;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@PostConstruct
	public void startParserLoop() {
		executorService.submit(() -> {
			while (true) {
				try {
					log.info("Starting parsing cycle...");

					tengriService.parse();
					azattyqService.parse();
					ordaService.parse();
					zakonService.parse();

					log.info("Cleaning old news...");
					updateDBService.deleteNews3Month();

					log.info("Sleeping for 60 seconds...");
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					log.warn("Parsing loop interrupted, shutting down.");
					Thread.currentThread().interrupt();
					break;
				} catch (Exception e) {
					log.error("Error occurred during parsing:", e);
				}
			}
		});
	}

	public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}
}
