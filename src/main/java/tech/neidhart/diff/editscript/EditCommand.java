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

/**
 * Abstract base class for all commands used to transform an objects sequence
 * into another one.
 * <p>
 * When two objects sequences are compared through the
 * {@link DiffAlgorithm#getEditScript(java.util.List, java.util.List)} method,
 * the result is provided has a {@link EditScript script} containing the commands
 * that progressively transform the first sequence into the second one.
 * <p>
 * There are only three types of commands, all of which are subclasses of this
 * abstract class. Each command is associated with one object belonging to at
 * least one of the sequences. These commands are {@link InsertCommand
 * InsertCommand} which correspond to an object of the second sequence being
 * inserted into the first sequence, {@link DeleteCommand DeleteCommand} which
 * correspond to an object of the first sequence being removed and
 * {@link KeepCommand KeepCommand} which correspond to an object of the first
 * sequence which <code>equals</code> an object in the second sequence. It is
 * guaranteed that comparison is always performed this way (i.e. the
 * <code>equals</code> method of the object from the first sequence is used and
 * the object passed as an argument comes from the second sequence) ; this can
 * be important if subclassing is used for some elements in the first sequence
 * and the <code>equals</code> method is specialized.
 *
 * @see DiffAlgorithm
 *
 * @author Jordane Sarda
 * @author Luc Maisonobe
 */
abstract class EditCommand<T> {

    /** Object on which the command should be applied. */
    protected final T object;

    public static <T> EditCommand<T> keep(T object) {
        return new KeepCommand<>(object);
    }

    public static <T> EditCommand<T> insert(T object) {
        return new InsertCommand<>(object);
    }

    public static <T> EditCommand<T> delete(T object) {
        return new DeleteCommand<>(object);
    }

    /**
     * Simple constructor. Creates a new instance of EditCommand
     *
     * @param object  reference to the object associated with this command, this
     *   refers to an element of one of the sequences being compared
     */
    protected EditCommand(final T object) {
        this.object = object;
    }

    /**
     * Returns the object associated with this command.
     *
     * @return the object on which the command is applied
     */
    protected T getObject() {
        return object;
    }

    /**
     * Accept a visitor.
     * <p>
     * This method is invoked for each commands belonging to
     * an {@link EditScript EditScript}, in order to implement the visitor design pattern.
     *
     * @param visitor  the visitor to be accepted
     */
    public abstract void accept(CommandVisitor<T> visitor);

    // Concrete implementations of an EditCommand.

    /**
     * Command representing the keeping of one object present in both sequences.
     * <p>
     * When one object of the first sequence <code>equals</code> another objects in
     * the second sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the keeping of this object. The objects embedded in
     * these type of commands always come from the first sequence.
     */
    private static class KeepCommand<T> extends EditCommand<T> {

        /**
         * Simple constructor. Creates a new instance of {@link KeepCommand}.
         *
         * @param object  the object belonging to both sequences (the object is a
         *   reference to the instance in the first sequence which is known
         *   to be equal to an instance in the second sequence)
         */
        public KeepCommand(final T object) {
            super(object);
        }

        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitKeepCommand(object);
        }

        @Override
        public String toString() {
            return String.format(" %s", object);
        }
    }

    /**
     * Command representing the insertion of one object of the second sequence.
     * <p>
     * When one object of the second sequence has no corresponding object in the
     * first sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the insertion of this object. The objects embedded in
     * these type of commands always come from the second sequence.
     */
    private static class InsertCommand<T> extends EditCommand<T> {

        /**
         * Simple constructor. Creates a new instance of {@link InsertCommand}.
         *
         * @param object the object of the second sequence that should be inserted
         */
        public InsertCommand(final T object) {
            super(object);
        }

        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitInsertCommand(object);
        }

        @Override
        public String toString() {
            return String.format("+%s", object);
        }
    }

    /**
     * Command representing the deletion of one object of the first sequence.
     * <p>
     * When one object of the first sequence has no corresponding object in the
     * second sequence at the right place, the {@link EditScript edit script}
     * transforming the first sequence into the second sequence uses an instance of
     * this class to represent the deletion of this object. The objects embedded in
     * these type of commands always come from the first sequence.
     */
    private static class DeleteCommand<T> extends EditCommand<T> {

        /**
         * Simple constructor. Creates a new instance of {@link DeleteCommand}.
         *
         * @param object  the object of the first sequence that should be deleted.
         */
        public DeleteCommand(final T object) {
            super(object);
        }

        @Override
        public void accept(final CommandVisitor<T> visitor) {
            visitor.visitDeleteCommand(object);
        }

        @Override
        public String toString() {
            return String.format("-%s", object);
        }
    }
}
