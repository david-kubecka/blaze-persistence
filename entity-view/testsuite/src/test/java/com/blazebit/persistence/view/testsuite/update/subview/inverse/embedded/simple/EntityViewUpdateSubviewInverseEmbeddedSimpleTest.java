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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionEmbeddable;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.LegacyOrderIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.LegacyOrderPositionDefaultIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.UpdatableLegacyOrderPositionEmbeddableView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.LegacyOrderPositionIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.UpdatableLegacyOrderPositionDefaultView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.UpdatableLegacyOrderPositionView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model.UpdatableLegacyOrderView;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseEmbeddedSimpleTest extends AbstractEntityViewUpdateTest<UpdatableLegacyOrderView> {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                LegacyOrder.class,
                LegacyOrderPosition.class,
                LegacyOrderPositionId.class,
                LegacyOrderPositionDefault.class,
                LegacyOrderPositionDefaultId.class,
                LegacyOrderPositionElement.class,
                LegacyOrderPositionEmbeddable.class
        };
    }

    public EntityViewUpdateSubviewInverseEmbeddedSimpleTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableLegacyOrderView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(LegacyOrderIdView.class);
        cfg.addEntityView(LegacyOrderPositionIdView.class);
        cfg.addEntityView(LegacyOrderPositionIdView.Id.class);
        cfg.addEntityView(LegacyOrderPositionDefaultIdView.class);
        cfg.addEntityView(LegacyOrderPositionDefaultIdView.Id.class);
        cfg.addEntityView(UpdatableLegacyOrderView.class);
        cfg.addEntityView(UpdatableLegacyOrderPositionView.class);
        cfg.addEntityView(UpdatableLegacyOrderPositionDefaultView.class);
        cfg.addEntityView(UpdatableLegacyOrderPositionEmbeddableView.class);
    }

    @Test
    public void testAddNewElementToCollection() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        update(newOrder);

        // When
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // Then
        em.clear();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 0), legacyOrder.getPositions().iterator().next().getId());
    }

    @Test
    public void testConvertEmbeddedIdEntityViewToEntityReference() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // When
        LegacyOrderPosition legacyOrderPosition = evm.getEntityReference(em, position);

        // Then
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 0), legacyOrderPosition.getId());
    }

    @Test
    public void testRemoveReadOnlyElementFromCollection() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // When
        em.clear();
        newOrder = evm.applySetting(EntityViewSetting.create(UpdatableLegacyOrderView.class), cbf.create(em, LegacyOrder.class)).getSingleResult();
        newOrder.getPositions().remove(newOrder.getPositions().iterator().next());
        PluralChangeModel<Object, Object> positionsChangeModel = (PluralChangeModel<Object, Object>) evm.getChangeModel(newOrder).get("positions");
        Assert.assertEquals(1, positionsChangeModel.getRemovedElements().size());
        update(newOrder);

        // Then
        em.clear();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(0, legacyOrder.getPositions().size());
    }

    @Test
    public void testPersistAndAddNewElementToCollection() {
        // When
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // Then
        // After update, the position is replaced with the declaration type
        assertFalse(newOrder.getPositions().iterator().next() instanceof UpdatableLegacyOrderPositionView);
        em.clear();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 0), legacyOrder.getPositions().iterator().next().getId());
    }

    @Test
    public void testPersistAndAddNewElementToNewCollection() {
        // When
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.setPositions(new HashSet<>(Arrays.<LegacyOrderPositionIdView>asList(position)));
        update(newOrder);

        // Then
        // After update, the position is replaced with the declaration type
        assertFalse(newOrder.getPositions().iterator().next() instanceof UpdatableLegacyOrderPositionView);
        em.clear();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 0), legacyOrder.getPositions().iterator().next().getId());
    }

    @Test
    public void testUpdateReferenceEmbeddable() {
        Assume.assumeFalse( "Partial reference updates don't work in full mode", isFullMode());

        // Given
        UpdatableLegacyOrderView order1 = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        position.getEmbeddable().setName("123");
        order1.getPositions().add(position);
        update(order1);

        UpdatableLegacyOrderView order2 = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position1 = evm.create(UpdatableLegacyOrderPositionView.class);
        position1.getId().setPositionId(0);
        position1.setArticleNumber("123");
        position1.getEmbeddable().setName("123");
        order2.getPositions().add(position1);
        UpdatableLegacyOrderPositionView position2 = evm.create(UpdatableLegacyOrderPositionView.class);
        position2.getId().setPositionId(1);
        position2.setArticleNumber("456");
        position2.getEmbeddable().setName("456");
        order2.getPositions().add(position2);
        update(order2);

        // When
        UpdatableLegacyOrderView order = evm.getReference(UpdatableLegacyOrderView.class, order2.getId());
        position = evm.getReference(UpdatableLegacyOrderPositionView.class, position2.getId());
        position.setEmbeddable(evm.create(UpdatableLegacyOrderPositionEmbeddableView.class));
        position.getEmbeddable().setName("test");
        order.getPositions().add(position);
        update(order);

        // Then
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, order2.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        LegacyOrderPosition orderPosition = legacyOrder.getPositions().iterator().next();
        Assert.assertEquals(new LegacyOrderPositionId(order2.getId(), position2.getId().getPositionId()), orderPosition.getId());
        assertEquals("456", orderPosition.getArticleNumber());
        assertEquals("test", orderPosition.getEmbeddable().getName());
    }

    @Test
    public void testFixConstraintViolationErrorOnInverseCollectionElement() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        newOrder.getPositions().add(position);
        try {
            update(newOrder);
            Assert.fail("Expected the transaction to fail!");
        } catch (Exception ex) {
            // When
            em.clear();
            position.setArticleNumber("123");
            update(newOrder);
        }

        // Then
        em.clear();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals("123", legacyOrder.getPositions().iterator().next().getArticleNumber());
    }

    @Test
    public void testLoadMultipleUpdatableWithUpdatableFlatViewIds() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                LegacyOrder o = new LegacyOrder();
                em.persist(o);

                LegacyOrderPosition p = new LegacyOrderPosition(new LegacyOrderPositionId(o.getId(), 0));
                p.setArticleNumber("123");
                p.setOrder(o);
                em.persist(p);
                o.getPositions().add(p);

                LegacyOrderPositionDefault d1 = new LegacyOrderPositionDefault(new LegacyOrderPositionDefaultId(p.getId(), 1));
                d1.setValue("1");
                em.persist(d1);

                LegacyOrderPositionDefault d2 = new LegacyOrderPositionDefault(new LegacyOrderPositionDefaultId(p.getId(), 2));
                d2.setValue("1");
                em.persist(d2);

                p.getDefaults().add(d1);
                p.getDefaults().add(d2);
            }
        });

        em.clear();
        List<UpdatableLegacyOrderPositionDefaultView> resultList = evm.applySetting(
                EntityViewSetting.create(UpdatableLegacyOrderPositionDefaultView.class),
                cbf.create(em, LegacyOrderPositionDefault.class)
        ).getResultList();
        // Making the id view updatable at some point caused just a part of the id to be considered when de-duplicating updatable views
        assertEquals(2, resultList.size());
        assertNotEquals(resultList.get(0).getId(), resultList.get(1).getId());
    }

    @Test
    public void testReplaceInverseCollectionElementWithNewElement() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);
        em.clear();

        // When
        position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setOrderId(newOrder.getId());
        position.getId().setPositionId(0);
        position.setArticleNumber("456");
        newOrder.getPositions().clear();
        newOrder.getPositions().add(position);
        update(newOrder);
        em.clear();

        // Then
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        Assert.assertEquals("456", legacyOrder.getPositions().iterator().next().getArticleNumber());
    }

    @Override
    protected void reload() {
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Document.class)
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        fullFetch(builder)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Document.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class);
        if (version) {
            builder.update(Document.class);
        }

        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
