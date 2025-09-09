package yunrry.flik.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import yunrry.flik.batch.controller.BatchController;

import java.util.ArrayList;
import java.util.Arrays;

@EnableScheduling
@SpringBootApplication
public class FlikBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlikBatchApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("=== Application Ready ===");
    }
}