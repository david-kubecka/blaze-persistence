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

package com.blazebit.persistence.testsuite.base.jpa.assertion;

import com.blazebit.persistence.testsuite.base.jpa.RelationalModelAccessor;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AssertMultiStatementBuilder extends AssertStatementBuilder {

    private final AssertStatementBuilder parentBuilder;

    public AssertMultiStatementBuilder(AssertStatementBuilder parentBuilder, RelationalModelAccessor relationalModelAccessor) {
        super(relationalModelAccessor, null);
        this.parentBuilder = parentBuilder;
        parentBuilder.setCurrentBuilder(this);
    }

    @Override
    public AssertMultiStatementBuilder unordered() {
        throw new IllegalStateException("Multiple nested unordered statement asserters aren't supported!");
    }

    public AssertStatementBuilder and() {
        validateBuilderEnded();
        parentBuilder.unsetCurrentBuilder(this);
        // Add the same multi-statement instead of the real statements
        // The multi-statement will "consume" the given statements unordered
        AssertMultiStatement assertMultiStatement = new AssertMultiStatement(statements);
        for (int i = 0; i < statements.size(); i++) {
            parentBuilder.addStatement(assertMultiStatement);
        }
        return parentBuilder;
    }

    public void validate() {
        failIfValidated();
        validated = true;
        and().validate();
    }
}
