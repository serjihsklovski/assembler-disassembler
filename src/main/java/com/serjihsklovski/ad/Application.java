package com.serjihsklovski.ad;

import com.serjihsklovski.ad.api.command.CommandManager;
import org.apache.commons.cli.*;

import java.util.function.Consumer;

public class Application implements Consumer<String[]> {

    @Override
    public void accept(String[] args) {
        Options options = CommandManager.prepareOptions();
        CommandLine cmd = createCommandLine(options, args);
        CommandManager.forName(CommandManager.determineCommand(cmd)).execute(cmd);
    }

    private CommandLine createCommandLine(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(options, args);
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }
}
