/*
 * Copyright 2002-2012 CS Systèmes d'Information.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netomi.jdiffutils.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.netomi.jdiffutils.algorithm.command.DeleteCommand;
import org.netomi.jdiffutils.algorithm.command.InsertCommand;
import org.netomi.jdiffutils.algorithm.command.KeepCommand;
import org.netomi.jdiffutils.transform.Transformer;

/**
 * This class allows to compare two objects sequences.
 * <p>
 * The two sequences can hold any object type, as only the <code>equals</code>
 * method is used to compare the elements of the sequences. It is guaranteed
 * that the comparisons will always be done as <code>o1.equals(o2)</code> where
 * <code>o1</code> belongs to the first sequence and <code>o2</code> belongs to
 * the second sequence. This can be important if subclassing is used for some
 * elements in the first sequence and the <code>equals</code> method is
 * specialized.
 * <p>
 * Comparison can be seen from two points of view: either as giving the smallest
 * modification allowing to transform the first sequence into the second one, or
 * as giving the longest sequence which is a subsequence of both initial
 * sequences. The <code>equals</code> method is used to compare objects, so any
 * object can be put into sequences. Modifications include deleting, inserting
 * or keeping one object, starting from the beginning of the first sequence.
 * <p>
 * This class implements the comparison algorithm, which is the very efficient
 * algorithm from Eugene W. Myers
 * <a href="http://www.cis.upenn.edu/~bcpierce/courses/dd/papers/diff.ps">
 * An O(ND) Difference Algorithm and Its Variations</a>. This algorithm produces
 * the shortest possible {@link EditScript edit script} containing all the
 * {@link EditCommand commands} needed to transform the first sequence into the second one.
 *
 * @see EditScript
 * @see EditCommand
 * @see CommandVisitor
 *
 * @author Jordane Sarda
 * @author Luc Maisonobe
 * @author Thomas Neidhart
 */
public class MyersDiffAlgorithm<T> implements DiffAlgorithm<T> {

    /** First sequence. */
    private List<T> sequence1;

    /** Second sequence. */
    private List<T> sequence2;

    /** Temporary variables. */
    private int[] vDown;
    private int[] vUp;

    /**
     * Get the {@link EditScript} object.
     * <p>
     * It is guaranteed that the objects embedded in the {@link InsertCommand
     * insert commands} come from the second sequence and that the objects
     * embedded in either the {@link DeleteCommand delete commands} or
     * {@link KeepCommand keep commands} come from the first sequence. This can
     * be important if subclassing is used for some elements in the first
     * sequence and the <code>equals</code> method is specialized.
     *
     * @return the edit script resulting from the comparison of the two sequences
     */
    @Override
    public EditScript<T> getEditScript(final List<T> origSequence, final List<T> newSequence,
                                       final Transformer<T> transformer) {
        if (transformer == null) {
            this.sequence1 = origSequence;
            this.sequence2 = newSequence;
        } else {
            // TODO: when using the plain myers diff algorithm and a transformer is defined
            //       the output shows the transformed data instead of the raw input data
            
            this.sequence1 = new ArrayList<T>(origSequence.size());
            this.sequence2 = new ArrayList<T>(newSequence.size());
            
            for (final T obj : origSequence) {
                sequence1.add(transformer.transform(obj));
            }
            for (final T obj : newSequence) {
                sequence2.add(transformer.transform(obj));
            }
        }

        final int size = sequence1.size() + sequence2.size() + 2;
        vDown = new int[size];
        vUp   = new int[size];
        
        final EditScript<T> script = new EditScript<T>();
        buildScript(0, sequence1.size(), 0, sequence2.size(), script);
        return script;
    }

    /**
     * Build a snake.
     *
     * @param start  the value of the start of the snake
     * @param diag  the value of the diagonal of the snake
     * @param end1  the value of the end of the first sequence to be compared
     * @param end2  the value of the end of the second sequence to be compared
     * @return the snake built
     */
    private Snake buildSnake(final int start, final int diag, final int end1, final int end2) {
        int end = start;
        while (end - diag < end2 && end < end1 && sequence1.get(end).equals(sequence2.get(end - diag))) {
            ++end;
        }
        return new Snake(start, end, diag);
    }

