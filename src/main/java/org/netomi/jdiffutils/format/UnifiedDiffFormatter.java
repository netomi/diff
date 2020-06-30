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

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.netomi.jdiffutils.Patch;
import org.netomi.jdiffutils.algorithm.ReplacementsFinder;

/**
 * Output formatter for the classic diff format.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Diff">Diff Format (Wikipedia)</a>
 * @author Thomas Neidhart
 */
public class UnifiedDiffFormatter implements OutputFormatter {

    /** The default number of context lines. */
    private static final int DEFAULT_CONTEXT_LINES = 3;

    /** The number of context lines to be used. */
    private int context;
    
    /**
     * Create a new {@link UnifiedDiffFormatter} with the default
     * number of context lines (3).
     */
    public UnifiedDiffFormatter() {
        this(DEFAULT_CONTEXT_LINES);
    }

    /**
     * Create a new {@link UnifiedDiffFormatter} with the given
     * number of context lines.
     *
     * @param context the number of context lines output
     */
    public UnifiedDiffFormatter(final int context) {
        this.context = context;
    }

    @Override
    public void format(final Patch patch, final PrintStream ps) {
        if (patch.getOriginalFileName() != null) {
            formatFileName(patch.getOriginalFileName(), "---", ps);
        }
        if (patch.getUpdatedFileName() != null) {
            formatFileName(patch.getUpdatedFileName(), "+++", ps);
        }

        final MyReplacementsHandler handler = new MyReplacementsHandler(ps);
        patch.getEditScript().visit(new ReplacementsFinder<String>(handler));
        handler.finishChunk();
    }
    
    private void formatFileName(final String fileName, final String prefix, final PrintStream ps) {
        ps.print(prefix);
        ps.print(' ');
        ps.print(fileName);
        ps.print('\t');

        File file = new File(fileName);
        if (file.exists()) {
            Date modificationDate = new Date(file.lastModified());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
            ps.print(dateFormat.format(modificationDate));
        }

        ps.println();
    }

    private class MyReplacementsHandler extends OutputReplacementsHandler {

        /** A circular queue containing the last N lines. */
        private Deque<String> queue;

        /** The buffer containing the current chunk. */
        private List<String> buffer;

        /** The starting line number of the current chunk for the first file. */
        private int lineA = 1;
        /** The starting line number of the current chunk for the second file. */
        private int lineB = 1;

        /** The length of the current chunk for the first file. */
        private int lengthA = 0;
        /** The length of the current chunk for the second file. */
        private int lengthB = 0;

        /** Indicates the number of context lines already added to the current chunk. */
        private int printedContext = -1;

        public MyReplacementsHandler(PrintStream ps) {
            super(ps);
            this.queue = new LinkedList<String>();
            this.buffer = new LinkedList<String>();
        }

        @Override
        public void handleReplacement(final List<String> from, final List<String> to) {
            if (from.size() == 0) {
                resetContext();
                handleInsert(to, true);
                outputLine += to.size();
                lengthB += to.size();
            } else if (to.size() == 0) {
                resetContext();
                handleDelete(from, true);
                inputLine += from.size();
                lengthA += from.size();
            } else {
                resetContext();
                handleDelete(from, true);
                handleInsert(to, false);
                inputLine += from.size();
                outputLine += to.size();
                lengthA += from.size();
                lengthB += to.size();
            }
        }

        private void handleInsert(final List<String> insert, final boolean withContext) {
            if (withContext) {
                while (!queue.isEmpty()) {
                    buffer.add(' ' + queue.remove());
                }
            }
            for (final String line : insert) {
                buffer.add('+' + line);
            }
            printedContext = 0;
        }

        private void handleDelete(final List<String> delete, final boolean withContext) {
            if (withContext) {
                while (!queue.isEmpty()) {
                    buffer.add(' ' + queue.remove());
                }
            }
            for (final String line : delete) {
                buffer.add('-' + line);
            }
            printedContext = 0;
        }

        @Override
        public void handleKeep(final String object) {
            if (object == null) {
                return;
            }
            if (printedContext == -1) {
                queue.add(object);
                if (queue.size() > context) {
                    queue.removeFirst();
                }
            } else {
                buffer.add(' ' + object);
                lengthA++;
                lengthB++;
                if (++printedContext == context) {
                    finishChunk();
                }
            }
        }

        private void resetContext() {
            if (printedContext == -1) {
                lineA = inputLine - queue.size();
                lineB = outputLine - queue.size();
                lengthA = lengthB = queue.size();
            }
        }

        /**
         * Finished the current buffered chunk.
         * <p>
         * This method must be called at the end of the visit to close the last pending chunk.
         */
        public void finishChunk() {
            if (!buffer.isEmpty()) {
                ps.print("@@ -");
                ps.print(lineA);
                ps.print(',');
                ps.print(lengthA);
                ps.print(" +");
                ps.print(lineB);
                ps.print(',');
                ps.print(lengthB);
                ps.println(" @@");
                
                for (final String s : buffer) {
                    ps.print(s);
                    if (!s.contains("\n")) {
                        ps.println();
                        ps.println("\\ No newline at end of file");
                    }
                }

                buffer.clear();
                printedContext = -1;
            }
        }
    }

}
