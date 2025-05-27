package info.ejava.alamnr.assignment1.autorentals.logging;

import java.time.Duration;
import java.time.Instant;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import info.ejava.alamnr.assignment1.autorentals.logging.app.AppCommand;

public class LoggingEfficiencyNTest {

    @Nested
    @SpringBootTest
    @DirtiesContext
    @ActiveProfiles("app-debug")
    class when_trace_disabled {
        @Autowired
        CommandLineRunner appCommand;

        @Test
        void should_have_no_delay(){
            Assertions.assertTimeout(Duration.ofSeconds(2),()-> appCommand.run(),
                    "Too slow, check for unnecessary logging with app-debug profile ");
        }
    }

    @Disabled
    @Nested
    @SpringBootTest
    @DirtiesContext
    @ActiveProfiles("repo-only")
    class when_trace_enabled {
        @Autowired
        CommandLineRunner appCommand;

        @Test
        void should_have_delay() throws Exception {
            Instant startTime = Instant.now();
            appCommand.run();
            Duration executionTime = Duration.between(startTime, Instant.now());
            BDDAssertions.then(executionTime).as("too fast, check toString() an repo-only profile configuration")
                    .isGreaterThan(Duration.ofMillis(1500));
        }
    }

    
}
