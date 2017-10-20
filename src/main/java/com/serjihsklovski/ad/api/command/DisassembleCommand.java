package com.serjihsklovski.ad.api.command;

import com.serjihsklovski.ad.component.disassembler.AsmX86DisassemblerImpl;
import com.serjihsklovski.ad.component.disassembler.Disassembler;
import com.serjihsklovski.ad.component.parser.AsmX86ParserImpl;
import com.serjihsklovski.ad.component.parser.Parser;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DisassembleCommand implements Command {

    private Parser parser = new AsmX86ParserImpl();
    private Disassembler disassembler = new AsmX86DisassemblerImpl();

    @Override
    public void execute(CommandLine cmd) {
        String input = cmd.getOptionValue("input");
        String output = cmd.getOptionValue("output");

        try {
            if (Files.exists(Paths.get(output))) {
                Files.delete(Paths.get(output));
            }
            Files.createFile(Paths.get(output));

            Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8).stream()
                    .flatMap(line -> disassembler.disassemble(line).stream())
                    .map(operationTreeNode -> parser.parse(operationTreeNode))
                    .map(source -> source + "\n")
                    .forEach(source -> {
                        try {
                            Files.write(Paths.get(output), source.getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    });
        } catch (IOException ioe) {
            ioe.printStackTrace();

            try {
                Files.delete(Paths.get(output));
            } catch (IOException ignored) {
            }
        } catch (RuntimeException re) {
            System.out.println("Error: " + re.getMessage());

            try {
                Files.delete(Paths.get(output));
            } catch (IOException ignored) {
            }
        }
    }
}
