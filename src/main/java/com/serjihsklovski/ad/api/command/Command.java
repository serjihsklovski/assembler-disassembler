package com.serjihsklovski.ad.api.command;

import org.apache.commons.cli.CommandLine;

public interface Command {

    void execute(CommandLine cmd);
}