    /**
     * Get the middle snake corresponding to two subsequences of the
     * main sequences.
     * <p>
     * The snake is found using the MYERS Algorithm (this algorithms has
     * also been implemented in the GNU diff program). This algorithm is
     * explained in Eugene Myers article:
     * <a href="http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps">
     * An O(ND) Difference Algorithm and Its Variations</a>.
     *
     * @param start1  the begin of the first sequence to be compared
     * @param end1  the end of the first sequence to be compared
     * @param start2  the begin of the second sequence to be compared
     * @param end2  the end of the second sequence to be compared
     * @return the middle snake
     */
    private Snake getMiddleSnake(final int start1, final int end1, final int start2, final int end2) {
        // Myers Algorithm
        // Initialisations
        final int m = end1 - start1;
        final int n = end2 - start2;
        if (m == 0 || n == 0) {
            return null;
        }

        final int delta  = m - n;
        final int sum    = n + m;
        final int offset = (sum % 2 == 0 ? sum : sum + 1) / 2;
        vDown[1+offset] = start1;
        vUp[1+offset]   = end1 + 1;

        for (int d = 0; d <= offset ; d++) {
            // Down
            for (int k = -d; k <= d; k += 2) {
                // First step

                final int i = k + offset;
                if (k == -d || k != d && vDown[i-1] < vDown[i+1]) {
                    vDown[i] = vDown[i+1];
                } else {
                    vDown[i] = vDown[i-1] + 1;
                }

                int x = vDown[i];
                int y = x - start1 + start2 - k;

                while (x < end1 && y < end2 && sequence1.get(x).equals(sequence2.get(y))) {
                    vDown[i] = ++x;
                    ++y;
                }
                // Second step
                if (delta % 2 != 0 && delta - d + 1 <= k && k <= delta + d - 1) {
                    if (vUp[i-delta] <= vDown[i]) {
                        return buildSnake(vUp[i-delta], k + start1 - start2, end1, end2);
                    }
                }
            }

            // Up
            for (int k = delta - d; k <= delta + d; k += 2) {
                // First step
                final int i = k + offset - delta;
                if (k == delta - d
                        || k != delta + d && vUp[i+1] <= vUp[i-1]) {
                    vUp[i] = vUp[i+1] - 1;
                } else {
                    vUp[i] = vUp[i-1];
                }

                int x = vUp[i] - 1;
                int y = x - start1 + start2 - k;
                while (x >= start1 && y >= start2
                        && sequence1.get(x).equals(sequence2.get(y))) {
                    vUp[i] = x--;
                    y--;
                }
                // Second step
                if (delta % 2 == 0 && -d <= (k + delta) && (k + delta) <= d ) {
                    if (vUp[i] <= vDown[i + delta]) {
                        return buildSnake(vUp[i], k + start1 - start2, end1, end2);
                    }
                }
            }
        }

        // this should not happen
        throw new RuntimeException("Internal Error");
    }

    /**
     * Build an edit script.
     *
     * @param start1  the begin of the first sequence to be compared
     * @param end1  the end of the first sequence to be compared
     * @param start2  the begin of the second sequence to be compared
     * @param end2  the end of the second sequence to be compared
     * @param script the edited script
     */
    private void buildScript(int start1, int end1, int start2, int end2,
                             final EditScript<T> script) {

        // short-cut: for elements at the start/end of the two sequences that are equal
        //            we can already add keep commands and omit them during the main algorithm
        //            to speed up things.
        
        while (start1 < end1 && start2 < end2 && sequence1.get(start1).equals(sequence2.get(start2))) {
            script.appendKeep(sequence1.get(start1));
            ++start1;
            ++start2;
        }

        final LinkedList<T> commands = new LinkedList<T>();
        while (end1 > start1 && end2 > start2 && sequence1.get(end1 - 1).equals(sequence2.get(end2 - 1))) {
            // collect the equal elements at the end in reverse order
            commands.addFirst(sequence1.get(end1 - 1));
            --end1;
            --end2;
        }

        // one of the sequences is a sub-sequence of the other one
        if (start1 == end1) {
            while (start2 < end2) {
                script.appendInsert(sequence2.get(start2++));
            }
        } else if (start2 == end2) {
            while (start1 < end1) {
                script.appendDelete(sequence1.get(start1++));
            }
        } else {
            // for the remaining part, calculate the middle snake
            final Snake middle = getMiddleSnake(start1, end1, start2, end2);

            if (middle == null
                    || middle.getStart() == end1 && middle.getDiag() == end1 - end2
                    || middle.getEnd() == start1 && middle.getDiag() == start1 - start2) {

                int i = start1;
                int j = start2;
                while (i < end1 || j < end2) {
                    if (i < end1 && j < end2 && sequence1.get(i).equals(sequence2.get(j))) {
                        script.appendKeep(sequence1.get(i));
                        ++i;
                        ++j;
                    } else {
                        if (end1 - start1 > end2 - start2) {
                            script.appendDelete(sequence1.get(i));
                            ++i;
                        } else {
                            script.appendInsert(sequence2.get(j));
                            ++j;
                        }
                    }
                }
            } else {
                buildScript(start1, middle.getStart(),
                            start2, middle.getStart() - middle.getDiag(),
                            script);
                for (int i = middle.getStart(); i < middle.getEnd(); ++i) {
                    script.appendKeep(sequence1.get(i));
                }
                buildScript(middle.getEnd(), end1,
                            middle.getEnd() - middle.getDiag(), end2,
                            script);
            }
        }

        //do not forget to add the collected keep commands at the end
        for (final T cmd : commands) {
            script.appendKeep(cmd);
        }
    }

    /**
     * This class is a simple placeholder to hold the end part of a path
     * under construction in a {@link MyersDiffAlgorithm SequencesComparator}.
     */
    private static class Snake {

        /** Start index. */
        private final int start;

        /** End index. */
        private final int end;

        /** Diagonal number. */
        private final int diag;

        /**
         * Simple constructor. Creates a new instance of Snake with specified indices.
         *
         * @param start  start index of the snake
         * @param end  end index of the snake
         * @param diag  diagonal number
         */
        public Snake(final int start, final int end, final int diag) {
            this.start = start;
            this.end   = end;
            this.diag  = diag;
        }

        /**
         * Get the start index of the snake.
         *
         * @return start index of the snake
         */
        public int getStart() {
            return start;
        }

        /**
         * Get the end index of the snake.
         *
         * @return end index of the snake
         */
        public int getEnd() {
            return end;
        }

        /**
         * Get the diagonal number of the snake.
         *
         * @return diagonal number of the snake
         */
        public int getDiag() {
            return diag;
        }
    }
}
