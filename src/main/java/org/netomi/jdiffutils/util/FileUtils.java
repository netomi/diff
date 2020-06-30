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
package org.netomi.jdiffutils.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    /**
     * Loads a file
     * @param fileName
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> loadFile(final String fileName) throws FileNotFoundException, IOException {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            final List<String> list = new ArrayList<String>();
            
            while(scanner.hasNextLine()) {
                String line = scanner.findWithinHorizon(".*\r?\n", 0);
                if (line == null) {
                    line = scanner.nextLine();
                }
                list.add(line);
            }
            
            return list;
        }
    }

}
