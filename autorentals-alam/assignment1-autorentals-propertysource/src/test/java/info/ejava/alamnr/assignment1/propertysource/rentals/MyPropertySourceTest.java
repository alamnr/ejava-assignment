package info.ejava.alamnr.assignment1.propertysource.rentals;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

import info.ejava.assignments.propertysource.autorentals.PropertyCheck;
import info.ejava.assignments.propertysource.autorentals.PropertySourceTest;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

//we will cover testing in a future topic, very soon
//@Disabled //enable when ready to start assignment
public class MyPropertySourceTest //extends PropertySourceTest 
{

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
        private PropertyCheck props;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("(default value)");
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-default.properties");
            softly.then(props.getDbUrl()).as("dbUrl").isEqualTo("mongodb://defaultUser:defaultPass@defaulthost:27027/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Test 
        void has_expected_sources_1 () throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("(default value)");
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-default.properties");
            softly.then(props.getDbUrl()).as("dbUrl").isEqualTo("mongodb://defaultUser:defaultPass@defaulthost:27027/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Autowired
        ConfigurableEnvironment configurableEnvironment;        

        @Test
        void sources_have_unique_properties() {
            then_sources_have_unique_properties(configurableEnvironment);
        }
    }   

    @Nested
    @SpringBootTest(properties = {"spring.config.location="+CONFIG_LOCATION,
                                    // need to set the profiles using the property
                                    // so that property injected matches the command line
                                    "spring.profiles.active=dev,dev1"})
    // otherwise  we normally would have acivated using @ActiveProfiles
    //@ActiveProfiles("dev","dev1")
    class dev_dev1_profiles {

        @Autowired
        private PropertyCheck props;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("dev,dev1");
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-dev1.properties");
            softly.then(props.getDbUrl()).as("debUrl").isEqualTo("mongodb://devUser:dev1pass@127.0.0.1:33027/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Autowired
        ConfigurableEnvironment configurableEnvironment;
        @Test
        void sources_have_unique_properties(){
            then_sources_have_unique_properties(configurableEnvironment);
        }
    }

    @Nested
    @SpringBootTest(properties = {"spring.config.location="+CONFIG_LOCATION,
                            // need to set the profiles using the property
                            // so that property injected matches the command line
                            "spring.profiles.active=dev,dev2"})
    // otherwise we normally would have activated using @ActiveProfiles
    //@ActiveProfiles("dev,dev2")
    class dev_dev2_profiles {
        @Autowired
        private PropertyCheck props;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocation").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-dev2.properties");
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("dev,dev2");
            softly.then(props.getDbUrl()).as("dbUrl").isEqualTo("mongodb://devUser:dev2pass@127.0.0.1:44027/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Autowired
        ConfigurableEnvironment configurableEnvironment;

        @Test
        void sources_have_unique_properties() {
            then_sources_have_unique_properties(configurableEnvironment);
        }
    }

    @Nested
    @SpringBootTest(properties = {"spring.config.location="+CONFIG_LOCATION,
                                "spring.profiles.active=prd,site1"})
    class prd_site1_profiles {
        @Autowired
        private PropertyCheck props;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocation").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-site1.properties");
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("prd,site1");
            softly.then(props.getDbUrl()).as("dbUrl").isEqualTo("mongodb://prdUser:site1pass@db.site1.net:27017/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Autowired
        ConfigurableEnvironment configurableEnvironment;

        @Test
        void sources_have_unique_properties() {
            then_sources_have_unique_properties(configurableEnvironment);
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.config.location="+CONFIG_LOCATION,
                    "spring.profiles.active=prd,site2"})
    class prd_site2_profiles {
        @Autowired
        private PropertyCheck props;

        @Test
        void has_expected_sources() throws Exception {
            BDDSoftAssertions softly = new BDDSoftAssertions();
            softly.then(props.getConfigName()).as("configName").isEqualTo("(default value)");
            softly.then(props.getConfigLocations()).as("configLocations").isEqualTo(CONFIG_LOCATION);
            softly.then(props.getProfilesActive()).as("profilesActive").isEqualTo("prd,site2");
            softly.then(props.getPrioritySource()).as("prioritySource").isEqualTo("application-site2.yml");
            softly.then(props.getDbUrl()).as("dbUrl").isEqualTo("mongodb://prdUser:site2pass@db.site2.net:27017/test?authSource=admin");
            props.run();
            softly.assertAll();
        }

        @Autowired
        ConfigurableEnvironment configurableEnvironment;
        @Test
        void sources_have_unique_properties() {
            then_sources_have_unique_properties(configurableEnvironment);
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.config.location="+CONFIG_LOCATION,
            "spring.profiles.active=dev,dev1,dev2"})
    class dev_dev1_dev2 {
        @Autowired
        ConfigurableEnvironment configurableEnvironment;
        @Test
        void sources_have_unique_properties() {
            //then_sources_have_unique_properties(configurableEnvironment);
        }
    }

    @Nested
    @SpringBootTest(properties = { "spring.config.location="+CONFIG_LOCATION,
            "spring.profiles.active=prd,site1,site2"})
    class prd_site1_site2 {
        @Autowired
        ConfigurableEnvironment configurableEnvironment;
        @Test
        void sources_have_unique_properties() {
          //  then_sources_have_unique_properties(configurableEnvironment);
        }
    }

}


