/*
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 
 * @author Jordane Sarda
 * @author Luc Maisonobe
 */
public class MyersDiffAlgorithmTest {

    private List<String> before;
    private List<String> after;
    private int[] length;

    @Test
    public void testLength() {
        final MyersDiffAlgorithm<Character> comparator = new MyersDiffAlgorithm<Character>();

        for (int i = 0; i < before.size(); ++i) {
            assertEquals(length[i],
                         comparator.getEditScript(sequence(before.get(i)),
                                                  sequence(after.get(i)), null).getModifications());
        }
    }

    @Test
    public void testExecution() {
        final ExecutionVisitor<Character> ev = new ExecutionVisitor<Character>();
        for (int i = 0; i < before.size(); ++i) {
            ev.setList(sequence(before.get(i)));
            new MyersDiffAlgorithm<Character>().getEditScript(sequence(before.get(i)), sequence(after.get(i)), null).visit(ev);
            assertEquals(after.get(i), ev.getString());
        }
    }

    @Test
    public void testMinimal() {
        final String[] shadokAlph = new String[] { new String("GA"), new String("BU"), new String("ZO"),
                new String("MEU") };
        final List<String> sentenceBefore = new ArrayList<String>();
        final List<String> sentenceAfter = new ArrayList<String>();
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[2]);
        sentenceBefore.add(shadokAlph[3]);
        sentenceBefore.add(shadokAlph[1]);
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[2]);
        sentenceBefore.add(shadokAlph[1]);
        sentenceBefore.add(shadokAlph[3]);
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[2]);
        sentenceBefore.add(shadokAlph[1]);
        sentenceBefore.add(shadokAlph[3]);
        sentenceBefore.add(shadokAlph[2]);
        sentenceBefore.add(shadokAlph[2]);
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[1]);
        sentenceBefore.add(shadokAlph[3]);
        sentenceBefore.add(shadokAlph[0]);
        sentenceBefore.add(shadokAlph[3]);

        final Random random = new Random(4564634237452342L);

        for (int nbCom = 0; nbCom <= 40; nbCom += 5) {
            sentenceAfter.clear();
            sentenceAfter.addAll(sentenceBefore);
            for (int i = 0; i < nbCom; i++) {
                if (random.nextInt(2) == 0) {
                    sentenceAfter.add(random.nextInt(sentenceAfter.size() + 1), shadokAlph[random.nextInt(4)]);
                } else {
                    sentenceAfter.remove(random.nextInt(sentenceAfter.size()));
                }
            }

            final MyersDiffAlgorithm<String> comparator = new MyersDiffAlgorithm<String>();
            assertTrue(comparator.getEditScript(sentenceBefore, sentenceAfter, null).getModifications() <= nbCom);
        }
    }

    @Test
    public void testShadok() {
        final int lgMax = 5;
        final String[] shadokAlph = new String[] { "GA", "BU", "ZO", "MEU" };
        List<List<String>> shadokSentences = new ArrayList<List<String>>();
        for (int lg = 0; lg < lgMax; ++lg) {
            final List<List<String>> newTab = new ArrayList<List<String>>();
            newTab.add(new ArrayList<String>());
            for (final String element : shadokAlph) {
                for (final List<String> sentence : shadokSentences) {
                    final List<String> newSentence = new ArrayList<String>(sentence);
                    newSentence.add(element);
                    newTab.add(newSentence);
                }
            }
            shadokSentences = newTab;
        }

        final ExecutionVisitor<String> ev = new ExecutionVisitor<String>();

        for (int i = 0; i < shadokSentences.size(); ++i) {
            for (int j = 0; j < shadokSentences.size(); ++j) {
                ev.setList(shadokSentences.get(i));
                new MyersDiffAlgorithm<String>().getEditScript(shadokSentences.get(i), shadokSentences.get(j), null).visit(ev);

                final StringBuilder concat = new StringBuilder();
                for (final String s : shadokSentences.get(j)) {
                    concat.append(s);
                }
                assertEquals(concat.toString(), ev.getString());
            }
        }
    }

    private List<Character> sequence(final String string) {
        final List<Character> list = new ArrayList<Character>();
        for (int i = 0; i < string.length(); ++i) {
            list.add(new Character(string.charAt(i)));
        }
        return list;
    }

    private class ExecutionVisitor<T> implements CommandVisitor<T> {

        private List<T> v;
        private int index;

        public void setList(final List<T> array) {
            v = new ArrayList<T>(array);
            index = 0;
        }

        public void visitInsertCommand(final T object) {
            v.add(index++, object);
        }

        public void visitKeepCommand(final T object) {
            ++index;
        }

        public void visitDeleteCommand(final T object) {
            v.remove(index);
        }

        public String getString() {
            final StringBuffer buffer = new StringBuffer();
            for (final T c : v) {
                buffer.append(c);
            }
            return buffer.toString();
        }

        @Override
        public void startVisit() {
        }

        @Override
        public void finishVisit() {
        }

    }

    @BeforeEach
    public void setUp() {
        before = Arrays.asList("bottle", "nematode knowledge", "", "aa", "prefixed string", "ABCABBA",
                               "glop glop", "coq", "spider-man");

        after = Arrays.asList("noodle", "empty bottle", "", "C", "prefix", "CBABAC",
                              "pas glop pas glop", "ane", "klingon");

        length = new int[] { 6, 16, 0, 3, 9, 5, 8, 6, 13 };
    }

    @AfterEach
    public void tearDown() {
        before = null;
        after = null;
        length = null;
    }
}
