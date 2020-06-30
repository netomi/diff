/*
 * Copyright 2013 Thomas Neidhart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netomi.jdiffutils.format;

import java.io.PrintStream;
import java.util.List;

import org.netomi.jdiffutils.Patch;
import org.netomi.jdiffutils.algorithm.ReplacementsFinder;

/**
 * Output formatter for the classic diff format.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Diff">Diff Format (Wikipedia)</a>
 * @author Thomas Neidhart
 */
public class ClassicDiffFormatter implements OutputFormatter {

    @Override
    public void format(final Patch patch, final PrintStream ps) {
        patch.getEditScript().visit(new ReplacementsFinder<String>(new MyReplacementsHandler(ps)));
    }

    private static class MyReplacementsHandler extends OutputReplacementsHandler {

        public MyReplacementsHandler(final PrintStream ps) {
            super(ps);
        }

        @Override
        public void handleReplacement(final List<String> from, final List<String> to) {
            if (from.size() == 0) {
                ps.print(inputLine - 1);
                ps.print('a');
                ps.println(getLineInfo(outputLine, to.size()));
                handleInsert(to);
                outputLine += to.size();
            } else if (to.size() == 0) {
                ps.print(getLineInfo(inputLine, from.size()));
                ps.print('d');
                ps.println(outputLine - 1);
                handleDelete(from);
                inputLine += from.size();
            } else {
                ps.print(getLineInfo(inputLine, from.size()));
                ps.print('c');
                ps.println(getLineInfo(outputLine, to.size()));
                handleDelete(from);
                ps.println("---");
                handleInsert(to);

                inputLine += from.size();
                outputLine += to.size();
            }
        }

        private void handleInsert(final List<String> insert) {
            for (String line : insert) {
                ps.print("> ");
                ps.print(line);
                if (!line.contains("\n")) {
                    ps.println();
                    ps.println("\\ No newline at end of file");
                }
            }
        }

        private void handleDelete(List<String> delete) {
            for (String line : delete) {
                ps.print("< ");
                ps.print(line);
                if (!line.contains("\n")) {
                    ps.println();
                    ps.println("\\ No newline at end of file");
                }
            }
        }

        private String getLineInfo(final int startLine, final int len) {
            if (len == 1) {
                return String.valueOf(startLine);
            } else {
                return String.format("%1$d,%2$d", startLine, (startLine + len - 1));
            }
        }
    }
}
