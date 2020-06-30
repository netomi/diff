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
package tech.neidhart.diff.editscript;

import java.util.ArrayList;
import java.util.List;

import static tech.neidhart.diff.editscript.EditCommand.*;

/**
 * This class gathers all the {@link EditCommand commands} needed to transform
 * one objects sequence into another objects sequence.
 * <p>
 * An edit script is the most general view of the differences between two
 * sequences. It is built as the result of the comparison between two sequences
 * by the {@link MyersDiffAlgorithm SequencesComparator} class. The user can
 * walk through it using the <em>visitor</em> design pattern.
 * <p>
 * It is guaranteed that the objects embedded in the {@link InsertCommand insert
 * commands} come from the second sequence and that the objects embedded in
 * either the {@link DeleteCommand delete commands} or {@link KeepCommand keep
 * commands} come from the first sequence. This can be important if subclassing
 * is used for some elements in the first sequence and the <code>equals</code>
 * method is specialized.
 *
 * @see DiffAlgorithm
 * @see EditCommand
 * @see CommandVisitor
 * @see ReplacementsHandler
 *
 * @author Jordane Sarda
 * @author Luc Maisonobe
 */
public class EditScript<T> {

    /** Container for the commands. */
    private final List<EditCommand<T>> commands;

    /** Length of the longest common subsequence. */
    private int lcsLength;

    /** Number of modifications. */
    private int editDistance;

    /**
     * Simple constructor. Creates a new empty script.
     */
    public EditScript() {
        commands     = new ArrayList<>();
        lcsLength    = 0;
        editDistance = 0;
    }

    /**
     * Add a keep command for the specified object to the script.
     *
     * @param object  object to keep.
     */
    public void appendKeep(final T object) {
        commands.add(keep(object));
        ++lcsLength;
    }

    /**
     * Add an insert command for the specified object to the script.
     *
     * @param object  object to add
     */
    public void appendInsert(final T object) {
        commands.add(insert(object));
        ++editDistance;
    }

    /**
     * Add a delete command for the specified object to the script.
     *
     * @param object  object to delete
     */
    public void appendDelete(final T object) {
        commands.add(delete(object));
        ++editDistance;
    }

    /**
     * Visit the script. The script implements the <em>visitor</em> design
     * pattern, this method is the entry point to which the user supplies its
     * own visitor, the script will be responsible to drive it through the
     * commands in order and call the appropriate method as each command is
     * encountered.
     *
     * @param visitor  the visitor that will visit all commands in turn
     */
    public void visit(final CommandVisitor<T> visitor) {
        visitor.startVisit();
        for (final EditCommand<T> command : commands) {
            command.accept(visitor);
        }
        visitor.finishVisit();
    }

    /**
     * Visit the script in reverse order. The script implements the <em>visitor</em> design
     * pattern, this method is the entry point to which the user supplies its
     * own visitor, the script will be responsible to drive it through the
     * commands in order and call the appropriate method as each command is
     * encountered.
     *
     * @param visitor  the visitor that will visit all commands in turn
     */
    public void visitReverse(final CommandVisitor<T> visitor) {
        visitor.startVisit();
        for (int i = commands.size() - 1; i >= 0; i--) {
            final EditCommand<T> command = commands.get(i);
            command.accept(visitor);
        }
        visitor.finishVisit();
    }

    /**
     * Get the length of the Longest Common Subsequence (LCS). The length of the
     * longest common subsequence is the number of keep commands in the script.
     *
     * @return length of the longest common subsequence.
     */
    public int getLCSLength() {
        return lcsLength;
    }

    /**
     * Get the edit distance encoded by the commands in this {@link EditScript}.
     *
     * @return the edit distance represented by this script.
     */
    public int getEditDistance() {
        return editDistance;
    }

    /**
     * Returns the total number of commands contained in this {@link EditScript}.
     *
     * @return the total number of commands.
     */
    public int getSize() {
        return commands.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (EditCommand<T> command : commands) {
            sb.append(command);
        }

        return sb.toString();
    }
}
