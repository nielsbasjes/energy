/*
 * Energy readers and parsers toolkit
 * Copyright (C) 2019-2023 Niels Basjes
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package nl.basjes.energy.sunspec;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TestParserAgainstAllFiles {

    @Parameters(name = "Test {index} -> Parse: \"{0}\"")
    public static Iterable<String> data() throws IOException {
        Pattern wantedFilesPattern = Pattern.compile("^.*/smdx_[0-9]+\\.xml");

        try (Stream<Path> walk = Files.walk(Paths.get("src/main/resources/models/smdx/"))) {
            return walk
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(name -> wantedFilesPattern.matcher(name).matches())
                .sorted()
                .collect(Collectors.toList());
        }
    }

    // CHECKSTYLE.OFF: VisibilityModifier doesn't work like that for @Parameter variables
    @Parameterized.Parameter
    public String fileName;
    // CHECKSTYLE.ON

    @Test
    public void ensureErrorFreeParsing() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));

        CodePointCharStream input  = CharStreams.fromString(content);
        Lexer               lexer  = new SunspecModelLexer(input);
        CommonTokenStream   tokens = new CommonTokenStream(lexer);
        SunspecModelParser<Void>  parser = new SunspecModelParser<>(tokens);

        ErrorCounter errorListener = new ErrorCounter();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        // Parse the tree
        parser.sunSpecModels();

        assertEquals(0, errorListener.errors);
    }

    public static class ErrorCounter implements ANTLRErrorListener {
        int errors = 0;

        @Override
        public void syntaxError(Recognizer<?, ?> r, Object o, int i, int i1, String s, RecognitionException e) {
            errors++;
        }

        @Override public void reportAmbiguity(Parser p, DFA d, int i, int j, boolean k, BitSet s, ATNConfigSet a){}
        @Override public void reportAttemptingFullContext(Parser p, DFA d, int i, int j, BitSet k, ATNConfigSet a){}
        @Override public void reportContextSensitivity(Parser p, DFA d, int i, int j, int k, ATNConfigSet a){}

    }
}
