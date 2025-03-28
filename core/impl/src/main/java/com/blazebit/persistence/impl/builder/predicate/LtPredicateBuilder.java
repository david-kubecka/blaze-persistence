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

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.QuantifiableBinaryExpressionPredicate;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LtPredicateBuilder<T> extends AbstractQuantifiablePredicateBuilder<T> {

    public LtPredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        super(result, listener, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clauseType);
    }

    @Override
    protected QuantifiableBinaryExpressionPredicate createPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        return new LtPredicate(left, right, quantifier, false);
    }

}
