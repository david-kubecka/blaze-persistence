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

package com.blazebit.persistence.impl.function.window.denserank;

import com.blazebit.persistence.impl.function.window.AbstractWindowFunction;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 * Rank of the current row without gaps; this function counts peer groups
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class DenseRankFunction extends AbstractWindowFunction {

    public static final String FUNCTION_NAME = "DENSE_RANK";

    public DenseRankFunction(DbmsDialect dbmsDialect) {
        super(FUNCTION_NAME, dbmsDialect.isNullSmallest(), dbmsDialect.supportsWindowNullPrecedence(), dbmsDialect.supportsFilterClause(), false);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Integer.class;
    }

    @Override
    protected boolean requiresOver() {
        return true;
    }
}
