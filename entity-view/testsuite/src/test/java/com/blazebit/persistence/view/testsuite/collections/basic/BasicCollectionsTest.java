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

package com.blazebit.persistence.view.testsuite.collections.basic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentListMapSetView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentListSetMapView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentMapListSetView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentMapSetListView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentSetListMapView;
import com.blazebit.persistence.view.testsuite.collections.basic.model.BasicDocumentSetMapListView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@RunWith(Parameterized.class)
public class BasicCollectionsTest<T extends BasicDocumentCollectionsView> extends AbstractEntityViewTest {

    private final Class<T> viewType;

    private DocumentForCollections doc1;
    private DocumentForCollections doc2;

    public BasicCollectionsTest(Class<T> viewType) {
        this.viewType = viewType;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new DocumentForCollections("doc1");
                doc2 = new DocumentForCollections("doc2");

                PersonForCollections o1 = new PersonForCollections("pers1");
                PersonForCollections o2 = new PersonForCollections("pers2");
                PersonForCollections o3 = new PersonForCollections("pers3");
                PersonForCollections o4 = new PersonForCollections("pers4");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc1);
                o4.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);
                doc1.getContacts().put(2, o3);
                doc2.getContacts().put(2, o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                doc1.getPartners().add(o1);
                doc1.getPartners().add(o3);
                doc2.getPartners().add(o2);
                doc2.getPartners().add(o4);

                doc1.getPersonList().add(o1);
                doc1.getPersonList().add(o2);
                doc2.getPersonList().add(o3);
                doc2.getPersonList().add(o4);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc2").getSingleResult();
    }

    @Parameterized.Parameters
    public static Collection<?> entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { BasicDocumentListMapSetView.class },
            { BasicDocumentListSetMapView.class },
            { BasicDocumentMapListSetView.class },
            { BasicDocumentMapSetListView.class },
            { BasicDocumentSetListMapView.class },
            { BasicDocumentSetMapListView.class }
        });
    }

    @Test
    // NOTE: DataNucleus renders joins wrong: https://github.com/datanucleus/datanucleus-rdbms/issues/177
    // Eclipselink has a result set mapping bug in case of map keys
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void testCollections() {
        EntityViewManager evm = build(viewType);

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getContacts(), results.get(0).getContacts());
        assertEquals(doc1.getPartners(), results.get(0).getPartners());
        assertEquals(doc1.getPersonList(), results.get(0).getPersonList());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getContacts(), results.get(1).getContacts());
        assertEquals(doc2.getPartners(), results.get(1).getPartners());
        assertEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }
}
