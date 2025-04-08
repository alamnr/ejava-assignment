package info.ejava.assignments.propertysource.autorentals;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.assertj.core.api.BDDSoftAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootConfiguration
@EnableAutoConfiguration
public class PropertySourceTest {

    static final String CONFIG_LOCATION="classpath:/,optional:file:src/locations/";
    static final List<String> GENERIC_FILES = List.of(
            "application.properties", "application-default.properties", "application-dev.yml", "application-prd.properties"
    );
    static final List<String> CREDENTIAL_FILES = List.of(
            "application-dev1.properties","application-dev2.properties","application-site1.properties", "application-site2.yml"
    );

    void then_sources_have_unique_properties(ConfigurableEnvironment configurableEnvironment) {
        MultiValueMap<String, String> propertyOrigins = new LinkedMultiValueMap<>();
        Pattern propertySourceFilePattern = Pattern.compile("\\[(.*)\\]");
        configurableEnvironment.getPropertySources().stream()
                .filter(ps -> ps instanceof OriginTrackedMapPropertySource)
                .map(ps -> (OriginTrackedMapPropertySource) ps)
                .forEach(ps->{
                    Matcher matcher = propertySourceFilePattern.matcher(ps.getName());
                    if (matcher.find()) {
                        //Intellij is copying src/locations into target/test-classes -- causing them to be in classpath
                        String sourcePath = matcher.group(1);
                        String sourceName = sourcePath.replaceAll("src/locations/", "");
                        URL uri = this.getClass().getResource("/" + sourceName);
                        //any file in file path or in classpath but not contributed by test-classes
                        if (ps.getName().contains("optional:file:") || null==uri || !uri.toExternalForm().contains("test-classes")) {
                            for (Map.Entry<String, Object> entry : ps.getSource().entrySet()) {
                                String propertyName = entry.getKey();
                                String propertyValue = entry.getValue() == null ? "" : entry.getValue().toString();

                                String keyValue = propertyName + "=" + propertyValue;
                                propertyOrigins.add(keyValue, sourcePath);
                            }
                        }
                    }
                });

        BDDSoftAssertions softly = new BDDSoftAssertions();
        for (Map.Entry<String, List<String>> entry: propertyOrigins.entrySet()) {
            String propertySetting = entry.getKey();
            HashSet<String> distinctSources = new HashSet<>(entry.getValue());
            softly.then(distinctSources).as(propertySetting).hasSize(1);
        }
        softly.assertAll();
    }

    @Nested
    @SpringBootTest(properties = { "spring.config.location=" + CONFIG_LOCATION})
    class no_profile {
        @Value("${spring.config.location:(default value)}") String configLocations;
        @Autowired
        private PropertyCheck propertyCheck;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(propertyCheck.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(propertyCheck.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(propertyCheck.getProfilesActive()).as("profilesActive").isEqualTo("(default value)");
            softly.then(propertyCheck.getPrioritySource()).as("prioritySource").isEqualTo("application-default.properties");
            softly.then(propertyCheck.getDbUrl()).as("dbUrl").isEqualTo("mongodb://defaultUser:defaultPass@defaulthost:27027/test?authSource=admin");
            propertyCheck.run();
            softly.assertAll();
        }

        @Test 
        void has_expected_sources_1 () throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(propertyCheck.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(propertyCheck.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(propertyCheck.getProfilesActive()).as("profilesActive").isEqualTo("(default value)");
            softly.then(propertyCheck.getPrioritySource()).as("prioritySource").isEqualTo("application-default.properties");
            softly.then(propertyCheck.getDbUrl()).as("dbUrl").isEqualTo("mongodb://defaultUser:defaultPass@defaulthost:27027/test?authSource=admin");
            propertyCheck.run();
            softly.assertAll();
        }
    }   
}
