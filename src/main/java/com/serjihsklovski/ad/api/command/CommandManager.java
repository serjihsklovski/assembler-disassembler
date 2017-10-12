package com.serjihsklovski.ad.api.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class CommandManager {

    private static final String EXCEPTION_MSG_ONLY_ONE_COMMAND = "you cannot choose more than 1 command!";
    private static final String EXCEPTION_MSG_COMMAND_NOT_EXIST = "this command does not exist!";
    private static Map<String, Class<? extends Command>> commands;

    public static Command forName(String commandName) {
        try {
            return Optional.ofNullable(commands.getOrDefault(commandName, null))
                    .orElseThrow(() -> new RuntimeException(EXCEPTION_MSG_COMMAND_NOT_EXIST))
                    .newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static String determineCommand(CommandLine cmd) {
        String command = null;

        for (String c : commands.keySet()) {
            if (cmd.hasOption(c)) {
                if (command != null) {
                    throw new RuntimeException(EXCEPTION_MSG_ONLY_ONE_COMMAND);
                } else {
                    command = c;
                }
            }
        }

        return command;
    }

    private static void registerCommands() {
        Properties commandsProperties = new Properties();

        try {
            commandsProperties.load(new FileReader("./src/main/resources/commands.properties"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        commandsProperties.stringPropertyNames().forEach(commandName -> {
            String commandClassName = commandsProperties.getProperty(commandName);

            try {
                Class commandClass = Class.forName(commandClassName);

                if (Command.class.isAssignableFrom(commandClass)) {
                    commands.put(commandName, commandClass);
                }
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        });
    }

    public static Options prepareOptions() {
        Options options = new Options();

        commands.keySet().stream()
                .map(command -> new Option(command, false, ""))
                .forEach(options::addOption);

        options.addOption("input", true, "");
        options.addOption("output", true, "");

        return options;
    }

    static {
        commands = new HashMap<>();
        registerCommands();
    }
}
