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

package com.blazebit.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Query;

import com.blazebit.persistence.BaseInsertCriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.ModificationQuerySpecification;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseInsertCriteriaBuilderImpl<T, X extends BaseInsertCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseInsertCriteriaBuilder<T, X>, SelectBuilder<X> {

    protected final Map<String, Integer> bindingMap = new TreeMap<>();

    public BaseInsertCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.INSERT, clazz, null, cteKey, cteClass, result, listener);
        
        if (!mainQuery.jpaProvider.supportsInsertStatement()) {
            throw new IllegalStateException("JPA provider does not support insert statements!");
        }
    }

    public BaseInsertCriteriaBuilderImpl(BaseInsertCriteriaBuilderImpl<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
        bindingMap.putAll(builder.bindingMap);
    }

    @Override
    protected void appendSelectClause(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        selectManager.buildSelect(sbSelectFrom, true, externalRepresentation);
    }

    @Override
    @SuppressWarnings("unchecked")
    public X bind(String attributeName, Object value) {
        addBind(attributeName);
        selectManager.select(parameterManager.addParameterExpression(value, ClauseType.SELECT, this), null);
        
        return (X) this;
    }

    @Override
    public SelectBuilder<X> bind(String attributeName) {
        addBind(attributeName);
        return this;
    }

    protected void addBind(String attributeName) {
        // NOTE: We are not resolving embedded properties, because hibernate does not support them
        // Just do that to assert the attribute exists
        if (entityType.getAttribute(attributeName) == null) {
            // Well, some implementations might not be fully spec compliant..
            throw new IllegalArgumentException("Attribute '" + attributeName + "' does not exist on '" + entityType.getName() + "'!");
        }
        Integer attributeBindIndex = bindingMap.get(attributeName);

        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }

        bindingMap.put(attributeName, selectManager.getSelectInfos().size());
    }
    
    @Override
    protected void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }
        List<String> attributes = new ArrayList<String>(bindingMap.size());
        List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
        List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
        newSelectInfos.clear();
        
        for (Map.Entry<String, Integer> attributeEntry : bindingMap.entrySet()) {
            // Reorder select infos to fit the attribute order
            Integer newPosition = attributes.size();
            attributes.add(attributeEntry.getKey());
            
            SelectInfo selectInfo = originalSelectInfos.get(attributeEntry.getValue());
            newSelectInfos.add(selectInfo);
            attributeEntry.setValue(newPosition);
        }
        expandBindings();
        super.prepareAndCheck();
    }

    @Override
    protected void prepareSelect() {
        // We have an insert statement here which supports parameters in queries
    }

    protected void expandBindings() {
    }

    @Override
    protected boolean isJoinRequiredForSelect() {
        // NOTE: since we aren't actually selecting properties but passing them through to the insert, we don't require joins
        return false;
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode, boolean countWrapped) {
        appendInsertIntoFragment(sbSelectFrom, externalRepresentation);
        sbSelectFrom.append('(');
        
        boolean first = true;
        for (Map.Entry<String, Integer> attributeEntry : bindingMap.entrySet()) {
            if (first) {
                first = false;
            } else {
                sbSelectFrom.append(", ");
            }
            
            sbSelectFrom.append(attributeEntry.getKey());
        }
        
        sbSelectFrom.append(")\n");
        buildSelectBaseQueryString(sbSelectFrom, externalRepresentation);
    }

    protected void buildSelectBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        super.buildBaseQueryString(sbSelectFrom, externalRepresentation, null, false);
    }

    protected void appendInsertIntoFragment(StringBuilder sbSelectFrom, boolean externalRepresentation) {
        sbSelectFrom.append("INSERT INTO ");
        sbSelectFrom.append(entityType.getName());
    }

    @Override
    public Query getQuery() {
        if (mainQuery.jpaProvider.supportsInsertStatement()) {
            return super.getQuery();
        } else {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    @Override
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        // We need to change the underlying sql when doing a limit with hibernate since it does not support limiting insert ... select statements
        Query baseQuery = em.createQuery(getBaseQueryStringWithCheck(null, null));
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        Set<JoinNode> keyRestrictedLeftJoins = getKeyRestrictedLeftJoins();

        List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(baseQuery, keyRestrictedLeftJoins, Collections.EMPTY_SET);
        List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(baseQuery, 0);

        boolean isEmbedded = this instanceof ReturningBuilder;
        String[] returningColumns = getReturningColumns();
        boolean shouldRenderCteNodes = renderCteNodes(isEmbedded);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(isEmbedded) : Collections.EMPTY_LIST;

        QuerySpecification querySpecification = new ModificationQuerySpecification(
                this,
                baseQuery,
                getCountExampleQuery(),
                parameterManager.getParameterImpls(),
                parameterListNames,
                keyRestrictedLeftJoinAliases,
                entityFunctionNodes,
                mainQuery.cteManager.isRecursive(),
                ctes,
                shouldRenderCteNodes,
                isEmbedded,
                returningColumns,
                null,
                includedModificationStates,
                returningAttributeBindingMap,
                mainQuery.getQueryConfiguration().isQueryPlanCacheEnabled()
        );

        CustomSQLQuery query = new CustomSQLQuery(
                querySpecification,
                baseQuery,
                parameterManager.getCriteriaNameMapping(),
                parameterManager.getTransformers(),
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        parameterManager.parameterizeQuery(query);

        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);

        return query;
    }

}
