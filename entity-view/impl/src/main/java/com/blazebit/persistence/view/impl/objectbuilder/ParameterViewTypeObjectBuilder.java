/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.QueryBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cpbec
 */
public class ParameterViewTypeObjectBuilder<T> extends DelegatingObjectBuilder<T> {
    
    private final String[] parameterMappings;
    private final int[] parameterIndices;
    private final QueryBuilder<?, ?> queryBuilder;

    public ParameterViewTypeObjectBuilder(ObjectBuilder<T> delegate, ViewTypeObjectBuilderTemplate<T> template, QueryBuilder<?, ?> queryBuilder, int startIndex) {
        super(delegate);
        
        if (!template.hasParameters()) {
            throw new IllegalArgumentException("No templates without parameters allowed for this object builder!");
        }
        
        String[] fullParamMappings = template.getParameterMappings();
        String[] paramMappings = new String[fullParamMappings.length];
        int[] paramIndices = new int[fullParamMappings.length];
        int size = 0;
        
        for (int i = 0; i < fullParamMappings.length; i++) {
            if (fullParamMappings[i] != null) {
                paramMappings[size] = fullParamMappings[i];
                paramIndices[size] = i + startIndex;
                size++;
            }
        }
        
        this.parameterMappings = Arrays.copyOf(paramMappings, size);
        this.parameterIndices = Arrays.copyOf(paramIndices, size);
        this.queryBuilder = queryBuilder;
    }

    @Override
    public T build(Object[] tuple) {
        for (int i = 0; i < parameterMappings.length; i++) {
            tuple[parameterIndices[i]] = queryBuilder.getParameterValue(parameterMappings[i]);
        }
        
        return super.build(tuple);
    }
}
