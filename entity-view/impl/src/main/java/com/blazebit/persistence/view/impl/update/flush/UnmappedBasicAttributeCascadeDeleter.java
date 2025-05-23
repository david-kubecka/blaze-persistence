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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UnmappedBasicAttributeCascadeDeleter extends AbstractUnmappedAttributeCascadeDeleter {

    private final String ownerIdAttributeName;
    private final String deleteQuery;
    private final String deleteByOwnerIdQuery;
    private final boolean requiresDeleteCascadeAfterRemove;
    private final boolean requiresDeleteAsEntity;
    private final UnmappedAttributeCascadeDeleter[] unmappedPreRemoveCascadeDeleters;
    private final UnmappedAttributeCascadeDeleter[] unmappedPostRemoveCascadeDeleters;

    public UnmappedBasicAttributeCascadeDeleter(EntityViewManagerImpl evm, String attributeName, ExtendedAttribute<?, ?> attribute, String ownerIdAttributeName, boolean disallowCycle) {
        super(evm, attributeName, attribute);
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ExtendedManagedType extendedManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, elementEntityClass);
        EntityType<?> entityType = (EntityType<?>) extendedManagedType.getType();
        this.requiresDeleteCascadeAfterRemove = !attribute.isForeignJoinColumn();
        this.ownerIdAttributeName = ownerIdAttributeName;
        this.deleteQuery = "DELETE FROM " + entityType.getName() + " e WHERE e." + elementIdAttributeName + " = :id";
        this.deleteByOwnerIdQuery = "DELETE FROM " + entityType.getName() + " e WHERE e." + ownerIdAttributeName + " = :ownerId";

        if (elementIdAttributeName == null) {
            this.requiresDeleteAsEntity = false;
            this.unmappedPreRemoveCascadeDeleters = this.unmappedPostRemoveCascadeDeleters = EMPTY;
        } else {
            // If the attribute introduces a cycle, we can't construct pre- and post-deleters. We must do entity deletion, otherwise we'd get a stack overflow
            if (disallowCycle && attribute.hasCascadingDeleteCycle()) {
                this.requiresDeleteAsEntity = true;
                this.unmappedPreRemoveCascadeDeleters = this.unmappedPostRemoveCascadeDeleters = EMPTY;
            } else {
                List<UnmappedAttributeCascadeDeleter> unmappedCascadeDeleters = UnmappedAttributeCascadeDeleterUtil.createUnmappedCascadeDeleters(evm, elementEntityClass, elementIdAttributeName);
                List<UnmappedAttributeCascadeDeleter> unmappedPreRemoveCascadeDeleters = new ArrayList<>(unmappedCascadeDeleters.size());
                List<UnmappedAttributeCascadeDeleter> unmappedPostRemoveCascadeDeleters = new ArrayList<>(unmappedCascadeDeleters.size());
                for (UnmappedAttributeCascadeDeleter deleter : unmappedCascadeDeleters) {
                    if (deleter.requiresDeleteCascadeAfterRemove()) {
                        unmappedPostRemoveCascadeDeleters.add(deleter);
                    } else {
                        unmappedPreRemoveCascadeDeleters.add(deleter);
                    }
                }

                this.requiresDeleteAsEntity = false;
                this.unmappedPreRemoveCascadeDeleters = unmappedPreRemoveCascadeDeleters.toArray(new UnmappedAttributeCascadeDeleter[unmappedPreRemoveCascadeDeleters.size()]);
                this.unmappedPostRemoveCascadeDeleters = unmappedPostRemoveCascadeDeleters.toArray(new UnmappedAttributeCascadeDeleter[unmappedPostRemoveCascadeDeleters.size()]);
            }
        }
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return requiresDeleteCascadeAfterRemove;
    }

    @Override
    public void removeById(UpdateContext context, Object id) {
        if (context.invokePreRemove(elementEntityClass, id)) {
            for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
            }
            removeWithoutPreCascadeDelete(context, null, null, id);
        }
    }

    public void remove(UpdateContext context, Object o) {
        if (context.hasRemoveListeners(elementEntityClass)) {
            PersistenceUnitUtil persistenceUnitUtil = context.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
            Object identifier = persistenceUnitUtil.getIdentifier(o);
            if (context.invokePreRemove(elementEntityClass, identifier)) {
                context.getEntityManager().remove(o);
                context.invokePostRemove(elementEntityClass, identifier);
            }
        } else {
            context.getEntityManager().remove(o);
        }
    }

    @Override
    public void removeByOwnerId(UpdateContext context, Object ownerId) {
        if (requiresDeleteAsEntity) {
            CriteriaBuilder<?> cb = context.getEntityViewManager().getCriteriaBuilderFactory().create(context.getEntityManager(), elementEntityClass);
            cb.where(ownerIdAttributeName).eq(ownerId);

            if (context.hasRemoveListeners(elementEntityClass)) {
                PersistenceUnitUtil persistenceUnitUtil = context.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
                for (Object o : cb.getResultList()) {
                    Object identifier = persistenceUnitUtil.getIdentifier(o);
                    if (context.invokePreRemove(elementEntityClass, identifier)) {
                        context.getEntityManager().remove(o);
                        context.invokePostRemove(elementEntityClass, identifier);
                    }
                }
            } else {
                for (Object o : cb.getResultList()) {
                    context.getEntityManager().remove(o);
                }
            }

            // We need to flush here, otherwise the deletion will be deferred and might cause a constraint violation
            context.getEntityManager().flush();
        } else {
            if (unmappedPreRemoveCascadeDeleters.length != 0) {
                // If we have pre remove cascade deleters, we need to query the id first so we can remove these elements
                List<String> returningAttributes = new ArrayList<>();
                for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                    returningAttributes.add(unmappedPostRemoveCascadeDeleters[i].getAttributeValuePath());
                }

                CriteriaBuilder<Object[]> cb = context.getEntityViewManager().getCriteriaBuilderFactory().create(context.getEntityManager(), Object[].class);
                cb.from(elementEntityClass);
                cb.where(ownerIdAttributeName).eq(ownerId);
                for (String attribute : returningAttributes) {
                    cb.select(attribute);
                }
                cb.select(elementIdAttributeName);
                List<Object[]> resultList = cb.getResultList();
                if (!resultList.isEmpty() && resultList.get(0) instanceof Object[]) {
                    if (context.hasRemoveListeners(elementEntityClass)) {
                        for (Object[] returnedValues : resultList) {
                            Object id = returnedValues[returnedValues.length - 1];

                            if (context.invokePreRemove(elementEntityClass, id)) {
                                for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                                    unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
                                }
                                removeWithoutPreCascadeDelete(context, ownerId, returnedValues, id);
                            }
                        }
                    } else {
                        for (Object[] returnedValues : resultList) {
                            Object id = returnedValues[returnedValues.length - 1];

                            for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                                unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
                            }
                            removeWithoutPreCascadeDelete(context, ownerId, returnedValues, id);
                        }
                    }
                } else {
                    // Hibernate returns the scalar value directly when using only a single select item
                    if (context.hasRemoveListeners(elementEntityClass)) {
                        for (Object id : resultList) {
                            if (context.invokePreRemove(elementEntityClass, id)) {
                                for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                                    unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
                                }
                                removeWithoutPreCascadeDelete(context, ownerId, null, id);
                            }
                        }
                    } else {
                        for (Object id : resultList) {
                            for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                                unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, id);
                            }
                            removeWithoutPreCascadeDelete(context, ownerId, null, id);
                        }
                    }
                }
            } else {
                if (context.hasRemoveListeners(elementEntityClass)) {
                    CriteriaBuilder<Object[]> cb = context.getEntityViewManager().getCriteriaBuilderFactory().create(context.getEntityManager(), Object[].class);
                    cb.from(elementEntityClass);
                    cb.where(ownerIdAttributeName).eq(ownerId);
                    cb.select(elementIdAttributeName);
                    List<Object[]> resultList = cb.getResultList();

                    for (Object id : resultList) {
                        if (context.invokePreRemove(elementEntityClass, id)) {
                            removeWithoutPreCascadeDelete(context, ownerId, null, id);
                        }
                    }
                } else {
                    removeWithoutPreCascadeDelete(context, ownerId, null, null);
                }
            }
        }
    }

    private void removeWithoutPreCascadeDelete(UpdateContext context, Object ownerId, Object[] returnedValues, Object id) {
        List<Object[]> returnedValuesList;
        // need to "return" the values from the delete query for the post deleters since the values aren't available after executing the delete query
        if (unmappedPostRemoveCascadeDeleters.length != 0 && returnedValues == null) {
            List<String> returningAttributes = new ArrayList<>();
            for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                returningAttributes.add(unmappedPostRemoveCascadeDeleters[i].getAttributeValuePath());
            }

            EntityViewManagerImpl evm = context.getEntityViewManager();
            // If the dbms supports it, we use the returning feature to do this
            if (evm.getDbmsDialect().supportsReturningColumns()) {
                DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().delete(context.getEntityManager(), elementEntityClass);
                if (id == null) {
                    cb.where(ownerIdAttributeName).eq(ownerId);
                } else {
                    cb.where(elementIdAttributeName).eq(id);
                }

                ReturningResult<Tuple> result = cb.executeWithReturning(returningAttributes.toArray(new String[returningAttributes.size()]));
                // We make sure in the caller, that when there are post remove transition listeners, we have an id
                context.invokePostRemove(elementEntityClass, id);
                returnedValuesList = new ArrayList<>();
                for (Tuple tuple : result.getResultList()) {
                    returnedValues = tuple.toArray();
                    returnedValuesList.add(returnedValues);
                }
            } else {
                // Otherwise we query the attributes
                CriteriaBuilder<Object[]> cb = evm.getCriteriaBuilderFactory().create(context.getEntityManager(), Object[].class);
                cb.from(elementEntityClass);
                if (id == null) {
                    cb.where(ownerIdAttributeName).eq(ownerId);
                } else {
                    cb.where(elementIdAttributeName).eq(id);
                }
                for (String attribute : returningAttributes) {
                    cb.select(attribute);
                }
                cb.select(elementIdAttributeName);
                returnedValuesList = new ArrayList<>();
                for (Object[] objects : cb.getResultList()) {
                    returnedValues = objects;
                    returnedValuesList.add(returnedValues);
                    id = returnedValues[returnedValues.length - 1];
                    deleteElement(context, ownerId, id);
                    context.invokePostRemove(elementEntityClass, id);
                }
            }
        } else {
            deleteElement(context, ownerId, id);
            context.invokePostRemove(elementEntityClass, id);
            returnedValuesList = returnedValues == null ? Collections.<Object[]>emptyList() : Collections.singletonList(returnedValues);
        }

        for (int i = 0; i < returnedValuesList.size(); i++) {
            Object[] objects = returnedValuesList.get(i);
            for (int j = 0; j < unmappedPostRemoveCascadeDeleters.length; j++) {
                if (objects[j] != null) {
                    unmappedPostRemoveCascadeDeleters[j].removeById(context, objects[j]);
                }
            }
        }
    }

    private void deleteElement(UpdateContext context, Object ownerId, Object id) {
        if (requiresDeleteAsEntity) {
            if (id == null) {
                throw new UnsupportedOperationException("Delete by owner id should not be invoked!");
            }
            context.getEntityManager().remove(context.getEntityManager().getReference(elementEntityClass, id));
        } else {
            if (id == null) {
                Query query = context.getEntityManager().createQuery(deleteByOwnerIdQuery);
                query.setParameter("ownerId", ownerId);
                query.executeUpdate();
            } else {
                Query query = context.getEntityManager().createQuery(deleteQuery);
                query.setParameter("id", id);
                query.executeUpdate();
            }
        }
    }

    @Override
    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter() {
        return this;
    }
}
