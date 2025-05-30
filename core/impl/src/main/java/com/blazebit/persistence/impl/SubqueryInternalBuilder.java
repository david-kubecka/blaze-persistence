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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.Subquery;
import com.blazebit.persistence.spi.JpqlFunctionProcessor;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SubqueryInternalBuilder<T> extends Subquery {
    
    public T getResult();

    public List<Expression> getSelectExpressions();

    public Map<Integer, JpqlFunctionProcessor<?>> getJpqlFunctionProcessors();

    public Set<Expression> getCorrelatedExpressions(AliasManager aliasManager);
    
    public int getFirstResult();
    
    public int getMaxResults();
    
}
