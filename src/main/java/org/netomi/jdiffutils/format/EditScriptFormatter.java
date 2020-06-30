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
import org.netomi.jdiffutils.algorithm.EditScript;
import org.netomi.jdiffutils.algorithm.ReplacementsFinder;
import org.netomi.jdiffutils.algorithm.ReplacementsHandler;

/**
 * Output formatter for the edit script format.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Diff">Diff Format (Wikipedia)</a>
 * @author Thomas Neidhart
 */
public class EditScriptFormatter implements OutputFormatter {

    @Override
    public void format(final Patch patch, final PrintStream ps) {
        final EditScript<String> script = patch.getEditScript();
        
        // first get the count of the last replacement command in the original sequence
        final LineCounter counter = new LineCounter();
        script.visit(new ReplacementsFinder<String>(counter));

        script.visitReverse(new ReplacementsFinder<String>(new MyReplacementsHandler(counter.getLineNumber(), ps)));
    }

    /** A simple {@link ReplacementsHandler} the calculate the line number of the last command. */
    private static class LineCounter implements ReplacementsHandler<String> {

        private int inputLine = 1;

        @Override
        public void handleReplacement(int skipped, List<String> from, List<String> to) {
            inputLine += skipped + from.size();
        }

        @Override
        public void handleKeep(String object) {}

        public int getLineNumber() {
            return inputLine;
        }
    }

    private static class MyReplacementsHandler implements ReplacementsHandler<String> {

        private final PrintStream ps;
        private int inputLine;
        private boolean firstSkip;

        public MyReplacementsHandler(int lineNumber, PrintStream ps) {
            this.inputLine = lineNumber;
            this.ps = ps;
            this.firstSkip = true;
        }

        @Override
        public void handleReplacement(int skipped, List<String> from, List<String> to) {
            // ignore the first skip, as we already calculated the line number up to the first replacement
            if (!firstSkip) {
                inputLine -= skipped;
            } else {
                firstSkip = false;
            }

            if (from.size() == 0) {
                ps.print(inputLine - 1);
                ps.println('a');
                handleInsert(to);
            } else if (to.size() == 0) {
                inputLine -= from.size();
                ps.print(getLineInfo(inputLine, from.size()));
                ps.println('d');
            } else {
                inputLine -= from.size();
                ps.print(getLineInfo(inputLine, from.size()));
                ps.println('c');
                handleInsert(to);
            }
        }

        private String getLineInfo(int startLine, int len) {
            if (len == 1) {
                return String.valueOf(startLine);
            } else {
                return startLine + "," + (startLine + len - 1);
            }
        }

        private void handleInsert(List<String> insert) {
            for (int i = insert.size() - 1; i >= 0; i--) {
                ps.println(insert.get(i));
            }
            ps.println('.');
        }

        @Override
        public void handleKeep(String object) {}
    }
}
