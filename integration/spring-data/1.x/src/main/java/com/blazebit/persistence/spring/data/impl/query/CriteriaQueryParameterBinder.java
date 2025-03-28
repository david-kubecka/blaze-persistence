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

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.spring.data.base.query.AbstractCriteriaQueryParameterBinder;
import com.blazebit.persistence.spring.data.base.query.JpaParameters;
import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

/**
 * Concrete version for Spring Data 1.x.
 * 
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CriteriaQueryParameterBinder extends AbstractCriteriaQueryParameterBinder {

    public CriteriaQueryParameterBinder(EntityManager em, EntityViewManager evm, JpaParameters parameters, Object[] values, Iterable<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
        super(em, evm, parameters, values, expressions);
    }

    @Override
    protected int getOffset() {
        Pageable pageable = getPageable();
        if (pageable instanceof KeysetPageable) {
            return ((KeysetPageable) pageable).getIntOffset();
        } else {
            return pageable.getOffset();
        }
    }
}
