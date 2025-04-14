package info.ejava.alamnr.assignment1.autorentals.logging;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

/**
 * This test verifies which component was selected to be injected.
 * It uses relection to get around the fact that it does not know the
 * solution's package and classnames -- just obvious patterns.
 *
 * We will cover testing soon and reflection several weeks after that
 */

 
public class LoggingNTest {


    @ExtendWith(OutputCaptureExtension.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class TestBase {

        @Autowired
        private CommandLineRunner appCommand;
        protected CapturedOutput output;

        @BeforeAll
        void run(CapturedOutput output) throws Exception {
            this.output = output;
            appCommand.run();
        }

    }

    @Nested
    @SpringBootTest(properties = "spring.main.banner-mode=LOG")
    class root_logger extends TestBase {
        @Test
        void has_no_output() throws Exception   {
              
            BDDAssertions.then(output.getOut()).as("root logger not off").isBlank();
            if(output.isEmpty())
                System.out.println("output value is empty or blank");
            else
                System.out.println("output value - "+output);   
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.main.banner-mode=off")
    @ActiveProfiles("app-debug")
    class app_debug_profile extends TestBase{
        @Test
        void logs_no_trace() throws Exception {
            
            BDDAssertions.then(output.getOut()).as("Trace logs found").doesNotContain("TRACE");

            System.out.println("====================== output value - " +output.getOut());
        }

        @Test
        void xy_info_logs_included(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("X.Y INFO is missing").containsPattern("INFO.+X\\.Y");
            System.out.println("====================== output value - " +output);
        }

        @Test
        void xy_info_logs_included_1() throws Exception {
            BDDAssertions.then(output).as("X.Y INFO is missing").containsPattern("INFO.+X\\.Y");
            System.out.println("====================== output value - " +output);
        }

        @Test
        void helper_debug_logs_included(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("HrlperImpl debug missing").containsPattern("DEBUG.+HelperImpl");
        }
       
        @Test
        void svc_info_logs_included(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("Service Impl info missing").containsPattern("INFO.+ServiceImpl");
        }

        @Test
        void repo_all_trace_logs_missing(CapturedOutput output)  throws Exception {
            BDDAssertions.then(output).as("repo impl trace log found").doesNotContainPattern("RepositoryImpl");
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.main.banner-mode=off")
    @ActiveProfiles("repo-only")
    class repo_only extends TestBase {
        @Test
        void contains_trace() throws Exception {
            BDDAssertions.then(output.getOut()).as("missing TRACE statements").contains("TRACE");
        }
        @Test
        void repo_trace_logs_included(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("RepoImpl TRACE missing").containsPattern("TRACE.+RepositoryImpl");
        }
        @Test
        void repo_has_only_trace(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("unexpected RepoImpl DEBUG logs").doesNotContainPattern("DEBUG.+RepositoryImpl");
            BDDAssertions.then(output).as("unexpected RepoImpl INFO logs").doesNotContainPattern("INFO.+RepositoryImpl");
        }

        @Test
        void xy_no_logs(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("unexpected X.Y TRACE logs").doesNotContainPattern("TRACE.+X\\.Y");
            BDDAssertions.then(output).as("unexpected X.Y DEBUG logs").doesNotContainPattern("DEBUG.+X\\.Y");
            BDDAssertions.then(output).as("unexpected X.Y INFO logs").doesNotContainPattern("INFO.+X\\.Y");
        }
        @Test
        void helper_no_logs(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("unexpected HelperImpl TRACE logs").doesNotContainPattern("TRACE.+HelperImpl");
            BDDAssertions.then(output).as("unexpected HelperImpl DEBUG logs").doesNotContainPattern("DEBUG.+HelperImpl");
            BDDAssertions.then(output).as("unexpected HelperImpl INFO logs").doesNotContainPattern("INFO.+HelperImpl");
        }
        @Test
        void svc_no_logs(CapturedOutput output) throws Exception {
            BDDAssertions.then(output).as("unexpected ServiceImpl TRACE logs").doesNotContainPattern("TRACE.+ServiceImpl");
            BDDAssertions.then(output).as("unexpected ServiceImpl DEBUG logs").doesNotContainPattern("DEBUG.+ServiceImpl");
            BDDAssertions.then(output).as("unexpected ServiceImpl INFO logs").doesNotContainPattern("INFO.+ServiceImpl");
        }
    }

    }



