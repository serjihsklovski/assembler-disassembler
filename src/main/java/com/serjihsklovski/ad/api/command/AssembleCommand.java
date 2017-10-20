package com.serjihsklovski.ad.api.command;

import com.serjihsklovski.ad.component.assembler.AsmX86AssemblerImpl;
import com.serjihsklovski.ad.component.assembler.Assembler;
import com.serjihsklovski.ad.component.lexer.DefaultLexerImpl;
import com.serjihsklovski.ad.component.lexer.Lexer;
import com.serjihsklovski.ad.component.parser.AsmX86ParserImpl;
import com.serjihsklovski.ad.component.parser.Parser;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AssembleCommand implements Command {

    private Lexer lexer = new DefaultLexerImpl();
    private Parser parser = new AsmX86ParserImpl();
    private Assembler assembler = new AsmX86AssemblerImpl();

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
                    .map(line -> lexer.getLexemes(line))
                    .map(lexemes -> parser.parse(lexemes))
                    .map(operationTreeNode -> assembler.assemble(operationTreeNode))
                    .forEach(o -> {
                        try {
                            Files.write(Paths.get(output), o.getBytes(), StandardOpenOption.APPEND);
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
