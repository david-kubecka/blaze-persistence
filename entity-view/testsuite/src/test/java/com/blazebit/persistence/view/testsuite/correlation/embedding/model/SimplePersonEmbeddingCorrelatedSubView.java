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

package com.blazebit.persistence.view.testsuite.correlation.embedding.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.filter.ContainsFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Person.class)
public interface SimplePersonEmbeddingCorrelatedSubView {
    
    @IdMapping
    public Long getId();

    @Mapping("UPPER(name)")
    @AttributeFilter(ContainsFilter.class)
    public String getName();

    @Mapping("EMBEDDING_VIEW(owner.partnerDocument.name)")
    public String getPartnerDocumentName();
}
