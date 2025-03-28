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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation.JoinCorrelationBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ExpressionCorrelationJoinTupleElementMapper extends AbstractCorrelationJoinTupleElementMapper {

    private final CorrelationProvider provider;

    public ExpressionCorrelationJoinTupleElementMapper(CorrelationProvider provider, ExpressionFactory ef, String joinBase, String correlationBasis, Expression correlationResult, String alias, String attributePath, String embeddingViewPath, String[] fetches, Limiter limiter, Set<String> rootAliases) {
        super(ef, joinBase, correlationBasis, correlationResult, alias, attributePath, embeddingViewPath, fetches, limiter, rootAliases);
        this.provider = provider;
    }

    @Override
    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean asString) {
        String oldViewPath = viewJpqlMacro.getViewPath();
        String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        viewJpqlMacro.setViewPath(embeddingViewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
        FullQueryBuilder<?, ?> fullQueryBuilder;
        if (queryBuilder instanceof ConstrainedSelectBuilder) {
            fullQueryBuilder = ((ConstrainedSelectBuilder) queryBuilder).getQueryBuilder();
        } else {
            fullQueryBuilder = (FullQueryBuilder<?, ?>) queryBuilder;
        }
        int originalFirstResult = -1;
        int originalMaxResults = -1;
        if (queryBuilder instanceof LimitBuilder<?>) {
            originalFirstResult = ((LimitBuilder<?>) queryBuilder).getFirstResult();
            originalMaxResults = ((LimitBuilder<?>) queryBuilder).getMaxResults();
        }

        JoinCorrelationBuilder correlationBuilder = new JoinCorrelationBuilder(parameterHolder, optionalParameters, fullQueryBuilder, joinBase, correlationAlias, correlationExternalAlias, attributePath, JoinType.LEFT, limiter);
        provider.applyCorrelation(correlationBuilder, correlationBasis);

        if (queryBuilder instanceof LimitBuilder<?>) {
            if (originalFirstResult != ((LimitBuilder<?>) queryBuilder).getFirstResult()
                    || originalMaxResults != ((LimitBuilder<?>) queryBuilder).getMaxResults()) {
                throw new IllegalArgumentException("Correlation provider '" + provider + "' wrongly uses setFirstResult() or setMaxResults() on the query builder which might lead to wrong results. Use SELECT fetching with batch size 1 or reformulate the correlation provider to use the limit/offset in a subquery!");
            }
        }
        correlationBuilder.finish();

        // Basic element has an alias, subviews don't
        if (alias != null) {
            viewJpqlMacro.setViewPath(null);
            queryBuilder.select(correlationResult, alias);
        }
        viewJpqlMacro.setViewPath(oldViewPath);
        embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
        if (fetches.length != 0) {
            for (int i = 0; i < fetches.length; i++) {
                fullQueryBuilder.fetch(correlationBuilder.getCorrelationAlias() + "." + fetches[i]);
            }
        }
    }

}
