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
package org.netomi.jdiffutils;

import org.netomi.jdiffutils.algorithm.EditScript;

public class Patch {

    private String originalFileName;
    private String updatedFileName;
    private EditScript<String> script;
    
    public Patch(final EditScript<String> script) {
        this(null, null, script);
    }

    public Patch(final String originalFileName, final String updatedFileName, final EditScript<String> script) {
        this.originalFileName = originalFileName;
        this.updatedFileName = updatedFileName;
        this.script = script;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public String getUpdatedFileName() {
        return updatedFileName;
    }

    public EditScript<String> getEditScript() {
        return script;
    }
}
