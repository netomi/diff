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
package org.netomi.jdiffutils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netomi.jdiffutils.algorithm.DiffAlgorithm;
import org.netomi.jdiffutils.algorithm.EditScript;
import org.netomi.jdiffutils.algorithm.OptimizedDiffAlgorithm;
import org.netomi.jdiffutils.transform.NOPTransformer;
import org.netomi.jdiffutils.transform.Transformer;
import org.netomi.jdiffutils.util.FileUtils;

public class DiffUtils {

    private static Pattern UNIFIED_FORMAT_PATTERN = Pattern.compile("^@@ \\-(\\d+)\\,(\\d+) \\+(\\d+)\\,(\\d+) @@$");

    public static Patch diff(final String origFileName, final String newFileName) throws FileNotFoundException,
            IOException {
        return diff(origFileName, newFileName, NOPTransformer.<String> nopTransformer());
    }

    public static Patch diff(final String origFileName, final String newFileName, final Transformer<String> transformer)
            throws FileNotFoundException, IOException {
        final List<String> origList = FileUtils.loadFile(origFileName);
        final List<String> newList = FileUtils.loadFile(newFileName);

        DiffAlgorithm<String> diff = new OptimizedDiffAlgorithm<String>();
        EditScript<String> script = diff.getEditScript(origList, newList, transformer);

        return new Patch(origFileName, newFileName, script);
    }

    public static Patch diff(final List<String> origList, final List<String> newList, final Transformer<String> transformer) {
        DiffAlgorithm<String> diff = new OptimizedDiffAlgorithm<String>();
        EditScript<String> script = diff.getEditScript(origList, newList, transformer);

        return new Patch(script);
    }

    public static Patch loadPatch(final String fileName) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        try {
            EditScript<String> script = new EditScript<String>();
            int currentLine = 1;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("+++")) {
                    System.out.println("New filename: " + line.substring(4));
                } else if (line.startsWith("---")) {
                    System.out.println("Orig filename: " + line.substring(4));
                } else if (line.startsWith("+")) {
                    script.appendInsert(line.substring(1));
                } else if (line.startsWith("-")) {
                    script.appendDelete(line.substring(1));
                    currentLine++;
                } else if (line.startsWith("@@")) {
                    Matcher m = UNIFIED_FORMAT_PATTERN.matcher(line);
                    if (m.matches()) {
                        final int origStart = Integer.valueOf(m.group(1));
                        while (currentLine < origStart) {
                            script.appendKeep(null);
                            currentLine++;
                        }
                    }
                } else {
                    script.appendKeep(line.substring(1));
                    currentLine++;
                }
            }

            return new Patch(script);
        } finally {
            reader.close();
        }
    }
}
