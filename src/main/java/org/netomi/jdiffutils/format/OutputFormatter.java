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

import org.netomi.jdiffutils.Patch;

/**
 * An {@link OutputFormatter} is used to transform a {@link Patch} object
 * into a human-readable format, e.g. the classical diff format.
 *
 * @author Thomas Neidhart
 */
public interface OutputFormatter {

    /**
     * Format the given {@link Patch} using the specified {@link PrintStream}.
     *
     * @param patch the patch object to format
     * @param ps the print stream to use for formatting
     */
    void format(Patch patch, PrintStream ps);
}
