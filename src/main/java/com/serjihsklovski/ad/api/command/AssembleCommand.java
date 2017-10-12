package com.serjihsklovski.ad.api.command;

import org.apache.commons.cli.CommandLine;

public class AssembleCommand implements Command {

    @Override
    public void execute(CommandLine cmd) {
        System.out.println("AssembleCommand!");
        System.out.println(cmd.getOptionValue("input"));
        System.out.println(cmd.getOptionValue("output"));
    }
}
