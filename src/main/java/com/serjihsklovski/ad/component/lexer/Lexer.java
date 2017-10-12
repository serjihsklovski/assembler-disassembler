package com.serjihsklovski.ad.component.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Lexer {

    String DELIMITERS = " \n\t`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

    default List<Character> getDelimiters() {
        return DELIMITERS.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
    }

    default String getEndLexeme() {
        return "\n";
    }

    default boolean addEndLexeme() {
        return true;
    }

    default boolean isDelimiter(Character c) {
        return getDelimiters().stream()
                .anyMatch(character -> character.equals(c));
    }

    default List<String> getLexemes(String src) {
        List<String> lexemes = new ArrayList<>();
        StringBuilder lexeme = new StringBuilder();

        Consumer<StringBuilder> flushLexeme = lex -> {
            lexemes.add(lex.toString());
            lex.delete(0, lex.length());
        };

        Function<StringBuilder, Boolean> isLexemeEmpty = lex -> lex.length() == 0;

        Consumer<StringBuilder> flushIfLexemeNotEmpty = lex -> {
            if (!isLexemeEmpty.apply(lex)) {
                flushLexeme.accept(lex);
            }
        };

        for (char c : src.toCharArray()) {
            if (isDelimiter(c)) {
                flushIfLexemeNotEmpty.accept(lexeme);
                lexemes.add(String.valueOf(c));
            } else {
                lexeme.append(c);
            }
        }

        flushIfLexemeNotEmpty.accept(lexeme);

        if (addEndLexeme()) {
            lexemes.add(getEndLexeme());
        }

        return lexemes;
    }
}
