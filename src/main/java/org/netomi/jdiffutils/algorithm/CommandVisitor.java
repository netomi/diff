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

/**
 * This interface should be implemented by user object to walk
 * through {@link EditScript EditScript} objects.
 * <p>
 * Users should implement this interface in order to walk through
 * the {@link EditScript EditScript} object created by the comparison
 * of two sequences. This is a direct application of the visitor
 * design pattern. The {@link EditScript#visit EditScript.visit}
 * method takes an object implementing this interface as an argument,
 * it will perform the loop over all commands in the script and the
 * proper methods of the user class will be called as the commands are
 * encountered.
 * <p>
 * The implementation of the user visitor class will depend on the
 * need. Here are two examples.
 * <p>
 * This example shows the commands and the way
 * they transform the first sequence into the second one:
 * <pre>
 * import org.netomi.jdiffutils.algorithm.CommandVisitor;
 *
 * import java.util.Arrays;
 * import java.util.ArrayList;
 * import java.util.Iterator;
 *
 * public class ShowVisitor implements CommandVisitor {
 *
 *   public ShowVisitor(Object[] sequence1) {
 *     v = new ArrayList();
 *     v.addAll(Arrays.asList(sequence1));
 *     index = 0;
 *   }
 *
 *   public void startVisit() {}
 *
 *   public void finishVisit() {}
 *
 *   public void visitInsertCommand(Object object) {
 *     v.insertElementAt(object, index++);
 *     display("insert", object);
 *   }
 *
 *   public void visitKeepCommand(Object object) {
 *     ++index;
 *     display("keep  ", object);
 *   }
 *
 *   public void visitDeleteCommand(Object object) {
 *     v.remove(index);
 *     display("delete", object);
 *   }
 *
 *   private void display(String commandName, Object object) {
 *     System.out.println(commandName + " " + object + " ->" + this);
 *   }
 *
 *   public String toString() {
 *     StringBuffer buffer = new StringBuffer();
 *     for (Iterator iter = v.iterator(); iter.hasNext();) {
 *       buffer.append(' ').append(iter.next());
 *     }
 *     return buffer.toString();
 *   }
 *
 *   private ArrayList v;
 *   private int index;
 *
 * }
 * </pre>
 * 
 * @author Jordane Sarda
 * @author Luc Maisonobe
 * @author Thomas Neidhart
 */
public interface CommandVisitor<T> {

    /**
     * Method called before the first command.
     */
    void startVisit();

    /**
     * Method called after the last command.
     */
    void finishVisit();

    /**
     * Method called when an insert command is encountered.
     *
     * @param object object to insert (this object comes from the second sequence)
     */
    void visitInsertCommand(T object);

    /**
     * Method called when a keep command is encountered.
     * <p>
     * NOTE: in case an {@link EditScript} has been loaded from a diff file,
     * the keep information from the original file may be lost, thus {@code object}
     * may be {@code null} in such cases.
     *
     * @param object object to keep (this object comes from the first sequence)
     */
    void visitKeepCommand(T object);

    /**
     * Method called when a delete command is encountered.
     *
     * @param object object to delete (this object comes from the first sequence)
     */
    void visitDeleteCommand(T object);

}
