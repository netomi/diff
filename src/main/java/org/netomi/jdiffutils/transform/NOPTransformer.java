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
 * Transformer implementation that does nothing.
 *
 * @param <T> the data type
 *
 * @author Thomas Neidhart
 */
public final class NOPTransformer<T> implements Transformer<T> {

    /** Singleton predicate instance. */
    public static final Transformer<Object> INSTANCE = new NOPTransformer<Object>();

    /**
     * Factory method returning the singleton instance.
     *
     * @param <T>  the input/output type
     * @return the singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Transformer<T> nopTransformer() {
        return (Transformer<T>) INSTANCE;
    }
    
    @Override
    public T transform(final T input) {
        return input;
    }
    
}
