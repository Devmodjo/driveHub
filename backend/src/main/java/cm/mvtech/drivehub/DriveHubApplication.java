package cm.mvtech.drivehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling            // tâches planifiées (ex : purge des jetons révoqués)
@ConfigurationPropertiesScan   // active les classes @ConfigurationProperties (ex : PaymentProperties)
@SpringBootApplication
public class DriveHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriveHubApplication.class, args);
	}

}
