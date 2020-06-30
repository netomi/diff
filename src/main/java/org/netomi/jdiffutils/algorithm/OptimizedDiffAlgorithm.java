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
package org.netomi.jdiffutils.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netomi.jdiffutils.transform.Transformer;

/**
 * 
 *
 * @param <T>
 */
public class OptimizedDiffAlgorithm<T> implements DiffAlgorithm<T> {

    @Override
    public EditScript<T> getEditScript(final List<T> origSequence, final List<T> newSequence,
                                       Transformer<T> transformer) {

        Map<T, EquivalenceClass<T>> map = new HashMap<T, EquivalenceClass<T>>();

        List<T> transformedA = new ArrayList<T>(origSequence.size());
        for (final T a : origSequence) {
            final T transformed = transformer == null ? a : transformer.transform(a);
            transformedA.add(transformed);
            
            EquivalenceClass<T> c = map.get(transformed);
            if (c == null) {
                c = new EquivalenceClass<T>(transformed);
                map.put(transformed, c);
            }

            c.incA();
        }

        List<T> transformedB = new ArrayList<T>(newSequence.size());
        for (final T b : newSequence) {
            final T transformed = transformer == null ? b : transformer.transform(b);
            transformedB.add(transformed);
            
            EquivalenceClass<T> c = map.get(transformed);
            if (c == null) {
                c = new EquivalenceClass<T>(transformed);
                map.put(transformed, c);
            }

            c.incB();
        }

        @SuppressWarnings("unchecked")
        EquivalenceClass<T>[] reverseMap = new EquivalenceClass[map.size()];
        int index = 0;
        int countA = 0;
        int countB = 0;
        for (Map.Entry<T, EquivalenceClass<T>> entry : map.entrySet()) {
            EquivalenceClass<T> c = entry.getValue();
            if (c.getCountA() > 0 && c.getCountB() > 0) {
                c.setIndex(index);
                reverseMap[index] = c;
                countA += c.getCountA();
                countB += c.getCountB();
                index++;
            }
        }

        // TODO: find better heuristic when to omit the transformation
        //        as the plain myers algorithm will be faster, i.e. because
        //        the number of estimated changes is very small
        if (countA + countB > origSequence.size()) {
            return new MyersDiffAlgorithm<T>().getEditScript(origSequence, newSequence, transformer);
        }
        
        List<Integer> listA = new ArrayList<Integer>(countA);
        List<Integer> listB = new ArrayList<Integer>(countB);

        List<Integer> indexA = new ArrayList<Integer>(countA);
        List<Integer> indexB = new ArrayList<Integer>(countB);

        int lineNo = 0;
        for (final T s : transformedA) {
            EquivalenceClass<T> c = map.get(s);
            if (c.getIndex() >= 0) {
                listA.add(c.getIndex());
                indexA.add(lineNo);
            }
            lineNo++;
        }

        lineNo = 0;
        for (final T s : transformedB) {
            EquivalenceClass<T> c = map.get(s);
            if (c.getIndex() >= 0) {
                listB.add(c.getIndex());
                indexB.add(lineNo);
            }
            lineNo++;
        }

        DiffAlgorithm<Integer> diff = new MyersDiffAlgorithm<Integer>();
        EditScript<Integer> script = diff.getEditScript(listA, listB, null);

        ReverseTransformVisitor<T> visitor =
                new ReverseTransformVisitor<T>(origSequence, newSequence, reverseMap, indexA, indexB);
        script.visit(visitor);
        EditScript<T> updatedScript = visitor.getScript();
        
        return updatedScript;
    }

    private static class ReverseTransformVisitor<T> implements CommandVisitor<Integer> {

        int lineNoA = 0;
        int lineNoB = 0;
        int lastA = 0;
        int lastB = 0;

        List<T> listA;
        List<Integer> indexA;
        List<T> listB;
        List<Integer> indexB;
        EquivalenceClass[] map;

        EditScript<T> script;

        public ReverseTransformVisitor(List<T> listA, List<T> listB, EquivalenceClass[] map,
                List<Integer> indexA, List<Integer> indexB) {
            this.listA = listA;
            this.indexA = indexA;
            this.listB = listB;
            this.indexB = indexB;
            this.map = map;
            script = new EditScript<T>();
        }

        public EditScript<T> getScript() {
            return script;
        }

        @Override
        public void visitInsertCommand(Integer object) {
            int next = indexB.get(lineNoB);
            while (next >= lastB) {
                script.appendInsert(listB.get(lastB++));
            }
            lineNoB++;
        }

        @Override
        public void visitKeepCommand(Integer object) {
            int nextA = indexA.get(lineNoA);
            int nextB = indexB.get(lineNoB);

            while (nextA > lastA) {
                script.appendDelete(listA.get(lastA++));
            }
            while (nextB > lastB) {
                script.appendInsert(listB.get(lastB++));
            }

            EquivalenceClass<T> c = map[object.intValue()];
            script.appendKeep(c.getLine());

            lastA++;
            lastB++;

            lineNoA++;
            lineNoB++;
        }

        @Override
        public void visitDeleteCommand(Integer object) {
            int next = indexA.get(lineNoA);
            while (next >= lastA) {
                script.appendDelete(listA.get(lastA++));
            }
            lineNoA++;
        }

        @Override
        public void startVisit() {
        }

        @Override
        public void finishVisit() {
            while (lastA < listA.size()) {
                script.appendDelete(listA.get(lastA++));
            }
            while (lastB < listB.size()) {
                script.appendInsert(listB.get(lastB++));
            }
        }

    }
    
    /**
     * A class to represent equivalence classes of input data.
     * <p>
     * An equivalence class corresponds to a set of input data that
     * is supposed to be equal based on some criteria, e.g. after
     * transformation of the input data to lower case.
     *
     * @param <T>
     */
    private static class EquivalenceClass<T> {

        private T line;
        private int index;
        private int countA;
        private int countB;

        public EquivalenceClass(final T line) {
            this.line = line;
            index = -1;
            countA = 0;
            countB = 0;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public T getLine() {
            return line;
        }

        public int getCountA() {
            return countA;
        }

        public int getCountB() {
            return countB;
        }

        public void incA() {
            countA++;
        }

        public void incB() {
            countB++;
        }
    }

}
