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

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(EmbeddableTestEntity2.class)
public interface EmbeddableTestEntityFetchAsViewViewJoin extends EmbeddableTestEntityFetchAsViewView {

    @Mapping(value = "embeddable.name", fetch = FetchStrategy.JOIN)
    String getName();

    @Mapping(value = "embeddable.manyToOne", fetch = FetchStrategy.JOIN)
    EmbeddableTestEntitySimpleFetchView getManyToOne();

    @Mapping(value = "embeddable.oneToMany", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntitySimpleFetchView> getOneToMany();

    @Mapping(value = "embeddable.elementCollection", fetch = FetchStrategy.JOIN)
    Set<IntIdEntityFetchSubView> getElementCollection();

    @Mapping(value = "embeddableSet", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableSet();

    @Mapping(value = "embeddableMap", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableMap();

}
