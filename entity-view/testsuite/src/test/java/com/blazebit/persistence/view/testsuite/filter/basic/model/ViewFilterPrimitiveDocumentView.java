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

package com.blazebit.persistence.view.testsuite.filter.basic.model;

import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.ViewFilter;
import com.blazebit.persistence.view.ViewFilterProvider;
import com.blazebit.persistence.view.ViewFilters;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(PrimitiveDocument.class)
@ViewFilters({
        @ViewFilter(name = "viewFilter1", value = ViewFilterPrimitiveDocumentView.TestViewFilter1Provider.class),
        @ViewFilter(name = "viewFilter2", value = ViewFilterPrimitiveDocumentView.TestViewFilter2Provider.class)
})
public interface ViewFilterPrimitiveDocumentView {
    @IdMapping
    Long getId();

    String getName();

    class TestViewFilter1Provider extends ViewFilterProvider {

        @Override
        public <T extends WhereBuilder<T>> T apply(T whereBuilder) {
            return whereBuilder.where("owner.name").eqExpression("'James'");
        }
    }

    class TestViewFilter2Provider extends ViewFilterProvider {

        @Override
        public <T extends WhereBuilder<T>> T apply(T whereBuilder) {
            return whereBuilder.where("owner.name").eqExpression(":viewFilterParam");
        }
    }
}
