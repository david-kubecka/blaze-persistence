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

package com.blazebit.persistence.impl.builder.expression;

import java.util.ArrayList;
import java.util.List;

import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.util.TypeUtils;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SimpleCaseWhenBuilderImpl<T> implements SimpleCaseWhenBuilder<T>, ExpressionBuilder {

    private final T result;
    private final Expression caseOperandExpression;
    private final List<WhenClauseExpression> whenExpressions;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private final ExpressionBuilderEndedListener listener;

    private SimpleCaseExpression expression;

    public SimpleCaseWhenBuilderImpl(T result, ExpressionBuilderEndedListener listener, ExpressionFactory expressionFactory, Expression caseOperandExpression, SubqueryInitiatorFactory subqueryInitFactory, ParameterManager parameterManager, ClauseType clauseType) {
        this.result = result;
        this.caseOperandExpression = caseOperandExpression;
        this.subqueryInitFactory = subqueryInitFactory;
        this.parameterManager = parameterManager;
        this.clauseType = clauseType;
        this.whenExpressions = new ArrayList<WhenClauseExpression>();
        this.expressionFactory = expressionFactory;
        this.listener = listener;
    }

    @Override
    public SimpleCaseWhenBuilder<T> when(String condition, String thenExpression) {
        whenExpressions.add(new WhenClauseExpression(expressionFactory.createSimpleExpression(condition, false), expressionFactory.createSimpleExpression(thenExpression, false)));
        return this;
    }

    @Override
    public T otherwiseLiteral(Object elseValue) {
        if (whenExpressions.isEmpty()) {
            throw new IllegalStateException("No when clauses specified");
        }
        String literal = TypeUtils.asLiteral(elseValue, subqueryInitFactory.getQueryBuilder().getMetamodel().getEnumTypes().keySet());
        if (literal == null) {
            return otherwiseValue(elseValue);
        }
        expression = new SimpleCaseExpression(caseOperandExpression, whenExpressions, expressionFactory.createInItemExpression(literal));
        listener.onBuilderEnded(this);
        return result;
    }

    public T otherwiseValue(Object value) {
        if (whenExpressions.isEmpty()) {
            throw new IllegalStateException("No when clauses specified");
        }
        expression = new SimpleCaseExpression(caseOperandExpression, whenExpressions, parameterManager.addParameterExpression(value, clauseType, subqueryInitFactory.getQueryBuilder()));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public T otherwise(String elseExpression) {
        if (whenExpressions.isEmpty()) {
            throw new IllegalStateException("No when clauses specified");
        }
        expression = new SimpleCaseExpression(caseOperandExpression, whenExpressions, expressionFactory.createSimpleExpression(elseExpression, false));
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

}
