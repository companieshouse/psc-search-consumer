package uk.gov.companieshouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String NAMESPACE = "psc-search-consumer";

    static {
        System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "*");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
