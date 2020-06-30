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
package org.netomi.jdiffutils.transform;

/**
 * Transforms the input to lower-case.
 *
 * @author Thomas Neidhart
 */
public class LowerCaseTransformer implements Transformer<String> {

    @Override
    public String transform(final String input) {
        return input.toLowerCase();
    }
}
