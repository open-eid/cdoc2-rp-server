package ee.cyber.cdoc2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@ConfigurationPropertiesScan
@EnableScheduling
public final class Cdoc2RpServerApplication {
    private Cdoc2RpServerApplication() {

    }

    public static void main(String[] args) {
        SpringApplication.run(Cdoc2RpServerApplication.class, args);
    }
}
