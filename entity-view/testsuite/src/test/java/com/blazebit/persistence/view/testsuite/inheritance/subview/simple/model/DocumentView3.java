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

package com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceSubtype;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentView3 extends SimpleDocumentView {

    @MappingInheritance(onlySubtypes = true, value = {
            @MappingInheritanceSubtype(mapping = "age < 16", value = YoungPersonView3.class)
    })
    public PersonBaseView3 getOwner();
}
