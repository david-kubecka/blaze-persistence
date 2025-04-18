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

package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;

import java.util.Properties;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        properties.put("openjpa.RuntimeUnenhancedClasses", "supported");
        properties.put("openjpa.jdbc.SchemaFactory", "native(foreignKeys=true)");
        properties.put("openjpa.Sequence", "native");
        properties.put("openjpa.Log", "DefaultLevel=WARN, Tool=INFO, SQL=TRACE");
        properties.put("openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=restrict,JoinForeignKeyDeleteAction=restrict");
        String dbAction = (String) properties.remove("javax.persistence.schema-generation.database.action");
        if ("drop-and-create".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='dropDB,add')");
        } else if ("create".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='add')");
        } else if ("drop".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='dropDB')");
        } else if (!"none".equals(dbAction)) {
            throw new IllegalArgumentException("Unsupported database action: " + dbAction);
        }
        return properties;
    }

    @Override
    protected boolean needsEntityManagerForDbAction() {
        return true;
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        return true;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        return true;
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        return JpaProviderFamily.OPENJPA;
    }

    @Override
    protected int getJpaProviderMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int getJpaProviderMinorVersion() {
        throw new UnsupportedOperationException();
    }
}
