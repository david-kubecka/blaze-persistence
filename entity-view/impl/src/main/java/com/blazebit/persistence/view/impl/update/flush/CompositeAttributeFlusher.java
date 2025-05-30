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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.OptimisticLockException;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.change.DirtyChecker;
import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.entity.EntityLoader;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.impl.entity.FlusherBasedEntityLoader;
import com.blazebit.persistence.view.impl.entity.ReferenceEntityLoader;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.mapper.ViewMapper;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.UpdateQueryFactory;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.DirtyStateTrackable;
import com.blazebit.persistence.view.spi.type.DirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompositeAttributeFlusher extends CompositeAttributeFetchGraphNode<CompositeAttributeFlusher, DirtyAttributeFlusher<?, Object, Object>> implements DirtyAttributeFlusher<CompositeAttributeFlusher, Object, Object> {

    private static final Runnable EMPTY_RUNNABLE = new Runnable() {
        @Override
        public void run() {
        }
    };
    private static final int FEATURE_SUPPORTS_QUERY_FLUSH = 0;
    private static final int FEATURE_HAS_PASS_THROUGH_FLUSHER = 1;
    private static final int FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED = 2;
    private static final int FEATURE_LOAD_FOR_ENTITY_FLUSH = 3;
    private static final UnmappedAttributeCascadeDeleter[] EMPTY = new UnmappedAttributeCascadeDeleter[0];

    private final Class<?> entityClass;
    private final boolean persistable;
    private final ViewMapper<Object, Object> persistViewMapper;
    private final javax.persistence.metamodel.SingularAttribute<?, ?> jpaIdAttribute;
    private final ViewToEntityMapper viewIdMapper;
    private final AttributeAccessor viewIdAccessor;
    private final AttributeAccessor entityIdAccessor;
    private final EntityTupleizer tupleizer;
    private final EntityLoader jpaIdInstantiator;
    private final ObjectBuilder<Object> idViewBuilder;
    private final DirtyAttributeFlusher<?, Object, Object> idFlusher;
    private final VersionAttributeFlusher<Object, Object> versionFlusher;
    private final DirtyAttributeFlusher<?, Object, Object>[] fullFlushers;
    // split in pre- and post-object remove based on requiresDeleteCascadeAfterRemove()
    private final UnmappedAttributeCascadeDeleter[] unmappedPreRemoveCascadeDeleters;
    private final UnmappedAttributeCascadeDeleter[] unmappedPostRemoveCascadeDeleters;
    private final UnmappedOwnerAwareDeleter[] unmappedOwnerAwareCascadeDeleters;
    private final FlushMode flushMode;
    private final FlushStrategy flushStrategy;
    private final EntityLoader entityLoader;
    private final EntityLoader referenceEntityLoader;
    private final String deleteQuery;
    private final String versionedDeleteQuery;
    private final String lockOwner;
    private final boolean supportsQueryFlush;
    private final boolean loadForEntityFlush;
    private final boolean hasPassThroughFlushers;
    private final boolean optimisticLockProtected;

    private final Object element;

    @SuppressWarnings("unchecked")
    public CompositeAttributeFlusher(EntityViewManagerImpl evm, Class<?> viewType, Class<?> entityClass, ManagedType<?> managedType, boolean persistable, ViewMapper<Object, Object> persistViewMapper, SingularAttribute<?, ?> jpaIdAttribute, SingularAttribute<?, ?> viewIdMappingAttribute, AttributeAccessor entityIdAccessor,
                                     ViewToEntityMapper viewIdMapper, AttributeAccessor viewIdAccessor, EntityTupleizer tupleizer, EntityLoader jpaIdInstantiator, ObjectBuilder<Object> idViewBuilder, DirtyAttributeFlusher<?, Object, Object> idFlusher, VersionAttributeFlusher<Object, Object> versionFlusher,
                                     String lockOwner, UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers, UnmappedAttributeCascadeDeleter[][] flusherWiseCascadeDeleteUnmappedFlushers, DirtyAttributeFlusher[] flushers, FlushMode flushMode, FlushStrategy flushStrategy) {
        super(viewType, flushers, null);
        this.entityClass = entityClass;
        this.persistable = persistable;
        this.persistViewMapper = persistViewMapper;
        this.jpaIdAttribute = jpaIdAttribute;
        this.viewIdMapper = viewIdMapper;
        this.viewIdAccessor = viewIdAccessor;
        this.entityIdAccessor = entityIdAccessor;
        this.tupleizer = tupleizer;
        this.jpaIdInstantiator = jpaIdInstantiator;
        this.idViewBuilder = idViewBuilder;
        this.idFlusher = idFlusher;
        this.versionFlusher = versionFlusher;
        this.fullFlushers = flushers;
        this.unmappedPreRemoveCascadeDeleters = getPreRemoveFlushers(cascadeDeleteUnmappedFlushers);
        this.unmappedPostRemoveCascadeDeleters = getPostRemoveFlushers(cascadeDeleteUnmappedFlushers);
        this.unmappedOwnerAwareCascadeDeleters = getOwnerAwareDeleters(flusherWiseCascadeDeleteUnmappedFlushers);
        this.flushMode = flushMode;
        this.flushStrategy = flushStrategy;
        this.lockOwner = lockOwner;
        this.entityLoader = new FlusherBasedEntityLoader(evm, entityClass, jpaIdAttribute, viewIdMapper, entityIdAccessor, flushers);
        this.referenceEntityLoader = new ReferenceEntityLoader(evm, entityClass, jpaIdAttribute, viewIdMappingAttribute, viewIdMapper, entityIdAccessor, false);
        this.deleteQuery = createDeleteQuery(managedType, jpaIdAttribute);
        this.versionedDeleteQuery = createVersionedDeleteQuery(deleteQuery, versionFlusher);
        boolean[] features = determineFeatures(flushStrategy, flushers);
        this.supportsQueryFlush = features[FEATURE_SUPPORTS_QUERY_FLUSH];
        this.loadForEntityFlush = features[FEATURE_LOAD_FOR_ENTITY_FLUSH];
        this.hasPassThroughFlushers = features[FEATURE_HAS_PASS_THROUGH_FLUSHER];
        this.optimisticLockProtected = features[FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED];
        this.element = null;
    }

    private CompositeAttributeFlusher(EntityViewManagerImpl evm, CompositeAttributeFlusher original, DirtyAttributeFlusher[] flushers, Object element, boolean persist) {
        super(original.viewType, original.attributeIndexMapping, flushers, persist);
        this.entityClass = original.entityClass;
        this.persistable = original.persistable;
        this.persistViewMapper = original.persistViewMapper;
        this.jpaIdAttribute = original.jpaIdAttribute;
        this.viewIdMapper = original.viewIdMapper;
        this.viewIdAccessor = original.viewIdAccessor;
        this.entityIdAccessor = original.entityIdAccessor;
        this.tupleizer = original.tupleizer;
        this.jpaIdInstantiator = original.jpaIdInstantiator;
        this.idViewBuilder = original.idViewBuilder;
        this.idFlusher = original.idFlusher;
        this.fullFlushers = original.fullFlushers;
        this.unmappedPreRemoveCascadeDeleters = original.unmappedPreRemoveCascadeDeleters;
        this.unmappedPostRemoveCascadeDeleters = original.unmappedPostRemoveCascadeDeleters;
        this.unmappedOwnerAwareCascadeDeleters = original.unmappedOwnerAwareCascadeDeleters;
        this.flushMode = original.flushMode;
        this.flushStrategy = original.flushStrategy;
        this.lockOwner = original.lockOwner;
        this.entityLoader = new FlusherBasedEntityLoader(evm, entityClass, jpaIdAttribute, viewIdMapper, entityIdAccessor, flushers);
        this.referenceEntityLoader = original.referenceEntityLoader;
        this.deleteQuery = original.deleteQuery;
        boolean[] features = determineFeatures(flushStrategy, flushers);
        this.supportsQueryFlush = features[FEATURE_SUPPORTS_QUERY_FLUSH];
        this.loadForEntityFlush = features[FEATURE_LOAD_FOR_ENTITY_FLUSH];
        this.hasPassThroughFlushers = features[FEATURE_HAS_PASS_THROUGH_FLUSHER];
        this.element = element;
        // When flushing references that have no version set, we do a normal flush, not a versioned one
        if (original.versionFlusher == null || original.versionFlusher.getViewAttributeAccessor().getValue(element) == null) {
            this.versionedDeleteQuery = null;
            this.optimisticLockProtected = false;
        } else {
            this.versionedDeleteQuery = original.versionedDeleteQuery;
            this.optimisticLockProtected = features[FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED];
        }
        this.versionFlusher = original.versionFlusher;
    }

    private UnmappedOwnerAwareDeleter[] getOwnerAwareDeleters(UnmappedAttributeCascadeDeleter[][] flusherWiseCascadeDeleteUnmappedFlushers) {
        if (flusherWiseCascadeDeleteUnmappedFlushers == null) {
            return null;
        }
        UnmappedOwnerAwareDeleter[] preRemoveCascadeDeleteUnmappedFlushers = new UnmappedOwnerAwareDeleter[flusherWiseCascadeDeleteUnmappedFlushers.length];
        boolean hasDeleters = false;
        for (int i = 0; i < flusherWiseCascadeDeleteUnmappedFlushers.length; i++) {
            UnmappedAttributeCascadeDeleter[] preRemoveFlushers = getPreRemoveFlushers(flusherWiseCascadeDeleteUnmappedFlushers[i]);
            UnmappedAttributeCascadeDeleter[] postRemoveFlushers = getPostRemoveFlushers(flusherWiseCascadeDeleteUnmappedFlushers[i]);
            if (preRemoveFlushers == null && postRemoveFlushers == null) {
                preRemoveCascadeDeleteUnmappedFlushers[i] = null;
            } else {
                preRemoveCascadeDeleteUnmappedFlushers[i] = new UnmappedOwnerAwareDeleter(idFlusher, preRemoveFlushers, postRemoveFlushers);
                hasDeleters = true;
            }
        }

        if (hasDeleters) {
            return preRemoveCascadeDeleteUnmappedFlushers;
        }

        return null;
    }

    private UnmappedAttributeCascadeDeleter[] getPreRemoveFlushers(UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers) {
        if (cascadeDeleteUnmappedFlushers == null || cascadeDeleteUnmappedFlushers.length == 0) {
            return EMPTY;
        }
        List<UnmappedAttributeCascadeDeleter> flusherList = new ArrayList<>(cascadeDeleteUnmappedFlushers.length);
        for (UnmappedAttributeCascadeDeleter flusher : cascadeDeleteUnmappedFlushers) {
            if (!flusher.requiresDeleteCascadeAfterRemove()) {
                flusherList.add(flusher);
            }
        }

        return flusherList.toArray(new UnmappedAttributeCascadeDeleter[flusherList.size()]);
    }

    private UnmappedAttributeCascadeDeleter[] getPostRemoveFlushers(UnmappedAttributeCascadeDeleter[] cascadeDeleteUnmappedFlushers) {
        if (cascadeDeleteUnmappedFlushers == null || cascadeDeleteUnmappedFlushers.length == 0) {
            return EMPTY;
        }
        List<UnmappedAttributeCascadeDeleter> flusherList = new ArrayList<>(cascadeDeleteUnmappedFlushers.length);
        for (UnmappedAttributeCascadeDeleter flusher : cascadeDeleteUnmappedFlushers) {
            if (flusher.requiresDeleteCascadeAfterRemove()) {
                flusherList.add(flusher);
            }
        }

        return flusherList.toArray(new UnmappedAttributeCascadeDeleter[flusherList.size()]);
    }

    private String createDeleteQuery(ManagedType<?> managedType, SingularAttribute<?, ?> jpaIdAttribute) {
        if (managedType instanceof EntityType<?> && jpaIdAttribute != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ").append(((EntityType) managedType).getName()).append(" e WHERE ");
            idFlusher.appendUpdateQueryFragment(null, sb, "e.", EntityViewUpdaterImpl.WHERE_CLAUSE_PREFIX, " AND ");
            return sb.toString();
        }

        return null;
    }

    private String createVersionedDeleteQuery(String deleteQuery, DirtyAttributeFlusher versionFlusher) {
        if (deleteQuery != null && versionFlusher != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(deleteQuery);
            sb.append(" AND ");
            versionFlusher.appendUpdateQueryFragment(null, sb, "e.", EntityViewUpdaterImpl.WHERE_CLAUSE_PREFIX, " AND ");
            return sb.toString();
        }

        return null;
    }

    private static boolean[] determineFeatures(FlushStrategy flushStrategy, DirtyAttributeFlusher[] flushers) {
        boolean hasPassThroughFlusher = false;
        boolean supportsQueryFlush = flushStrategy != FlushStrategy.ENTITY;
        boolean anyOptimisticLockProtected = false;
        boolean loadForEntityFlush = false;
        for (int i = 0; i < flushers.length; i++) {
            final DirtyAttributeFlusher<?, ?, ?> f = flushers[i];
            if (f != null) {
                hasPassThroughFlusher = hasPassThroughFlusher || f.isPassThrough();
                supportsQueryFlush = supportsQueryFlush && f.supportsQueryFlush();
                anyOptimisticLockProtected = anyOptimisticLockProtected || f.isOptimisticLockProtected();
                loadForEntityFlush = loadForEntityFlush || f.loadForEntityFlush() && (flushStrategy == FlushStrategy.ENTITY || !f.supportsQueryFlush());
            }
        }

        boolean[] features = new boolean[4];
        features[FEATURE_HAS_PASS_THROUGH_FLUSHER] = hasPassThroughFlusher;
        features[FEATURE_SUPPORTS_QUERY_FLUSH] = supportsQueryFlush;
        features[FEATURE_IS_ANY_OPTIMISTIC_LOCK_PROTECTED] = anyOptimisticLockProtected;
        features[FEATURE_LOAD_FOR_ENTITY_FLUSH] = loadForEntityFlush;
        return features;
    }

    public Class<?> getViewTypeClass() {
        return viewType;
    }

    public boolean hasVersionFlusher() {
        return versionFlusher != null;
    }

    @Override
    public Object cloneDeep(Object view, Object oldValue, Object newValue) {
        return newValue;
    }

    @Override
    public Object getNewInitialValue(UpdateContext context, Object clonedValue, Object currentValue) {
        return currentValue;
    }

    @Override
    public boolean appendUpdateQueryFragment(UpdateContext context, StringBuilder sb, String mappingPrefix, String parameterPrefix, String separator) {
        int clauseEndIndex = sb.length();

        boolean wasDirty = false;
        boolean optimisticLock = false;
        for (int i = 0; i < flushers.length; i++) {
            DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
            if (flusher != null) {
                int endIndex = sb.length();
                if (flusher.appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator)) {
                    wasDirty = true;
                    optimisticLock |= flusher.isOptimisticLockProtected();
                }

                // If something was appended, we also append a comma
                if (endIndex != sb.length()) {
                    clauseEndIndex = sb.length();
                    sb.append(separator);
                }
            }
        }
        if (optimisticLock && optimisticLockProtected && versionFlusher != null) {
            versionFlusher.appendUpdateQueryFragment(context, sb, mappingPrefix, parameterPrefix, separator);
            // If something was appended, we also append a comma
            if (clauseEndIndex != sb.length()) {
                clauseEndIndex = sb.length();
                sb.append(separator);
            }
        }

        if (clauseEndIndex + separator.length() == sb.length()) {
            // Remove the last comma
            sb.setLength(clauseEndIndex);
        }
        return wasDirty;
    }

    @Override
    public boolean supportsQueryFlush() {
        return supportsQueryFlush;
    }

    @Override
    public boolean loadForEntityFlush() {
        return loadForEntityFlush;
    }

    @Override
    public Query flushQuery(UpdateContext context, String parameterPrefix, UpdateQueryFactory queryFactory, Query query, Object ownerView, Object view, Object value, UnmappedOwnerAwareDeleter ownerAwareDeleter, DirtyAttributeFlusher<?, ?, ?> ownerFlusher) {
        if (element != null) {
            value = element;
        } else if (value == null) {
            if (idFlusher != null) {
                return null;
            }
            // If we get here, we have an embeddable that is null now

            context.invokePreRemove((EntityViewProxy) view);
            if (ownerAwareDeleter != null) {
                ownerAwareDeleter.preDelete(context, (EntityViewProxy) view);
            }

            if (query == null) {
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
            }
            List<Integer> deferredFlushers = null;
            for (int i = 0; i < flushers.length; i++) {
                DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, view, null, ownerAwareDeleter == null ? null : ownerAwareDeleter.getSubDeleter(flusher), ownerFlusher);
                }
            }
            if (deferredFlushers != null) {
                for (int i = 0; i < deferredFlushers.size(); i++) {
                    final int index = deferredFlushers.get(i);
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                    query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, view, null, ownerAwareDeleter == null ? null : ownerAwareDeleter.getSubDeleter(flusher), ownerFlusher);
                }
            }

            if (ownerAwareDeleter != null) {
                ownerAwareDeleter.postDelete(context, (EntityViewProxy) view);
            }
            context.invokePostRemove((EntityViewProxy) view);

            return query;
        } else if (!(value instanceof MutableStateTrackable)) {
            // Pass-through i.e. read-only id attributes
            if (query == null) {
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
            }
            for (int i = 0; i < flushers.length; i++) {
                DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, view, flusher.getViewAttributeAccessor().getValue(value), ownerAwareDeleter, ownerFlusher);
                }
            }
            return query;
        }

        MutableStateTrackable element = (MutableStateTrackable) value;
        // Already removed objects or objects without a parent can't be flushed
        // The root object, which is given when view == value, is the exception
        // Embeddables/flat views are also an exception, we always need to flush these
        if (idFlusher != null && (context.isRemovedObject(element) || !element.$$_hasParent() && view != value)) {
            return null;
        }
        // Object persisting only works via entity flushing
        boolean shouldPersist = persistable && (persist == Boolean.TRUE || persist == null && element.$$_isNew());
        if (shouldPersist) {
            flushEntity(context, null, ownerView, value, value, null);
            return null;
        }

        Object[] state = element.$$_getMutableState();
        boolean optimisticLock = false;
        List<Integer> deferredFlushers = null;

        if (value instanceof DirtyStateTrackable) {
            // After Pre-Update the dirtyness could change
            if (preUpdate(context, element)) {
                Query oldQuery = query;
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
                if (oldQuery != null) {
                    for (Parameter<?> parameter : oldQuery.getParameters()) {
                        if (oldQuery.isBound(parameter)) {
                            query.setParameter(parameter.getName(), oldQuery.getParameterValue(parameter));
                        }
                    }
                }
            }

            Object[] initialState = ((DirtyStateTrackable) value).$$_getInitialState();
            context.getInitialStateResetter().addState(initialState, initialState.clone());

            if (query == null && queryFactory != null) {
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
            }
            for (int i = 0; i < state.length; i++) {
                DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    if (flusher.requiresDeferredFlush(state[i])) {
                        if (deferredFlushers == null) {
                            deferredFlushers = new ArrayList<>();
                        }
                        deferredFlushers.add(i);
                        optimisticLock |= flusher.isOptimisticLockProtected();
                    } else {
                        optimisticLock |= flusher.isOptimisticLockProtected();
                        Object newInitialValue = flusher.cloneDeep(value, initialState[i], state[i]);
                        query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, state[i], unmappedOwnerAwareCascadeDeleters == null ? null : unmappedOwnerAwareCascadeDeleters[i], ownerFlusher);
                        initialState[i] = flusher.getNewInitialValue(context, newInitialValue, state[i]);
                    }
                }
            }
        } else {
            // After Pre-Update the dirtyness could change
            if (preUpdate(context, element)) {
                Query oldQuery = query;
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
                if (oldQuery != null) {
                    for (Parameter<?> parameter : oldQuery.getParameters()) {
                        if (oldQuery.isBound(parameter)) {
                            query.setParameter(parameter.getName(), oldQuery.getParameterValue(parameter));
                        }
                    }
                }
            }
            if (query == null) {
                query = queryFactory.createUpdateQuery(context, (MutableStateTrackable) ownerView, ownerFlusher);
            }
            for (int i = 0; i < state.length; i++) {
                DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    if (flusher.requiresDeferredFlush(state[i])) {
                        if (deferredFlushers == null) {
                            deferredFlushers = new ArrayList<>();
                        }
                        deferredFlushers.add(i);
                        optimisticLock |= flusher.isOptimisticLockProtected();
                    } else {
                        optimisticLock |= flusher.isOptimisticLockProtected();
                        query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, state[i], unmappedOwnerAwareCascadeDeleters == null ? null : unmappedOwnerAwareCascadeDeleters[i], ownerFlusher);
                    }
                }
            }
        }

        // Pass through flushers
        for (int i = state.length; i < flushers.length; i++) {
            DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
            if (flusher != null) {
                optimisticLock |= flusher.isOptimisticLockProtected();
                query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, flusher.getViewAttributeAccessor().getValue(value), unmappedOwnerAwareCascadeDeleters == null ? null : unmappedOwnerAwareCascadeDeleters[i], ownerFlusher);
            }
        }

        if (optimisticLock && optimisticLockProtected && versionFlusher != null) {
            context.getInitialStateResetter().addVersionedView(element, element.$$_getVersion());
            versionFlusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, element.$$_getVersion(), null, ownerFlusher);
        }

        if (deferredFlushers != null) {
            if (value instanceof DirtyStateTrackable) {
                Object[] initialState = ((DirtyStateTrackable) value).$$_getInitialState();
                context.getInitialStateResetter().addState(initialState, initialState.clone());

                for (int i = 0; i < deferredFlushers.size(); i++) {
                    final int index = deferredFlushers.get(i);
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                    if (flusher != null) {
                        optimisticLock |= flusher.isOptimisticLockProtected();
                        Object newInitialValue = flusher.cloneDeep(value, initialState[index], state[index]);
                        query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, state[index], unmappedOwnerAwareCascadeDeleters == null ? null : unmappedOwnerAwareCascadeDeleters[index], ownerFlusher);
                        initialState[index] = flusher.getNewInitialValue(context, newInitialValue, state[index]);
                    }
                }
            } else {
                for (int i = 0; i < deferredFlushers.size(); i++) {
                    final int index = deferredFlushers.get(i);
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                    if (flusher != null) {
                        optimisticLock |= flusher.isOptimisticLockProtected();
                        query = flusher.flushQuery(context, parameterPrefix, queryFactory, query, ownerView, value, state[index], unmappedOwnerAwareCascadeDeleters == null ? null : unmappedOwnerAwareCascadeDeleters[index], ownerFlusher);
                    }
                }
            }
        }
        context.invokePostUpdate(element);
        return query;
    }

    public Object getEntityIdCopy(UpdateContext context, EntityViewProxy updatableProxy) {
        if (jpaIdInstantiator != null) {
            Object oldId = jpaIdInstantiator.toEntity(context, null, null);
            if (idFlusher instanceof BasicAttributeFlusher<?, ?>) {
                ((BasicAttributeFlusher<Object, Object>) idFlusher).flushEntityComponents(context, oldId, updatableProxy.$$_getId());
            } else if (idFlusher instanceof EmbeddableAttributeFlusher<?, ?>) {
                ((EmbeddableAttributeFlusher<Object, Object>) idFlusher).getViewToEntityMapper().flushToEntity(context, oldId, updatableProxy.$$_getId());
            } else {
                idFlusher.flushEntity(context, oldId, updatableProxy, updatableProxy.$$_getId(), updatableProxy.$$_getId(), null);
            }
            return oldId;
        } else {
            return updatableProxy.$$_getId();
        }
    }

    public Object createViewIdByEntityId(Object id) {
        if (tupleizer == null) {
            return id;
        }
        Object[] tuple = tupleizer.tupleize(id);
        return idViewBuilder.build(tuple);
    }

    private Object determineOldId(UpdateContext context, EntityViewProxy updatableProxy, Runnable postReplaceListener) {
        if (updatableProxy.$$_getId() != null && postReplaceListener != null) {
            if (updatableProxy.$$_getId() instanceof EntityViewProxy) {
                // Copy the id view to preserve the original values
                return context.getEntityViewManager().convert(updatableProxy.$$_getId(), ((EntityViewProxy) updatableProxy.$$_getId()).$$_getEntityViewClass());
            } else if (jpaIdInstantiator != null) {
                Object oldId = jpaIdInstantiator.toEntity(context, null, null);
                ((BasicAttributeFlusher<Object, Object>) idFlusher).flushEntityComponents(context, oldId, updatableProxy.$$_getId());
                return oldId;
            } else {
                return updatableProxy.$$_getId();
            }
        } else {
            return updatableProxy.$$_getId();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean flushEntity(UpdateContext context, Object entity, Object ownerView, Object view, Object value, Runnable postReplaceListener) {
        if (element != null) {
            value = element;
        }
        if (!(value instanceof MutableStateTrackable)) {
            // Pass-through i.e. read-only id attributes
            for (int i = 0; i < flushers.length; i++) {
                DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    flusher.flushEntity(context, entity, ownerView, value, flusher.getViewAttributeAccessor().getValue(value), null);
                }
            }
            return false;
        }
        MutableStateTrackable updatableProxy = (MutableStateTrackable) value;
        // Already removed objects or objects without a parent can't be flushed
        // The root object, which is given when view == value, is the exception
        if (context.isRemovedObject(updatableProxy) || !updatableProxy.$$_hasParent() && view != value) {
            return false;
        }

        final boolean shouldPersist = persist == Boolean.TRUE || persist == null && updatableProxy.$$_isNew();
        final boolean doPersist = shouldPersist && persistable;
        final Object oldId = determineOldId(context, updatableProxy, postReplaceListener);
        int parentIndex = updatableProxy.$$_getParentIndex();
        DirtyTracker parent = updatableProxy.$$_getParent();
        RecordingCollection<?, Object> recordingCollection = null;
        RecordingMap<?, Object, Object> recordingMap = null;
        Object removedValue = null;
        Set<Object> removedKeys = null;
        List<Integer> afterPersistFlushers = null;
        List<Integer> deferredFlushers = null;
        boolean successful = false;
        try {
            Object id = updatableProxy.$$_getId();

            if (doPersist) {
                // In case of nested attributes, the entity instance we get is the container of the attribute
                if (!entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, null, null);
                }
                context.invokePrePersist(updatableProxy, entity);
                // If the parent is a hash based collection, or the view is re-mapped to a different type, remove before setting the id/re-mapping
                // There are two cases here, either we are in full flushing and we can get a RecordingIterator via getCurrentIterator
                // Or we are in the elementFlusher case where we don't iterate through the backing collection and thus can operate on the backing collection directly
                if (parent != null) {
                    if (parent instanceof RecordingCollection<?, ?> && ((recordingCollection = (RecordingCollection<?, Object>) parent).isHashBased() || persistViewMapper != null)) {
                        if (recordingCollection.getCurrentIterator() == null) {
                            recordingCollection.getDelegate().remove(updatableProxy);
                        } else {
                            recordingCollection.getCurrentIterator().replace();
                        }
                    } else if (parent instanceof RecordingMap<?, ?, ?> && (persistViewMapper != null || updatableProxy.$$_getParentIndex() == 1 && (recordingMap = (RecordingMap<?, Object, Object>) parent).isHashBased())) {
                        recordingMap = (RecordingMap<?, Object, Object>) parent;
                        // Parent index 1 in a recording map means it is part of the key
                        if (updatableProxy.$$_getParentIndex() == 1) {
                            if (recordingMap.getCurrentIterator() == null) {
                                removedValue = recordingMap.getDelegate().remove(updatableProxy);
                            } else {
                                removedValue = recordingMap.getCurrentIterator().replace();
                            }
                        } else {
                            if (removedKeys == null) {
                                removedKeys = new HashSet<>();
                            }
                            // TODO: replaceValue currently only handles the current value, which is inconsistent regarding what we do in the elementFlusher case
                            // Not sure if a creatable view should be allowed to occur multiple times in the map as value..
                            if (recordingMap.getCurrentIterator() == null) {
                                for (Map.Entry<Object, Object> entry : recordingMap.getDelegate().entrySet()) {
                                    if (entry.getValue().equals(updatableProxy)) {
                                        removedKeys.add(entry.getKey());
                                    }
                                }
                            } else {
                                recordingMap.getCurrentIterator().replaceValue(removedKeys);
                            }
                        }
                    }
                }

                // I know, that this is likely the ugliest hack ever, but to fix this properly would require a major redesign of the flusher handling which is too much work for this version
                // A version 2.0 or 3.0 might improve on this when redesigning for operation queueing
                if (postReplaceListener != null) {
                    postReplaceListener.run();
                }

                if (id != null) {
                    idFlusher.flushEntity(context, entity, ownerView, updatableProxy, id, null);
                }
                context.getInitialStateResetter().addUpdatedView(updatableProxy);
            } else {
                // In case of nested attributes, the entity instance we get is the container of the attribute
                if ((loadForEntityFlush || viewIdAccessor == null) && !entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, view, id);
                }
                // After Pre-Update the dirtyness could change
                preUpdate(context, updatableProxy);
            }
            Object[] state = updatableProxy.$$_getMutableState();
            boolean wasDirty = false;
            boolean optimisticLock = false;

            if (updatableProxy instanceof DirtyStateTrackable) {
                Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
                context.getInitialStateResetter().addState(initialState, initialState.clone());

                for (int i = 0; i < state.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null) {
                        if (doPersist && flusher.requiresFlushAfterPersist(state[i])) {
                            if (afterPersistFlushers == null) {
                                afterPersistFlushers = new ArrayList<>();
                            }
                            afterPersistFlushers.add(i);
                            wasDirty = true;
                            optimisticLock |= flusher.isOptimisticLockProtected();
                        } else if (flusher.requiresDeferredFlush(state[i])) {
                            if (deferredFlushers == null) {
                                deferredFlushers = new ArrayList<>();
                            }
                            deferredFlushers.add(i);
                            wasDirty = true;
                            optimisticLock |= flusher.isOptimisticLockProtected();
                        } else {
                            Object newInitialValue = flusher.cloneDeep(value, initialState[i], state[i]);
                            if (flusher.flushEntity(context, entity, ownerView, value, state[i], null)) {
                                wasDirty = true;
                                optimisticLock |= flusher.isOptimisticLockProtected();
                            }
                            initialState[i] = flusher.getNewInitialValue(context, newInitialValue, state[i]);
                        }
                    }
                }
            } else {
                for (int i = 0; i < state.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null) {
                        if (doPersist && flusher.requiresFlushAfterPersist(state[i])) {
                            if (afterPersistFlushers == null) {
                                afterPersistFlushers = new ArrayList<>();
                            }
                            afterPersistFlushers.add(i);
                            wasDirty = true;
                            optimisticLock |= flusher.isOptimisticLockProtected();
                        } else if (flusher.requiresDeferredFlush(state[i])) {
                            if (deferredFlushers == null) {
                                deferredFlushers = new ArrayList<>();
                            }
                            deferredFlushers.add(i);
                            wasDirty = true;
                            optimisticLock |= flusher.isOptimisticLockProtected();
                        } else {
                            if (flusher.flushEntity(context, entity, ownerView, value, state[i], null)) {
                                wasDirty = true;
                                optimisticLock |= flusher.isOptimisticLockProtected();
                            }
                        }
                    }
                }
            }

            // Pass through flushers
            for (int i = state.length; i < flushers.length; i++) {
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                if (flusher != null) {
                    if (flusher.flushEntity(context, entity, ownerView, value, flusher.getViewAttributeAccessor().getValue(value), null)) {
                        wasDirty = true;
                        optimisticLock |= flusher.isOptimisticLockProtected();
                    }
                }
            }
            if (versionFlusher != null && optimisticLockProtected && optimisticLock) {
                context.getInitialStateResetter().addVersionedView(updatableProxy, updatableProxy.$$_getVersion());
                // We might have to load the entity for optimistic locking
                if (!entityClass.isInstance(entity)) {
                    entity = entityLoader.toEntity(context, view, id);
                }
                versionFlusher.flushEntity(context, entity, ownerView, value, updatableProxy.$$_getVersion(), null);
            }
            if (deferredFlushers != null) {
                deferredFlushEntity(context, entity, ownerView, updatableProxy, deferredFlushers);
            }
            if (doPersist) {
                // If the class of the object is an entity, we persist the object
                context.getEntityManager().persist(entity);
                id = createViewIdByEntityId(entityLoader.getEntityId(context, entity));
                viewIdAccessor.setValue(updatableProxy, id);
            }
            successful = true;
            return wasDirty;
        } finally {
            int newObjectIndex = -1;
            if (shouldPersist) {
                if (idFlusher == null) {
                    // Embeddables don't have an id
                    newObjectIndex = context.getInitialStateResetter().addPersistedView(updatableProxy);
                } else {
                    newObjectIndex = context.getInitialStateResetter().addPersistedView(updatableProxy, oldId);
                }
                if (successful && afterPersistFlushers != null) {
                    deferredFlushEntity(context, entity, ownerView, updatableProxy, afterPersistFlushers);
                }
            } else if (successful && afterPersistFlushers != null) {
                deferredFlushEntity(context, entity, ownerView, updatableProxy, afterPersistFlushers);
            }

            Object newObject = null;
            if (doPersist) {
                newObject = updatableProxy;
                if (persistViewMapper != null) {
                    newObject = persistViewMapper.map(newObject, context.getEntityViewManager().getOptionalParameters());
                    context.getInitialStateResetter().addPersistedViewNewObject(newObjectIndex, newObject);
                }
                if (recordingCollection != null && (recordingCollection.isHashBased() || persistViewMapper != null)) {
                    // Reset the parent accordingly
                    resetParents(updatableProxy, parentIndex, parent, newObject);
                    if (recordingCollection.getCurrentIterator() == null) {
                        recordingCollection.getDelegate().add(newObject);
                    } else {
                        recordingCollection.getCurrentIterator().add(newObject);
                    }
                } else if (recordingMap != null && (persistViewMapper != null || updatableProxy.$$_getParentIndex() == 1 && recordingMap.isHashBased())) {
                    // Reset the parent accordingly
                    resetParents(updatableProxy, parentIndex, parent, newObject);
                    if (updatableProxy.$$_getParentIndex() == 1) {
                        if (recordingMap.getCurrentIterator() == null) {
                            recordingMap.getDelegate().put(newObject, removedValue);
                        } else {
                            recordingMap.getCurrentIterator().add(newObject, removedValue);
                        }
                    } else {
                        for (Object removedKey : removedKeys) {
                            if (recordingMap.getCurrentIterator() == null) {
                                recordingMap.getDelegate().put(removedKey, newObject);
                            } else {
                                recordingMap.getCurrentIterator().add(removedKey, newObject);
                            }
                        }
                    }
                } else if (parent != null && persistViewMapper != null) {
                    // In case of a singular attribute, we replace the mutable state object to signal the parent flusher
                    // SubviewAttributeFlusher is the parent, that uses this object for setting the actual and initial state
                    ((MutableStateTrackable) parent).$$_getMutableState()[parentIndex] = newObject;
                    updatableProxy.$$_unsetParent();
                }
                context.invokePostPersist(updatableProxy, entity);
            } else {
                context.invokePostUpdate(updatableProxy);
            }
        }
    }

    private boolean preUpdate(UpdateContext context, MutableStateTrackable element) {
        // After Pre-Update the dirtyness could change
        long[] dirtyMask = context.getInitialStateResetter().addUpdatedView(element);
        boolean ranPreUpdate = context.invokePreUpdate(element);
        if (ranPreUpdate && this.fullFlushers != this.flushers && element.$$_getSimpleDirty() != 0) {
            dirtyMask[0] |= element.$$_getSimpleDirty();
            element.$$_setDirty(dirtyMask);
            updateFlushers(context, element, this.flushers);
            element.$$_resetDirty();
            return true;
        }
        return false;
    }

    private void resetParents(MutableStateTrackable updatableProxy, int parentIndex, DirtyTracker parent, Object newObject) {
        List<Object> readOnlyParents = updatableProxy.$$_getReadOnlyParents();
        if (newObject != updatableProxy) {
            Object newReadOnlyParent = null;
            if (newObject instanceof BasicDirtyTracker) {
                ((BasicDirtyTracker) newObject).$$_setParent(parent, parentIndex);
                if (newObject instanceof MutableStateTrackable && readOnlyParents != null) {
                    newReadOnlyParent = newObject;
                }
            }
            if (readOnlyParents != null) {
                // The list changes while replacing attributes, so copy it
                readOnlyParents = new ArrayList<>(readOnlyParents);
                for (int i = 0; i < readOnlyParents.size(); i += 2) {
                    ((DirtyTracker) readOnlyParents.get(i)).$$_replaceAttribute(updatableProxy, (int) readOnlyParents.get(i + 1), newReadOnlyParent);
                }
            }
            updatableProxy.$$_unsetParent();
        }
    }

    private void deferredFlushEntity(UpdateContext context, Object entity, Object ownerView, MutableStateTrackable updatableProxy, List<Integer> deferredFlushers) {
        Object[] state = updatableProxy.$$_getMutableState();
        if (updatableProxy instanceof DirtyStateTrackable) {
            Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
            for (int i = 0; i < deferredFlushers.size(); i++) {
                final int index = deferredFlushers.get(i);
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                Object newInitialValue = flusher.cloneDeep(updatableProxy, initialState[index], state[index]);
                flusher.flushEntity(context, entity, ownerView, updatableProxy, state[index], null);
                initialState[index] = flusher.getNewInitialValue(context, newInitialValue, state[index]);
            }
        } else {
            for (int i = 0; i < deferredFlushers.size(); i++) {
                final int index = deferredFlushers.get(i);
                final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[index];
                flusher.flushEntity(context, entity, ownerView, updatableProxy, state[index], null);
            }
        }
    }

    @Override
    public List<PostFlushDeleter> remove(UpdateContext context, Object entity, Object view, Object value) {
        EntityViewProxy entityView = (EntityViewProxy) value;
        if (entityView instanceof MutableStateTrackable && !entityView.$$_isReference()) {
            MutableStateTrackable updatableProxy = (MutableStateTrackable) entityView;

            // Only remove objects that are
            // 1. Persistable i.e. of an entity type that can be removed
            // 2. Haven't been removed yet
            // 3. Aren't new i.e. only existing objects, no need to delete object that hasn't been persisted yet
            if (persistable && context.addRemovedObject(entityView) && !updatableProxy.$$_isNew()) {
                if (context.invokePreRemove(entityView)) {
                    Object[] state = updatableProxy.$$_getMutableState();
                    List<PostFlushDeleter> postFlushDeleters = new ArrayList<>();

                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                            postFlushDeleters.addAll(flusher.remove(context, entity, entityView, state[i]));
                        }
                    }

                    remove(context, entity, updatableProxy, updatableProxy, updatableProxy.$$_getId(), updatableProxy.$$_getVersion(), false);

                    for (PostFlushDeleter postFlushDeleter : postFlushDeleters) {
                        postFlushDeleter.execute(context);
                    }

                    for (int i = 0; i < state.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                            flusher.remove(context, entity, entityView, state[i]);
                        }
                    }
                } else {
                    context.removeRemovedObject(entityView);
                    context.getEntityViewManager().update(context, entityView);
                }
            }
        } else {
            if (context.addRemovedObject(entityView)) {
                if (context.invokePreRemove(entityView)) {
                    remove(context, entity, entityView, entityView, entityView.$$_getId(), entityView.$$_getVersion(), true);
                } else {
                    context.removeRemovedObject(entityView);
                    context.getEntityViewManager().update(context, entityView);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void remove(UpdateContext context, Object viewId) {
        EntityViewProxy reference = (EntityViewProxy) context.getEntityViewManager().getReference(viewType, viewId);
        if (context.invokePreRemove(reference)) {
            remove(context, null, null, reference, viewId, null, true);
        }
    }

    @Override
    public void removeFromEntity(UpdateContext context, Object entity) {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    private boolean remove(UpdateContext context, Object entity, Object ownerView, Object view, Object viewId, Object version, boolean cascadeMappedDeletes) {
        if (view != null) {
            context.getInitialStateResetter().addRemovedView((EntityViewProxy) view);
        }
        if (flushStrategy == FlushStrategy.ENTITY) {
            if (entity == null) {
                entity = referenceEntityLoader.toEntity(context, view, viewId);
            }

            // Ensure the entity version is the expected one
            if (version != null && versionFlusher != null) {
                versionFlusher.remove(context, entity, null, version);
            }

            if (cascadeMappedDeletes) {
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                        flusher.removeFromEntity(context, entity);
                    }
                }
            }

            context.getEntityManager().remove(entity);
            context.invokePostRemove((EntityViewProxy) view);

            if (cascadeMappedDeletes) {
                // nested cascades
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                        flusher.removeFromEntity(context, entity);
                    }
                }
            }
        } else {
            // Query flush strategy

            // TODO: in the future, we could try to aggregate deletes into modification CTEs if we know there are no cycles
            Object entityId;
            if (idFlusher instanceof EmbeddableAttributeFlusher<?, ?>) {
                entityId = ((EmbeddableAttributeFlusher<Object, Object>) idFlusher).getViewToEntityMapper().applyToEntity(context, null, viewId);
            } else {
                entityId = viewId;
            }

            // We only need to cascade delete unmapped attributes for query flushing since entity flushing takes care of that for us
            for (int i = 0; i < unmappedPreRemoveCascadeDeleters.length; i++) {
                unmappedPreRemoveCascadeDeleters[i].removeByOwnerId(context, entityId);
            }

            Object[] returnedValues = null;
            List<PostFlushDeleter> postFlushDeleters = new ArrayList<>();
            if (cascadeMappedDeletes) {
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && !flusher.requiresDeleteCascadeAfterRemove()) {
                        postFlushDeleters.addAll(flusher.removeByOwnerId(context, entityId));
                    }
                }
            }

            boolean doDelete = true;
            // need to "return" the values from the delete query for the post deleters since the values aren't available after executing the delete query
            if (cascadeMappedDeletes || unmappedPostRemoveCascadeDeleters.length != 0) {
                List<String> returningAttributes = new ArrayList<>();
                for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                    returningAttributes.add(unmappedPostRemoveCascadeDeleters[i].getAttributeValuePath());
                }
                if (cascadeMappedDeletes) {
                    for (int i = 0; i < flushers.length; i++) {
                        final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                        if (flusher != null && flusher.requiresDeleteCascadeAfterRemove()) {
                            String elementIdAttributeName = flushers[i].getElementIdAttributeName();
                            if (elementIdAttributeName != null) {
                                returningAttributes.add(elementIdAttributeName);
                            }
                        }
                    }
                }

                if (!returningAttributes.isEmpty()) {
                    // If the dbms supports it, we use the returning feature to do this
                    if (context.getEntityViewManager().getDbmsDialect().supportsReturningColumns()) {
                        DeleteCriteriaBuilder<?> cb = context.getEntityViewManager().getCriteriaBuilderFactory().delete(context.getEntityManager(), entityClass);
                        cb.where(idFlusher.getMapping()).eq(entityId);
                        if (version != null && optimisticLockProtected && versionFlusher != null) {
                            cb.where(versionFlusher.getMapping()).eq(version);
                        }

                        ReturningResult<Tuple> result = cb.executeWithReturning(returningAttributes.toArray(new String[returningAttributes.size()]));
                        if (version != null && versionFlusher != null) {
                            if (result.getUpdateCount() != 1) {
                                throw new OptimisticLockException("The remove operation did not return the expected update count!", entity, view);
                            }
                        }
                        returnedValues = result.getLastResult().toArray();
                        doDelete = false;
                    } else {
                        // Otherwise we query the attributes
                        CriteriaBuilder<Object[]> cb = context.getEntityViewManager().getCriteriaBuilderFactory().create(context.getEntityManager(), Object[].class);
                        cb.from(entityClass);
                        cb.where(idFlusher.getMapping()).eq(entityId);
                        for (String attribute : returningAttributes) {
                            cb.select(attribute);
                        }
                        Object result = cb.getSingleResult();
                        // Hibernate might return the object itself although we specified that we want an Object[] return...
                        if (result instanceof Object[]) {
                            returnedValues = (Object[]) result;
                        } else {
                            returnedValues = new Object[]{result};
                        }
                    }
                }
            }

            if (doDelete) {
                if (version != null && optimisticLockProtected && versionFlusher != null) {
                    Query query = context.getEntityManager().createQuery(versionedDeleteQuery);
                    idFlusher.flushQuery(context, EntityViewUpdaterImpl.WHERE_CLAUSE_PREFIX, null, query, ownerView, view, viewId, null, null);
                    versionFlusher.flushQueryInitialVersion(context, EntityViewUpdaterImpl.WHERE_CLAUSE_PREFIX, query, view, version);
                    int updated = query.executeUpdate();
                    if (updated != 1) {
                        throw new OptimisticLockException("The remove operation did not return the expected update count!", entity, view);
                    }
                } else {
                    Query query = context.getEntityManager().createQuery(deleteQuery);
                    idFlusher.flushQuery(context, EntityViewUpdaterImpl.WHERE_CLAUSE_PREFIX, null, query, ownerView, view, viewId, null, null);
                    query.executeUpdate();
                }
            }

            context.invokePostRemove((EntityViewProxy) view);

            for (PostFlushDeleter postFlushDeleter : postFlushDeleters) {
                postFlushDeleter.execute(context);
            }

            for (int i = 0; i < unmappedPostRemoveCascadeDeleters.length; i++) {
                unmappedPostRemoveCascadeDeleters[i].removeById(context, returnedValues[i]);
            }

            if (cascadeMappedDeletes) {
                int valueIndex = unmappedPostRemoveCascadeDeleters.length;
                for (int i = 0; i < flushers.length; i++) {
                    final DirtyAttributeFlusher<?, Object, Object> flusher = flushers[i];
                    if (flusher != null && flusher.requiresDeleteCascadeAfterRemove() && flusher.getElementIdAttributeName() != null) {
                        flusher.remove(context, returnedValues[valueIndex++]);
                    }
                }
            }
        }

        return true;
    }

    public DirtyAttributeFlusher<?, Object, Object> getIdFlusher() {
        return idFlusher;
    }

    @Override
    public List<PostFlushDeleter> removeByOwnerId(UpdateContext context, Object id) {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isViewOnlyDeleteCascaded() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPassThrough() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public String getElementIdAttributeName() {
        // A composite flusher needs to be wrapped in a subview or collection flusher
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeAccessor getViewAttributeAccessor() {
        return null;
    }

    @Override
    public AttributeAccessor getEntityAttributeAccessor() {
        return null;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public boolean isOptimisticLockProtected() {
        return optimisticLockProtected;
    }

    @Override
    public boolean requiresFlushAfterPersist(Object value) {
        return false;
    }

    @Override
    public boolean requiresDeferredFlush(Object value) {
        return false;
    }

    @Override
    public DirtyAttributeFlusher<CompositeAttributeFlusher, Object, Object> getDirtyFlusher(UpdateContext context, Object view, Object initial, Object current) {
        return this;
    }

    @Override
    public <X> DirtyChecker<X>[] getNestedCheckers(Object current) {
        return (DirtyChecker<X>[]) flushers;
    }

    @Override
    public DirtyKind getDirtyKind(Object initial, Object current) {
        if (current == null) {
            if (initial == null) {
                return DirtyKind.NONE;
            }
            return DirtyKind.UPDATED;
        }
        if (initial == null) {
            return DirtyKind.UPDATED;
        }

        DirtyStateTrackable currentObject = (DirtyStateTrackable) current;
        DirtyStateTrackable initialObject = (DirtyStateTrackable) initial;
        // Skip further checks if we detect identity change
        if (initialObject != currentObject && !initialObject.equals(currentObject)) {
            return DirtyKind.UPDATED;
        }
        if (!currentObject.$$_isDirty()) {
            return DirtyKind.NONE;
        }
        long dirty = currentObject.$$_getSimpleDirty();
        Object[] initialState = initialObject.$$_getInitialState();
        Object[] dirtyState = currentObject.$$_getMutableState();

        for (int i = 0; i < initialState.length; i++) {
            long mask = 1L << i;
            if ((dirty & mask) != 0) {
                if (flushers[i].getDirtyKind(initialState[i], dirtyState[i]) != DirtyKind.NONE) {
                    return DirtyKind.MUTATED;
                }
            }
        }
        return DirtyKind.NONE;
    }

    @SuppressWarnings("unchecked")
    public <T extends DirtyAttributeFlusher<T, E, V>, E, V> DirtyAttributeFlusher<T, E, V> getNestedDirtyFlusher(UpdateContext context, MutableStateTrackable updatableProxy) {
        // When we persist, always flush all attributes
        boolean shouldPersist = updatableProxy.$$_isNew();
        if (context.isForceFull() || flushMode == FlushMode.FULL || shouldPersist || !(updatableProxy instanceof DirtyStateTrackable)) {
            return (DirtyAttributeFlusher<T, E, V>) this;
        }

        Object[] originalDirtyState = updatableProxy.$$_getMutableState();
        DirtyAttributeFlusher[] flushers = updateFlushers(context, updatableProxy, new DirtyAttributeFlusher[originalDirtyState.length]);
        if (flushers == null) {
            return null;
        }
        return (DirtyAttributeFlusher<T, E, V>) new CompositeAttributeFlusher(context.getEntityViewManager(), this, flushers, updatableProxy, false);
    }

    private DirtyAttributeFlusher[] updateFlushers(UpdateContext context, MutableStateTrackable updatableProxy, DirtyAttributeFlusher[] flushers) {
        Object[] initialState = ((DirtyStateTrackable) updatableProxy).$$_getInitialState();
        Object[] originalDirtyState = updatableProxy.$$_getMutableState();
        // Copy flushers to the target candidate flushers
        if (!updatableProxy.$$_copyDirty(this.fullFlushers, flushers)) {
            // If the dirty detection says nothing is dirty, we don't need to do anything
            return null;
        }

        boolean first = true;
        for (int i = 0; i < originalDirtyState.length; i++) {
            if (flushers[i] != null) {
                Object newState = originalDirtyState[i];
                DirtyAttributeFlusher flusher = flushers[i].getDirtyFlusher(context, updatableProxy, initialState[i], newState);

                if (flusher == null) {
                    flushers[i] = null;
                } else {
                    flushers[i] = flusher;

                    if (first) {
                        first = false;
                    }
                }
            }
        }

        // If nothing is dirty, we don't have to do anything
        if (first) {
            return null;
        }

        // If we know something is dirty, we copy over the pass through flushers too
        if (hasPassThroughFlushers) {
            for (int i = originalDirtyState.length; i < flushers.length; i++) {
                DirtyAttributeFlusher flusher = this.flushers[i];

                if (flusher != null && flusher.isPassThrough()) {
                    flushers[i] = flusher;
                }
            }
        }
        return flushers;
    }

}
