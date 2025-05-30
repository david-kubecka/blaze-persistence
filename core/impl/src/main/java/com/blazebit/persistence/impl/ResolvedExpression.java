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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ResolvedExpression {

    private final String expressionString;
    private final Expression expression;

    public ResolvedExpression(String expressionString, Expression expression) {
        this.expressionString = expressionString;
        this.expression = expression;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvedExpression)) {
            return false;
        }

        return expressionString.equals(((ResolvedExpression) o).expressionString);
    }

    @Override
    public int hashCode() {
        return expressionString.hashCode();
    }

    @Override
    public String toString() {
        return expressionString;
    }
}
