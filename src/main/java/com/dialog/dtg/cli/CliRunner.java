package com.dialog.dtg.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CliRunner implements ApplicationRunner {

    private final GenerateCommand generateCommand;
    private final RunCommand runCommand;
    private final ReportCommand reportCommand;

    public CliRunner(GenerateCommand generateCommand, RunCommand runCommand, ReportCommand reportCommand) {
        this.generateCommand = generateCommand;
        this.runCommand = runCommand;
        this.reportCommand = reportCommand;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] sourceArgs = args.getSourceArgs();
        if (sourceArgs == null || sourceArgs.length == 0) {
            return;
        }

        // Skip if first arg is a Spring Boot property (--key=value), not a CLI command
        String command = sourceArgs[0];
        if (command.startsWith("-")) {
            return;
        }

        int exitCode = switch (command) {
            case "generate" -> generateCommand.run(sourceArgs);
            case "run" -> runCommand.run(sourceArgs);
            case "report" -> reportCommand.run(sourceArgs);
            default -> 2;
        };

        if (exitCode != 0) {
            throw new IllegalStateException("CLI command failed with exit code: " + exitCode);
        }
    }
}
