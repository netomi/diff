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

import java.util.LinkedList;
import java.util.List;

/**
 * Executes a chain of {@link Transformer} instances in sequence.
 *
 * @param <T> the data type
 *
 * @author Thomas Neidhart
 */
public class TransformerChain<T> implements Transformer<T> {

    /** The list of transformers. */
    private final List<Transformer<T>> transformers;
    
    /**
     * Create a new empty TransformerChain.
     */
    public TransformerChain() {
        transformers = new LinkedList<Transformer<T>>();
    }

    /**
     * Create a new TransformerChain with two initial {@link Transformer} instances.
     *
     * @param a the first transformer to add
     * @param b the second transformer to add
     */
    public TransformerChain(final Transformer<T> a, final Transformer<T> b) {
        this();
        addTransformer(a);
        addTransformer(b);
    }

    /**
     * Create a new TransformerChain with the given {@link Transformer} instances.
     *
     * @param transformers the transformers to add initially
     */
    public TransformerChain(final Transformer<T>... transformers) {
        this();
        for (Transformer<T> t : transformers) {
            addTransformer(t);
        }
    }

    /**
     * Add another {@link Transformer} to this TransformerChain.
     *
     * @param transformer the transformer to add
     */
    public void addTransformer(final Transformer<T> transformer) {
        transformers.add(transformer);
    }

    @Override
    public T transform(final T input) {
        T transformed = input;
        for (Transformer<T> t : transformers) {
            transformed = t.transform(transformed);
        }
        return transformed;
    }
    
}
