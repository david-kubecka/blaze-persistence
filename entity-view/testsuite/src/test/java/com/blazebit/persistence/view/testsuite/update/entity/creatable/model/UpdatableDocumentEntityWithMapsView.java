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

package com.blazebit.persistence.view.testsuite.update.entity.creatable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.update.entity.model.UpdatableDocumentEntityWithMapsViewBase;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentEntityWithMapsView extends UpdatableDocumentEntityWithMapsViewBase {

    @UpdatableMapping(cascade = { CascadeType.PERSIST })
    public Map<Integer, Person> getContacts();
    
    public void setContacts(Map<Integer, Person> contacts);

}
