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
import java.util.Arrays;
import java.util.List;

import org.netomi.jdiffutils.Patch;
import org.netomi.jdiffutils.algorithm.ReplacementsFinder;

/**
 * A side-by-side formatter similar as produced by the gnu diff command with switch '-y'.
 *
 * @author Thomas Neidhart
 */
public class SideBySideFormatter implements OutputFormatter {

    /** The default column width. */
    private static final int DEFAULT_TOTAL_WIDTH = 130;

    /** The maximum number of columns to use for output. */
    private int totalWidth;

    /**
     * Create a new side-by-side formatter with the default total width (130).
     */
    public SideBySideFormatter() {
        this(DEFAULT_TOTAL_WIDTH);
    }

    /**
     * Create a new side-by-side formatter with the given total width.
     *
     * @param columnWidth the maximum column width to use
     */
    public SideBySideFormatter(final int totalWidth) {
        this.totalWidth = totalWidth;
    }

    @Override
    public void format(final Patch patch, final PrintStream ps) {
        patch.getEditScript().visit(new ReplacementsFinder<String>(new MyReplacementsHandler(ps)));
    }

    private class MyReplacementsHandler extends OutputReplacementsHandler {

        private final int columnWidth;
        private final int separatorWidth;
        private final char[] spaces;

        public MyReplacementsHandler(final PrintStream ps) {
            super(ps);
            
            // calculate the column and separator width
            columnWidth = (totalWidth - 3) / 2;
            separatorWidth = totalWidth - 2 * columnWidth;
            
            spaces = new char[columnWidth];
            Arrays.fill(spaces, ' ');
        }

        @Override
        public void handleReplacement(final List<String> from, final List<String> to) {
            if (from.size() == 0) {
                handleInsert(to);
            } else if (to.size() == 0) {
                handleDelete(from);
            } else {
                int i = 0;
                int j = 0;
                while (i < from.size() && j < to.size()) {
                    final String a = from.get(i);
                    final String b = to.get(i);

                    ps.print(rightTrim(a));
                    ps.print(getSpaces(columnWidth - a.length()));
                    printSeparator(ps, '|');
                    ps.println(rightTrim(b));
                    
                    i++;
                    j++;
                }

                handleDelete(from.subList(i, from.size()));
                handleInsert(to.subList(j, to.size()));
            }
        }

        @Override
        public void handleKeep(final String object) {
            if (object == null) {
                return;
            }
            final String left = rightTrim(object);
            ps.print(left);
            ps.print(getSpaces(columnWidth - getLength(left)));
            printSeparator(ps, ' ');
            ps.println(rightTrim(object));
        }

        private void handleInsert(final List<String> insert) {
            for (String line : insert) {
                ps.print(spaces);
                printSeparator(ps, '>');
                ps.println(rightTrim(line));
            }
        }

        private void handleDelete(final List<String> delete) {
            for (String line : delete) {
                ps.print(rightTrim(line));
                ps.print(getSpaces(columnWidth - line.length()));
                ps.println(" <");
            }
        }

        private String rightTrim(final String input) {
            // FIXME: this is inefficient and can lead to an infinite loop in case of tabs
            String str = input.substring(0, Math.min(columnWidth, input.length()));
            while (getLength(str) > columnWidth) {
                str = str.substring(0, str.length() - 1);
            }
            return str;
        }

        private int getLength(final String str) {
            int len = str.length();

            int idx = 0;
            int totalSlack = 0;
            while ((idx = str.indexOf('\t', idx)) != -1) {
                int slack = 7 - ((idx + totalSlack) % 8);
                totalSlack += slack;
                len += slack;
                idx++;
            }
            return len;
        }

        private String getSpaces(final int len) {
            if (len <= 0) {
                return "";
            }
            return new String(spaces, 0, len);
        }
        
        private void printSeparator(final PrintStream ps, final char separator) {
            ps.print(' ');
            ps.print(separator);
            ps.print(getSpaces(separatorWidth - 2));
        }
    }
}
