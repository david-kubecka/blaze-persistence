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

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class CachingJpaProvider implements JpaProvider {

    private final JpaProvider jpaProvider;
    private final EntityMetamodelImpl entityMetamodel;

    public CachingJpaProvider(EntityMetamodelImpl entityMetamodel) {
        this.jpaProvider = entityMetamodel.getJpaProvider();
        this.entityMetamodel = entityMetamodel;
    }

    public JpaProvider getJpaProvider() {
        return jpaProvider;
    }

    @Override
    public String[] getDiscriminatorColumnCheck(EntityType<?> entityType) {
        return jpaProvider.getDiscriminatorColumnCheck(entityType);
    }

    @Override
    public boolean isForeignJoinColumn(EntityType<?> ownerType, String attributeName) {
        ExtendedAttribute attribute = (ExtendedAttribute) entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttributes().get(attributeName);
        return attribute != null && attribute.isForeignJoinColumn();
    }

    @Override
    public boolean isColumnShared(EntityType<?> ownerType, String attributeName) {
        ExtendedAttribute attribute = (ExtendedAttribute) entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttributes().get(attributeName);
        return attribute != null && attribute.isColumnShared();
    }

    @Override
    public ConstraintType requiresTreatFilter(EntityType<?> ownerType, String attributeName, JoinType joinType) {
        ExtendedAttribute attribute = (ExtendedAttribute) entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttributes().get(attributeName);
        return attribute == null ? ConstraintType.NONE : attribute.getJoinTypeIndexedRequiresTreatFilter(joinType);
    }

    @Override
    public String getMappedBy(EntityType<?> ownerType, String attributeName) {
        return entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttribute(attributeName).getMappedBy();
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String attributeName) {
        return entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttribute(attributeName).getColumnNames();
    }

    @Override
    public String[] getColumnNames(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        return getColumnNames(ownerType, attributeName);
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String attributeName) {
        return entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttribute(attributeName).getColumnTypes();
    }

    @Override
    public String[] getColumnTypes(EntityType<?> ownerType, String elementCollectionPath, String attributeName) {
        return getColumnTypes(ownerType, attributeName);
    }

    @Override
    public Map<String, String> getWritableMappedByMappings(EntityType<?> inverseType, EntityType<?> ownerType, String attributeName, String inverseAttribute) {
        if (attributeName.isEmpty()) {
            return entityMetamodel.getManagedType(ExtendedManagedType.class, inverseType).getAttribute(inverseAttribute).getWritableMappedByMappings(ownerType);
        } else {
            return entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType).getAttribute(attributeName).getWritableMappedByMappings(inverseType);
        }
    }

    @Override
    public JoinTable getJoinTable(EntityType<?> ownerType, String attributeName) {
        return entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttribute(attributeName).getJoinTable();
    }

    @Override
    public boolean isBag(EntityType<?> ownerType, String attributeName) {
        ExtendedAttribute attribute = (ExtendedAttribute) entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType.getName()).getAttributes().get(attributeName);
        return attribute != null && attribute.isBag();
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        ExtendedManagedType managedType  = entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType);
        ExtendedAttribute attribute = (ExtendedAttribute) managedType.getAttributes().get(attributeName);
        return attribute != null && attribute.isOrphanRemoval();
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        return isOrphanRemoval(ownerType, attributeName);
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        ExtendedManagedType managedType = entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType);
        ExtendedAttribute attribute = (ExtendedAttribute) managedType.getAttributes().get(attributeName);
        return attribute != null && attribute.isDeleteCascaded();
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        return isDeleteCascaded(ownerType, attributeName);
    }

    @Override
    public boolean hasJoinCondition(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        ExtendedManagedType managedType = entityMetamodel.getManagedType(ExtendedManagedType.class, ownerType);
        ExtendedAttribute attribute = (ExtendedAttribute) managedType.getAttributes().get(attributeName);
        return attribute != null && attribute.hasJoinCondition();
    }
    // Simple delegates

    @Override
    public boolean supportsJpa21() {
        return jpaProvider.supportsJpa21();
    }

    @Override
    public boolean supportsEntityJoin() {
        return jpaProvider.supportsEntityJoin();
    }

    @Override
    public boolean supportsCrossJoin() {
        return jpaProvider.supportsCrossJoin();
    }

    @Override
    public boolean supportsInsertStatement() {
        return jpaProvider.supportsInsertStatement();
    }

    @Override
    public boolean needsBracketsForListParameter() {
        return jpaProvider.needsBracketsForListParameter();
    }

    @Override
    public boolean needsJoinSubqueryRewrite() {
        return jpaProvider.needsJoinSubqueryRewrite();
    }

    @Override
    public String getBooleanExpression(boolean value) {
        return jpaProvider.getBooleanExpression(value);
    }

    @Override
    public String getBooleanConditionalExpression(boolean value) {
        return jpaProvider.getBooleanConditionalExpression(value);
    }

    @Override
    public String getNullExpression() {
        return jpaProvider.getNullExpression();
    }

    @Override
    public String getOnClause() {
        return jpaProvider.getOnClause();
    }

    @Override
    public String getCollectionValueFunction() {
        return jpaProvider.getCollectionValueFunction();
    }

    @Override
    public boolean supportsCollectionValueDereference() {
        return jpaProvider.supportsCollectionValueDereference();
    }

    @Override
    public boolean supportsSubqueryLimitOffset() {
        return jpaProvider.supportsSubqueryLimitOffset();
    }

    @Override
    public boolean supportsSetOperations() {
        return jpaProvider.supportsSetOperations();
    }

    @Override
    public boolean supportsListagg() {
        return jpaProvider.supportsListagg();
    }

    @Override
    public Class<?> getDefaultQueryResultType() {
        return jpaProvider.getDefaultQueryResultType();
    }

    @Override
    public String getCustomFunctionInvocation(String functionName, int argumentCount) {
        return jpaProvider.getCustomFunctionInvocation(functionName, argumentCount);
    }

    @Override
    public String escapeCharacter(char character) {
        return jpaProvider.escapeCharacter(character);
    }

    @Override
    public boolean supportsNullPrecedenceExpression() {
        return jpaProvider.supportsNullPrecedenceExpression();
    }

    @Override
    public void renderNullPrecedence(StringBuilder sb, String expression, String resolvedExpression, String order, String nulls) {
        jpaProvider.renderNullPrecedence(sb, expression, resolvedExpression, order, nulls);
    }

    @Override
    public boolean supportsRootTreat() {
        return jpaProvider.supportsRootTreat();
    }

    @Override
    public boolean supportsTreatJoin() {
        return jpaProvider.supportsTreatJoin();
    }

    @Override
    public boolean supportsTreatCorrelation() {
        return jpaProvider.supportsTreatCorrelation();
    }

    @Override
    public boolean supportsRootTreatJoin() {
        return jpaProvider.supportsRootTreatJoin();
    }

    @Override
    public boolean supportsRootTreatTreatJoin() {
        return jpaProvider.supportsRootTreatTreatJoin();
    }

    @Override
    public boolean supportsSubtypePropertyResolving() {
        return jpaProvider.supportsSubtypePropertyResolving();
    }

    @Override
    public boolean supportsSubtypeRelationResolving() {
        return jpaProvider.supportsSubtypeRelationResolving();
    }

    @Override
    public boolean supportsCountStar() {
        return jpaProvider.supportsCountStar();
    }

    @Override
    public boolean supportsCustomFunctions() {
        return jpaProvider.supportsCustomFunctions();
    }

    @Override
    public boolean supportsNonScalarSubquery() {
        return jpaProvider.supportsNonScalarSubquery();
    }

    @Override
    public boolean supportsSubqueryInFunction() {
        return jpaProvider.supportsSubqueryInFunction();
    }

    @Override
    public boolean supportsSubqueryAliasShadowing() {
        return jpaProvider.supportsSubqueryAliasShadowing();
    }

    @Override
    public boolean containsEntity(EntityManager em, Class<?> entityClass, Object id) {
        return jpaProvider.containsEntity(em, entityClass, id);
    }

    @Override
    public boolean supportsSingleValuedAssociationIdExpressions() {
        return jpaProvider.supportsSingleValuedAssociationIdExpressions();
    }

    @Override
    public boolean supportsForeignAssociationInOnClause() {
        return jpaProvider.supportsForeignAssociationInOnClause();
    }

    @Override
    public boolean supportsUpdateSetEmbeddable() {
        return jpaProvider.supportsUpdateSetEmbeddable();
    }

    @Override
    public boolean supportsUpdateSetAssociationId() {
        return jpaProvider.supportsUpdateSetAssociationId();
    }

    @Override
    public boolean supportsTransientEntityAsParameter() {
        return jpaProvider.supportsTransientEntityAsParameter();
    }

    @Override
    public boolean needsAssociationToIdRewriteInOnClause() {
        return jpaProvider.needsAssociationToIdRewriteInOnClause();
    }

    @Override
    public boolean needsBrokenAssociationToIdRewriteInOnClause() {
        return jpaProvider.needsBrokenAssociationToIdRewriteInOnClause();
    }

    @Override
    public boolean needsTypeConstraintForColumnSharing() {
        return jpaProvider.needsTypeConstraintForColumnSharing();
    }

    @Override
    public boolean supportsCollectionTableCleanupOnDelete() {
        return jpaProvider.supportsCollectionTableCleanupOnDelete();
    }

    @Override
    public boolean supportsJoinTableCleanupOnDelete() {
        return jpaProvider.supportsJoinTableCleanupOnDelete();
    }

    @Override
    public boolean needsCorrelationPredicateWhenCorrelatingWithWhereClause() {
        return jpaProvider.needsCorrelationPredicateWhenCorrelatingWithWhereClause();
    }

    @Override
    public boolean supportsSingleValuedAssociationNaturalIdExpressions() {
        return jpaProvider.supportsSingleValuedAssociationNaturalIdExpressions();
    }

    @Override
    public boolean supportsGroupByEntityAlias() {
        return jpaProvider.supportsGroupByEntityAlias();
    }

    @Override
    public boolean needsElementCollectionIdCutoff() {
        return jpaProvider.needsElementCollectionIdCutoff();
    }

    @Override
    public boolean needsUnproxyForFieldAccess() {
        return jpaProvider.needsUnproxyForFieldAccess();
    }

    @Override
    public boolean needsCaseWhenElseBranch() {
        return jpaProvider.needsCaseWhenElseBranch();
    }

    @Override
    public boolean supportsLikePatternEscape() {
        return jpaProvider.supportsLikePatternEscape();
    }

    @Override
    public void setCacheable(Query query) {
        jpaProvider.setCacheable(query);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String attributeName) {
        // TODO: cache this via extended metamodel
        return jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(owner, attributeName);
    }

    @Override
    public List<String> getIdentifierOrUniqueKeyEmbeddedPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        // TODO: cache this via extended metamodel
        return jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(owner, elementCollectionPath, attributeName);
    }

    @Override
    public Map<String, String> getJoinMappingPropertyNames(EntityType<?> owner, String elementCollectionPath, String attributeName) {
        // TODO: cache this via extended metamodel
        return jpaProvider.getJoinMappingPropertyNames(owner, elementCollectionPath, attributeName);
    }

    @Override
    public boolean supportsEnumLiteral(ManagedType<?> ownerType, String attributeName, boolean key) {
        return jpaProvider.supportsEnumLiteral(ownerType, attributeName, key);
    }

    @Override
    public boolean supportsTemporalLiteral() {
        return jpaProvider.supportsTemporalLiteral();
    }

    @Override
    public boolean supportsNonDrivingAliasInOnClause() {
        return jpaProvider.supportsNonDrivingAliasInOnClause();
    }

    @Override
    public boolean supportsSelectCompositeIdEntityInSubquery() {
        return jpaProvider.supportsSelectCompositeIdEntityInSubquery();
    }

    @Override
    public boolean supportsProxyParameterForNonPkAssociation() {
        return jpaProvider.supportsProxyParameterForNonPkAssociation();
    }

    @Override
    public Object getIdentifier(Object entity) {
        return jpaProvider.getIdentifier(entity);
    }

    @Override
    public <T> T unproxy(T entity) {
        return jpaProvider.unproxy(entity);
    }

    @Override
    public JpaMetamodelAccessor getJpaMetamodelAccessor() {
        return jpaProvider.getJpaMetamodelAccessor();
    }
}
