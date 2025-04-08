package info.ejava.assignments.propertysource.autorentals;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PropertyCheck implements CommandLineRunner {

    @Value("${spring.config.name:(default value)}") String configName;
    @Value("${spring.config.location:(default value)}") String configLocations;
    @Value("${spring.profiles.active:(default value)}") String profilesActive;

    @Value("${rentals.priority.source:not assigned}") String prioritySource;
    @Value("${rentals.db.url:not assigned}") String dbUrl;


    public String getConfigName() {
        return configName;
    }

    public String getConfigLocations() {
        return configLocations;
    }

    public String getProfilesActive() {
        return profilesActive;
    }

    public String getPrioritySource() {
        return prioritySource;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    @Override
    public void run(String... args) throws Exception {
        String settings = String.format("\nconfigName=%s\nconfigLocations=%s\nprofilesActive=%s\nprioritySource=%s",
                configName,
                configLocations,
                profilesActive,
                prioritySource);
        System.out.println(settings);
        String msg = String.format("Rentals has started\ndbUrl=%s",
                dbUrl);
        System.out.println(msg);

    }


    
}
