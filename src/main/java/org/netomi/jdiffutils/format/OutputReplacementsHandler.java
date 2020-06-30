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

import org.netomi.jdiffutils.algorithm.ReplacementsHandler;

/**
 * A default {@link ReplacementsHandler} to simplify output formatting.
 * <p>
 * Keeps track of the current line numbers.
 *
 * @author Thomas Neidhart
 */
abstract class OutputReplacementsHandler implements ReplacementsHandler<String> {

    protected final PrintStream ps;
    protected int inputLine = 1;
    protected int outputLine = 1;

    protected OutputReplacementsHandler(final PrintStream ps) {
        this.ps = ps;
    }
    
    @Override
    public void handleReplacement(int skipped, final List<String> from, final List<String> to) {
        inputLine += skipped;
        outputLine += skipped;
        
        handleReplacement(from, to);
    }
    
    @Override
    public void handleKeep(String object) {}

    protected abstract void handleReplacement(List<String> from, List<String> to);
}
