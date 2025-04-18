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
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.view.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(PrimitiveDocument.class)
public interface PrimitiveDocumentView extends PrimitiveSimpleDocumentView {

    public void setId(long id);

    public void setName(String name);

    public PrimitivePersonView getOwner();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public PrimitivePersonView getCorrelatedOwner();

    @Mapping("contacts[1]")
    public PrimitivePerson getFirstContactPerson();

    public List<PrimitivePersonView> getPartners();

    public Map<Integer, PrimitivePersonView> getContacts();

    public List<PrimitivePersonView> getPeople();

    // TODO: Report that selecting bags in datanucleus leads to an exception
//    public List<PrimitivePersonView> getPeopleListBag();

//    public List<PrimitivePersonView> getPeopleCollectionBag();

    public PrimitiveSimpleDocumentView getParent();

}
