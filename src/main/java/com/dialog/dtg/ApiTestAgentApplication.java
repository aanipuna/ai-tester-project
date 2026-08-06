package com.dialog.dtg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dialog.dtg.core.StartupValidationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiTestAgentApplication {

    private static final Set<String> CLI_COMMANDS = new HashSet<>(Arrays.asList("generate", "run", "report"));

    private final StartupValidationService startupValidationService;

    public ApiTestAgentApplication(StartupValidationService startupValidationService) {
        this.startupValidationService = startupValidationService;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApiTestAgentApplication.class);
        if (isCliInvocation(args)) {
            application.setWebApplicationType(WebApplicationType.NONE);
            application.setDefaultProperties(java.util.Map.of("spring.main.web-application-type", "none"));
        }
        application.run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        startupValidationService.validateAndInitialize();
    }

    static boolean isCliInvocation(String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }

        for (String arg : args) {
            if (arg == null || arg.isBlank()) {
                continue;
            }
            if (arg.startsWith("-")) {
                continue;
            }
            return CLI_COMMANDS.contains(arg.toLowerCase());
        }
        return false;
    }
}
