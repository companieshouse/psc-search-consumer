package uk.gov.companieshouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.avro.util.ClassSecurityValidator;

@SpringBootApplication
public class Application {

    public static final String NAMESPACE = "psc-search-consumer";

    public static void main(String[] args) {
        ClassSecurityValidator.setGlobal(clazz -> true);
        SpringApplication.run(Application.class, args);
    }
}
