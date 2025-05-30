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

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class EqPredicate extends QuantifiableBinaryExpressionPredicate implements Negatable {

    public EqPredicate(boolean negated) {
        this(null, null, negated);
    }

    public EqPredicate(Expression left, Expression right) {
        this(left, right, PredicateQuantifier.ONE, false);
    }

    public EqPredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        this(left, right, quantifier, false);
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, quantifier, negated);
    }

    @Override
    public EqPredicate copy(ExpressionCopyContext copyContext) {
        return new EqPredicate(left.copy(copyContext), right.copy(copyContext), quantifier, negated);
    }

    @Override
    public void accept(Expression.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Expression.ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
