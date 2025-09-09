package yunrry.flik.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FlikBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlikBatchApplication.class, args);
    }

}
