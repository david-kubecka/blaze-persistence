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

package com.blazebit.persistence.impl.dialect;

import java.util.Map;

import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.UpdateJoinStyle;
import com.blazebit.persistence.spi.ValuesStrategy;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class HSQLDbmsDialect extends DefaultDbmsDialect {

    public HSQLDbmsDialect() {
    }

    public HSQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public boolean supportsReturningColumns() {
        return true;
    }

    @Override
    public boolean supportsWindowFunctions() {
        return false;
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        // NOTE: this is only supported in HSQL 2.0+
        return ValuesStrategy.VALUES;
    }

    @Override
    public boolean needsUniqueSelectItemNamesAlsoWhenTableColumnAliasing() {
        return false;
    }

    @Override
    public boolean isNullSmallest() {
        // Actually, HSQLDB always shows NULL first, regardless of the ordering, but we don't care because it supports null precedence handling
        return true;
    }

    @Override
    public LateralStyle getLateralStyle() {
        return LateralStyle.NONE;
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.MERGE;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.MERGE;
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        if (isSubquery && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }
        
        return super.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, dmlAffectedTable, returningColumns, includedModificationStates);
    }
    
}
