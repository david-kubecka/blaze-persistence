/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleTransformerFactory;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedSubselectTupleTransformerFactory implements TupleTransformerFactory {

    protected final Correlator correlator;
    protected final EntityViewManagerImpl evm;
    protected final ManagedViewTypeImplementor<?> viewRootType;
    protected final ManagedViewTypeImplementor<?> embeddingViewType;
    protected final String viewRootAlias;
    protected final String embeddingViewPath;
    protected final Expression correlationResult;
    protected final String correlationBasisExpression;
    protected final String correlationKeyExpression;
    protected final CorrelationProviderFactory correlationProviderFactory;
    protected final String attributePath;
    protected final String[] fetches;
    protected final int viewRootIndex;
    protected final int embeddingViewIndex;
    protected final int correlationBasisIndex;
    protected final Class<?> correlationBasisType;
    protected final Class<?> correlationBasisEntity;
    protected final Limiter limiter;

    public AbstractCorrelatedSubselectTupleTransformerFactory(Correlator correlator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRootType, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath,
                                                              Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory,
                                                              String attributePath, String[] fetches, int viewRootIndex, int embeddingViewIndex, int correlationBasisIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter) {
        this.correlator = correlator;
        this.evm = evm;
        this.viewRootType = viewRootType;
        this.embeddingViewType = embeddingViewType;
        this.viewRootAlias = viewRootAlias;
        this.embeddingViewPath = embeddingViewPath;
        this.correlationResult = correlationResult;
        this.correlationBasisExpression = correlationBasisExpression;
        this.correlationKeyExpression = correlationKeyExpression;
        this.correlationProviderFactory = correlationProviderFactory;
        this.attributePath = attributePath;
        this.fetches = fetches;
        this.viewRootIndex = viewRootIndex;
        this.embeddingViewIndex = embeddingViewIndex;
        this.correlationBasisIndex = correlationBasisIndex;
        this.correlationBasisType = correlationBasisType;
        this.correlationBasisEntity = correlationBasisEntity;
        this.limiter = limiter;
    }

    @Override
    public int getConsumeStartIndex() {
        return -1;
    }

    @Override
    public int getConsumeEndIndex() {
        return -1;
    }
}
