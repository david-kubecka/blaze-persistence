
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

package com.blazebit.persistence.testsuite.treat.jpql;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.treat.entity.Base;
import com.blazebit.persistence.testsuite.treat.entity.BaseEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.JoinedBase;
import com.blazebit.persistence.testsuite.treat.entity.JoinedSub1;
import com.blazebit.persistence.testsuite.treat.entity.JoinedSub2;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub2;
import com.blazebit.persistence.testsuite.treat.entity.Sub1;
import com.blazebit.persistence.testsuite.treat.entity.Sub1Embeddable;
import com.blazebit.persistence.testsuite.treat.entity.Sub2;
import com.blazebit.persistence.testsuite.treat.entity.Sub2Embeddable;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassBase;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassSub1;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
// NOTE: These tests are just for reference and have been copied and adapted form the jpa-treat-variations repository
@Category({ NoHibernate.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public abstract class AbstractTreatVariationsTest extends AbstractCoreTest {
    
    protected final String strategy;
    protected final String objectPrefix;
    
    public AbstractTreatVariationsTest(String strategy, String objectPrefix) {
        this.strategy = strategy;
        this.objectPrefix = objectPrefix;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                BaseEmbeddable.class,
                IntIdEntity.class,
                JoinedBase.class,
                JoinedSub1.class,
                JoinedSub2.class,
                SingleTableBase.class,
                SingleTableSub1.class,
                SingleTableSub2.class,
                Sub1Embeddable.class,
                Sub2Embeddable.class,
                TablePerClassBase.class,
                TablePerClassSub1.class,
                TablePerClassSub2.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                IntIdEntity i1 = new IntIdEntity("i1", 1);
                em.persist(i1);
                persist(em, new IntIdEntity("s1", 1));
                persist(em, new IntIdEntity("s2", 2));
                persist(em, new IntIdEntity("s1.parent", 101));
                persist(em, new IntIdEntity("s2.parent", 102));
                persist(em, new IntIdEntity("st1", 1));
                persist(em, new IntIdEntity("st2", 2));
                persist(em, new IntIdEntity("st1.parent", 101));
                persist(em, new IntIdEntity("st2.parent", 102));
                persist(em, new IntIdEntity("tpc1", 1));
                persist(em, new IntIdEntity("tpc2", 2));
                persist(em, new IntIdEntity("tpc1.parent", 101));
                persist(em, new IntIdEntity("tpc2.parent", 102));

                /****************
                 * Joined
                 ***************/

                JoinedSub1 s1 = new JoinedSub1("s1");
                JoinedSub2 s2 = new JoinedSub2("s2");
                JoinedSub1 s1Parent = new JoinedSub1("s1.parent");
                JoinedSub2 s2Parent = new JoinedSub2("s2.parent");

                persist(em, i1, s1, s2, s1Parent, s2Parent);

                /****************
                 * Single Table
                 ***************/

                SingleTableSub1 st1 = new SingleTableSub1("st1");
                SingleTableSub2 st2 = new SingleTableSub2("st2");
                SingleTableSub1 st1Parent = new SingleTableSub1("st1.parent");
                SingleTableSub2 st2Parent = new SingleTableSub2("st2.parent");

                persist(em, i1, st1, st2, st1Parent, st2Parent);

                /****************
                 * Table per Class
                 ***************/

                TablePerClassSub1 tpc1 = new TablePerClassSub1(1L, "tpc1");
                TablePerClassSub2 tpc2 = new TablePerClassSub2(2L, "tpc2");
                TablePerClassSub1 tpc1Parent = new TablePerClassSub1(3L, "tpc1.parent");
                TablePerClassSub2 tpc2Parent = new TablePerClassSub2(4L, "tpc2.parent");

                // The Java compiler can't up-cast automatically, maybe a bug?
                //persist(em, i1, tpc1, tpc2, tpc1Parent, tpc2Parent);
                persist(em, i1, (Sub1) tpc1, (Sub2) tpc2, (Sub1) tpc1Parent, (Sub2) tpc2Parent);
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void persist(
            EntityManager em,
            IntIdEntity i1,
            Sub1<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub1Embeddable<?>> s1,
            Sub2<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub2Embeddable<?>> s2,
            Sub1<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub1Embeddable<?>> s1Parent,
            Sub2<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub2Embeddable<?>> s2Parent) {
        
        
        em.persist(s1Parent);
        em.persist(s2Parent);
        em.persist(s1);
        em.persist(s2);
        
        s1Parent.setValue(101);
        s1Parent.setSub1Value(101);
        s1Parent.getSub1Embeddable().setSomeValue(101);
        s1Parent.getEmbeddable1().setSub1SomeValue(101);
        s1.setValue(1);
        s1.setSub1Value(1);
        s1.getEmbeddable1().setSub1SomeValue(1);
        s1.setRelation1(i1);
        ((Sub1) s1).setParent(s1Parent);
        ((Sub1) s1).setParent1(s1Parent);
        ((BaseEmbeddable) s1.getEmbeddable()).setParent(s1Parent);
        ((Sub1Embeddable) s1.getEmbeddable1()).setSub1Parent(s1Parent);
        ((List<Base<?, ?>>) s1.getList()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getList1()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getEmbeddable().getList()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getEmbeddable1().getSub1List()).add(s1Parent);
        ((List<Base<?, ?>>) s1Parent.getList()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getList1()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getEmbeddable().getList()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getEmbeddable1().getSub1List()).add(s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getMap()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getMap1()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getEmbeddable().getMap()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getEmbeddable1().getSub1Map()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getMap()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getMap1()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getEmbeddable().getMap()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getEmbeddable1().getSub1Map()).put(s2, s2);
        
        s2Parent.setValue(102);
        s2Parent.setSub2Value(102);
        s2Parent.getSub2Embeddable().setSomeValue(102);
        s2Parent.getEmbeddable2().setSub2SomeValue(102);
        s2.setValue(2);
        s2.setSub2Value(2);
        s2.getEmbeddable2().setSub2SomeValue(2);
        s2.setRelation2(i1);
        ((Sub2) s2).setParent(s2Parent);
        ((Sub2) s2).setParent2(s2Parent);
        ((BaseEmbeddable) s2.getEmbeddable()).setParent(s2Parent);
        ((Sub2Embeddable) s2.getEmbeddable2()).setSub2Parent(s2Parent);
        ((List<Base<?, ?>>) s2.getList()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getList2()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getEmbeddable().getList()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getEmbeddable2().getSub2List()).add(s2Parent);
        ((List<Base<?, ?>>) s2Parent.getList()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getList2()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getEmbeddable().getList()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getEmbeddable2().getSub2List()).add(s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getMap()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getMap2()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getEmbeddable().getMap()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getEmbeddable2().getSub2Map()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getMap()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getMap2()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getEmbeddable().getMap()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getEmbeddable2().getSub2Map()).put(s1, s1);
    }
    
    private void persist(
            EntityManager em,
            IntIdEntity i1) {
        // Persist 2 name matching IntIdEntity one with the child value and one with the parent value
        em.persist(i1);
        if (i1.getValue() > 100) {
            em.persist(new IntIdEntity(i1.getName(), i1.getValue() - 100));
        } else {
            em.persist(new IntIdEntity(i1.getName(), i1.getValue() + 100));
        }
    }
    
    /************************************************************
     * Just some helper methods
     ************************************************************/
    
    protected <T> List<T> list(String query, Class<T> clazz) {
        TypedQuery<T> q = em.createQuery(query, clazz);
        
        List<T> bases = q.getResultList();
        // Close the em to make sure this was fetched properly
        em.getTransaction().rollback();
        em.close();
        return bases;
    }
    
    protected void assertRemoved(List<Object[]> list, Object[] expected) {
        Iterator<Object[]> iter = list.iterator();
        while (iter.hasNext()) {
            if (Arrays.deepEquals(expected, iter.next())) {
                iter.remove();
                return;
            }
        }
        
        Assert.fail(Arrays.deepToString(list.toArray()) + " does not contain expected entry: " + Arrays.deepToString(expected));
    }
    
    protected void assertRemoved(List<? extends Object> list, Object expected) {
        if (list.remove(expected)) {
            return;
        }
        
        Assert.fail(list + " does not contain expected entry: " + expected);
    }
    
}
