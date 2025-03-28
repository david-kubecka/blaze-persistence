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

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereAndBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.RestrictionBuilderExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class WhereOrBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements WhereOrBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final CompoundPredicate predicate;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<RestrictionBuilder<WhereOrBuilder<T>>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<WhereOrBuilder<T>> rightSubqueryPredicateBuilderListener;
    private SubqueryBuilderListenerImpl<RestrictionBuilder<WhereOrBuilder<T>>> superExprLeftSubqueryPredicateBuilderListener;
    private CaseExpressionBuilderListener caseExpressionBuilderListener;
    private MultipleSubqueryInitiator<?> currentMultipleSubqueryInitiator;

    public WhereOrBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager) {
        this.result = result;
        this.listener = listener;
        this.predicate = new CompoundPredicate(CompoundPredicate.BooleanOperator.OR);
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
    }

    @Override
    public T endOr() {
        verifyBuilderEnded();
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public CompoundPredicate getPredicate() {
        return predicate;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate.getChildren().add(builder.getPredicate());
    }

    @Override
    public WhereAndBuilder<WhereOrBuilder<T>> whereAnd() {
        return startBuilder(new WhereAndBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @Override
    public RestrictionBuilder<WhereOrBuilder<T>> where(String expression) {
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        return startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, exp, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
    }

    @Override
    public CaseWhenStarterBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereCase() {
        RestrictionBuilderImpl<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new CaseWhenBuilderImpl<RestrictionBuilder<WhereOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
    }

    @Override
    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSimpleCase(String expression) {
        RestrictionBuilderImpl<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        caseExpressionBuilderListener = new CaseExpressionBuilderListener(restrictionBuilder);
        return caseExpressionBuilderListener.startBuilder(new SimpleCaseWhenBuilderImpl<RestrictionBuilder<WhereOrBuilder<T>>>(restrictionBuilder, caseExpressionBuilderListener, expressionFactory, expressionFactory.createSimpleExpression(expression, false), subqueryInitFactory, parameterManager, ClauseType.WHERE));
    }

    @Override
    public SubqueryInitiator<WhereOrBuilder<T>> whereExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator((WhereOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.WHERE);
    }

    @Override
    public SubqueryInitiator<WhereOrBuilder<T>> whereNotExists() {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryInitiator((WhereOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<WhereOrBuilder<T>> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereOrBuilder<T>>(this, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryBuilder((WhereOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<WhereOrBuilder<T>> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        rightSubqueryPredicateBuilderListener = startBuilder(new RightHandsideSubqueryPredicateBuilder<WhereOrBuilder<T>>(this, new ExistsPredicate(true)));
        return subqueryInitFactory.createSubqueryBuilder((WhereOrBuilder<T>) this, rightSubqueryPredicateBuilderListener, true, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery() {
        RestrictionBuilder<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, ClauseType.WHERE);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(String subqueryAlias, String expression) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, ClauseType.WHERE);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> whereSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        RestrictionBuilderImpl<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<RestrictionBuilder<WhereOrBuilder<T>>> initiator = new MultipleSubqueryInitiatorImpl<RestrictionBuilder<WhereOrBuilder<T>>>(restrictionBuilder, expr, new RestrictionBuilderExpressionBuilderListener(restrictionBuilder), subqueryInitFactory, ClauseType.WHERE);
        return initiator;
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        RestrictionBuilder<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, leftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<WhereOrBuilder<T>>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        superExprLeftSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        RestrictionBuilder<WhereOrBuilder<T>> restrictionBuilder = startBuilder(new RestrictionBuilderImpl<WhereOrBuilder<T>>(this, this, subqueryInitFactory, expressionFactory, parameterManager, ClauseType.WHERE));
        return subqueryInitFactory.createSubqueryBuilder(restrictionBuilder, superExprLeftSubqueryPredicateBuilderListener, false, criteriaBuilder, ClauseType.WHERE);
    }

    @Override
    public WhereOrBuilder<T> whereExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        this.predicate.getChildren().add(predicate);
        return this;
    }

    @Override
    public MultipleSubqueryInitiator<WhereOrBuilder<T>> whereExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        MultipleSubqueryInitiator<WhereOrBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<WhereOrBuilder<T>>(this, predicate, new ExpressionBuilderEndedListener() {

            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                WhereOrBuilderImpl.this.predicate.getChildren().add((Predicate) builder.getExpression());
                currentMultipleSubqueryInitiator = null;
            }

        }, subqueryInitFactory, ClauseType.WHERE);
        currentMultipleSubqueryInitiator = initiator;
        return initiator;
    }

    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        if (currentMultipleSubqueryInitiator != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if (rightSubqueryPredicateBuilderListener != null) {
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (superExprLeftSubqueryPredicateBuilderListener != null) {
            superExprLeftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
        if (caseExpressionBuilderListener != null) {
            caseExpressionBuilderListener.verifyBuilderEnded();
        }
    }
}
