package com.serjihsklovski.ad.api.command;

import org.apache.commons.cli.CommandLine;

public class DisassembleCommand implements Command {

    @Override
    public void execute(CommandLine cmd) {
        System.out.println("DisassembleCommand!");
        System.out.println(cmd.getOptionValue("input"));
        System.out.println(cmd.getOptionValue("output"));
    }
}
