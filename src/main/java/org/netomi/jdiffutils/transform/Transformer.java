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
package org.netomi.jdiffutils.transform;

import org.netomi.jdiffutils.algorithm.DiffAlgorithm;

/**
 * A {@link Transformer} is used to normalize the input data before passing
 * it to the {@link DiffAlgorithm}.
 *
 * @param <T> the input/output type
 *
 * @author Thomas Neidhart
 */
public interface Transformer<T> {

    /**
     * Transforms the input data.
     *
     * @param input the input data to transform
     * @return the transformed input data
     */
    T transform(T input);
}
