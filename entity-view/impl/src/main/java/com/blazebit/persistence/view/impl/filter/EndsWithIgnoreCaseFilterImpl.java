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

package com.blazebit.persistence.view.impl.filter;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.view.filter.EndsWithIgnoreCaseFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EndsWithIgnoreCaseFilterImpl<FilterValue> extends EndsWithIgnoreCaseFilter<FilterValue> {

    private final String value;

    public EndsWithIgnoreCaseFilterImpl(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

        this.value = "%" + value;
    }

    @Override
    public <T> T apply(RestrictionBuilder<T> rb) {
        return rb.like(false).value(value).noEscape();
    }
}
