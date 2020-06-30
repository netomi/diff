/*
 * Copyright (c) 2020 Thomas Neidhart.
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
package tech.neidhart.diff.editscript;

import org.netomi.jdiffutils.algorithm.MyersDiffAlgorithm;

import java.util.*;

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
 * the shortest possible {@link org.netomi.jdiffutils.algorithm.EditScript edit script} containing all the
 * {@link org.netomi.jdiffutils.algorithm.EditCommand commands} needed to transform the first sequence into the second one.
 *
 * @see org.netomi.jdiffutils.algorithm.EditScript
 * @see EditCommand
 * @see CommandVisitor
 *
 * @author Jordane Sarda
 * @author Luc Maisonobe
 * @author Thomas Neidhart
 */
public class MyersAlgorithm<T> {

    public EditScript<T> getEditScript(List<T> sequenceA,
                                       List<T> sequenceB) {
        Context ctx = new Context(sequenceA, sequenceB);
        buildScript(ctx, sequenceA, sequenceB);
        return ctx.getEditScript();
    }

    private void buildScript(Context context, List<T> listX, List<T> listY) {
        int startX = 0;
        int startY = 0;

        int endX = listX.size();
        int endY = listY.size();

        while (startX < endX && startY < endY && listX.get(startX).equals(listY.get(startY))) {
            context.appendKeep(listX.get(startX));
            startX++;
            startY++;
        }

        LinkedList<T> tail = new LinkedList<>();
        while (endX > startX && endY > startY && listX.get(endX - 1).equals(listY.get(endY - 1))) {
            tail.addFirst(listX.get(endX - 1));
            endX--;
            endY--;
        }

        if (startX == endX) {
            while (startY < endY) {
                context.appendInsert(listY.get(startY++));
            }
        } else if (startY == endY) {
            while (startX < endX) {
                context.appendDelete(listX.get(startX++));
            }
        } else {
            listX = listX.subList(startX, endX);
            listY = listY.subList(startY, endY);

            Snake middle = getMiddleSnake(context, listX, listY);

//            return;
//            System.exit(0);
//            buildScript(context, listX.subList(0, middle.getStart()), listY.subList(0, middle.getStart() - middle.getDiag()));
//
//            for (int i = middle.getStart(); i < middle.getEnd(); ++i) {
//                context.appendKeep(listX.get(i));
//            }
//
//            buildScript(context, listX.subList(middle.getEnd(), listX.size()), listY.subList(middle.getEnd() - middle.getDiag(), listY.size()));
        }

        for (final T object : tail) {
            context.appendKeep(object);
        }
    }

    private Snake getMiddleSnake(Context context, List<T> listX, List<T> listY) {

        int N = listX.size();
        int M = listY.size();

        int MAX = N + M;

        int[] V = new int[2 * MAX];

        List<int[]> trace = new ArrayList<>();

        V[1 + MAX] = 0;

        int x, y;

        for (int d = 0; d <= MAX; d++) {
            trace.add(V.clone());
            for (int k = -d; k <= d; k += 2) {
                int i = k + MAX;

                if (k == -d ||
                        (k != d && V[i - 1] < V[i + 1])) {
                    x = V[i + 1];
                } else {
                    x = V[i - 1] + 1;
                }

                y = x - k;

                while (x < N && y < M && listX.get(x).equals(listY.get(y))) {
                    x++;
                    y++;
                }

                V[i] = x;

                if (x >= N && y >= M) {

                    //trace.add(V.clone());

                    backtrack(context, listX, listY, trace, MAX);
                    //return buildSnake(listX, listY, V[i], k, N, M);
                    return null;
                }
            }

            //trace.add(V.clone());
        }

        throw new AssertionError("length of LCS is larger than N + M");
    }

    private void backtrack(Context context, List<T> listX, List<T> listY, List<int[]> trace, int offset) {
        int x = listX.size();
        int y = listY.size();

        List<Chunk> chunks = new ArrayList<>();

        for (int d = trace.size() - 1; d >= 0; d--) {
            int[] v = trace.get(d);

            int k = x - y;

            int prev_k;

            if (k == -d || (k != d && v[k - 1 + offset] < v[k + 1 + offset])){
                prev_k = k + 1;
            } else{
                prev_k = k - 1;
            }

            int prev_x = v[prev_k + offset];
            int prev_y = prev_x - prev_k;

            while (x > prev_x && y > prev_y) {
                chunks.add(new Chunk(x-1, y-1, x, y));
                System.out.println(String.format("%d, %d -> %d, %d", x-1, y-1, x, y));
                x--;
                y--;
            }

            if (d > 0) {
                chunks.add(new Chunk(prev_x, prev_y, x, y));
                System.out.println(String.format("%d, %d -> %d, %d", prev_x, prev_y, x, y));
            }

            x = prev_x;
            y = prev_y;
        }

        Collections.reverse(chunks);
        for (Chunk chunk : chunks) {
            if (chunk.x == chunk.prev_x) {
                context.appendInsert(listY.get(chunk.prev_y));
            } else if (chunk.y == chunk.prev_y) {
                context.appendDelete(listX.get(chunk.prev_x));
            } else {
                context.appendKeep(listX.get(chunk.prev_x));
            }
        }
    }

    private static final class Chunk {
        private final int prev_x;
        private final int prev_y;
        private final int x;
        private final int y;

        public Chunk(int prev_x, int prev_y, int x, int y) {
            this.prev_x = prev_x;
            this.prev_y = prev_y;
            this.x = x;
            this.y = y;
        }
    }

    private Snake buildSnake(List<T> sequence1, List<T> sequence2, final int start, final int diag, final int end1, final int end2) {
        int end = start;
        while (end - diag < end2 && end < end1 && sequence1.get(end).equals(sequence2.get(end - diag))) {
            ++end;
        }
        return new Snake(start, end, diag);
    }

    private final class Context {
        private final List<T> sequenceX;
        private final List<T> sequenceY;

        private final EditScript<T> editScript;

        private Context(List<T> sequenceX, List<T> sequenceY) {
            this.sequenceX = sequenceX;
            this.sequenceY = sequenceY;

            this.editScript = new EditScript<>();
        }

        public EditScript<T> getEditScript() {
            return editScript;
        }

        public void appendKeep(T object) {
            editScript.appendKeep(object);
        }

        public void appendInsert(T object) {
            editScript.appendInsert(object);
        }

        public void appendDelete(T object) {
            editScript.appendDelete(object);
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
