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
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedMapSubselectTupleTransformer extends AbstractCorrelatedSubselectTupleTransformer {

    private final boolean recording;

    public CorrelatedMapSubselectTupleTransformer(ExpressionFactory ef, Correlator correlator, ContainerAccumulator<?> containerAccumulator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRootType, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath,
                                                  Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                  String[] indexFetches, Expression indexExpression, Correlator indexCorrelator, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity,
                                                  Limiter limiter, EntityViewConfiguration entityViewConfiguration, boolean recording) {
        super(ef, correlator, containerAccumulator, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, indexFetches, indexExpression, indexCorrelator, viewRootIndex, embeddingViewIndex,
                tupleIndex, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
        this.recording = recording;
    }

    @Override
    protected boolean isRecording() {
        return recording;
    }

}
