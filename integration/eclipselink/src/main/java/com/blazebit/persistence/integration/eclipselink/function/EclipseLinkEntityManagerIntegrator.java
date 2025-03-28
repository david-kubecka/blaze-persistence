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

package com.blazebit.persistence.integration.eclipselink.function;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.eclipselink.EclipseLinkJpaProvider;
import com.blazebit.persistence.integration.jpa.function.CountStarFunction;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.platform.database.DatabasePlatform;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class EclipseLinkEntityManagerIntegrator implements EntityManagerFactoryIntegrator {
    
    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryIntegrator.class.getName());
    
    /**
     * EclipseLink uses integer values for something they call selectors.
     * Apparently every operator needs a unique selector value. We choose 100.000 as
     * the base value from which we will increment further for all functions.
     */
    private int functionSelectorCounter = 100000;

    @Override
    public String getDbms(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory == null) {
            return null;
        }
        AbstractSession session = entityManagerFactory.unwrap(JpaEntityManagerFactory.class).getDatabaseSession();
        DatabasePlatform platform = session.getPlatform();

        if (platform.isAccess()) {
            return "access";
        } else if (platform.isAttunity()) {
            return null;
        } else if (platform.isCloudscape()) {
            return null;
        } else if (platform.isDerby()) {
            return "derby";
        } else if (platform.isDB2()) {
            return "db2";
        } else if (platform.isHANA()) {
            return "hana";
        } else if (platform.isH2()) {
            return "h2";
        } else if (platform.isDBase()) {
            return "dbase";
        } else if (platform.isHSQL()) {
            return "hsql";
        } else if (platform.isInformix()) {
            return "informix";
        } else if (platform.isMySQL()) {
            return "mysql";
        } else if (platform.isODBC()) {
            return null;
        } else if (platform.isOracle()) {
            return "oracle";
        } else if (platform.isOracle9()) {
            return "oracle";
        } else if (platform.isPervasive()) {
            return null;
        } else if (platform.isPostgreSQL()) {
            return "postgresql";
        } else if (platform.isPointBase()) {
            return null;
        } else if (platform.isSQLAnywhere()) {
            return null;
        } else if (platform.isFirebird()) {
            return "firebird";
        } else if (platform.isSQLServer()) {
            return "microsoft";
        } else if (platform.isSybase()) {
            return "sybase";
        } else if (platform.isSymfoware()) {
            return null;
        } else if (platform.isTimesTen()) {
            return null;
        } else if (platform.isTimesTen7()) {
            return null;
        } else if (platform.isMaxDB()) {
            return null;
        }
        return null;
    }

    @Override
    public JpaProviderFactory getJpaProviderFactory(final EntityManagerFactory entityManagerFactory) {
        boolean eclipseLink24;
        String version;
        try {
            Class<?> versionClass = Class.forName("org.eclipse.persistence.Version");
            version = (String) versionClass.getMethod("getVersion").invoke(null);
            String[] versionParts = version.split("\\.");
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);

            eclipseLink24 = major > 2 || (major == 2 && minor >= 4);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported EclipseLink version", ex);
        }

        if (!eclipseLink24) {
            throw new IllegalArgumentException("Unsupported EclipseLink version " + version + "!");
        }
        return new JpaProviderFactory() {
            @Override
            public JpaProvider createJpaProvider(EntityManager em) {
                PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory == null ? null : entityManagerFactory.getPersistenceUnitUtil();
                if (persistenceUnitUtil == null && em != null) {
                    persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
                }
                return new EclipseLinkJpaProvider(persistenceUnitUtil, getDbms(entityManagerFactory));
            }
        };
    }

    @Override
    public Map<String, JpqlFunction> getRegisteredFunctions(EntityManagerFactory entityManagerFactory) {
        AbstractSession session = entityManagerFactory.unwrap(JpaEntityManagerFactory.class).getDatabaseSession();
        DatabasePlatform platform = session.getPlatform();
        @SuppressWarnings("unchecked")
        Map<Integer, ExpressionOperator> platformOperators = platform.getPlatformOperators();
        Map<String, JpqlFunction> functions = new HashMap<>(platformOperators.size());
        
        for (ExpressionOperator op : platformOperators.values()) {
            String name = (String) ExpressionOperator.getPlatformOperatorNames().get(op.getSelector());
            
            if (name != null) {
                if (op instanceof JpqlFunctionExpressionOperator) {
                    functions.put(name.toLowerCase(), ((JpqlFunctionExpressionOperator) op).unwrap());
                } else {
                    int selector = op.getSelector();

                    // No support for these expressions
                    if (selector != ExpressionOperator.Union
                            && selector != ExpressionOperator.UnionAll
                            && selector != ExpressionOperator.Intersect
                            && selector != ExpressionOperator.IntersectAll
                            && selector != ExpressionOperator.Except
                            && selector != ExpressionOperator.ExceptAll) {
                        functions.put(name.toLowerCase(), new ExpressionOperatorJpqlFunction(op));
                    }
                }
            }
        }

        // Eclipselink doesn't report all functions..
        functions.put("count", new ExpressionOperatorJpqlFunction(ExpressionOperator.count()));
        functions.put("sum", new ExpressionOperatorJpqlFunction(ExpressionOperator.sum()));
        functions.put("avg", new ExpressionOperatorJpqlFunction(ExpressionOperator.average()));
        functions.put("max", new ExpressionOperatorJpqlFunction(ExpressionOperator.maximum()));
        functions.put("min", new ExpressionOperatorJpqlFunction(ExpressionOperator.minimum()));
        functions.put("stddev", new ExpressionOperatorJpqlFunction(ExpressionOperator.standardDeviation()));
        functions.put("var", new ExpressionOperatorJpqlFunction(ExpressionOperator.variance()));

        return functions;
    }

    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        AbstractSession session = entityManagerFactory.unwrap(JpaEntityManagerFactory.class).getDatabaseSession();
        DatabasePlatform platform = session.getPlatform();
        @SuppressWarnings("unchecked")
        Map<Integer, ExpressionOperator> platformOperators = platform.getPlatformOperators();
        String dbms;

        // Register compatibility functions
        if (!dbmsFunctions.containsKey(CountStarFunction.FUNCTION_NAME)) {
            JpqlFunctionGroup jpqlFunctionGroup = new JpqlFunctionGroup(CountStarFunction.FUNCTION_NAME, true);
            jpqlFunctionGroup.add(null, new CountStarFunction());
            dbmsFunctions.put(CountStarFunction.FUNCTION_NAME, jpqlFunctionGroup);
        }

        platform.setShouldBindLiterals(false);

        if (platform.isMySQL()) {
            EntityManager em = entityManagerFactory.createEntityManager();
            EntityTransaction tx = null;
            boolean startedTransaction = false;
            try {
                tx = em.getTransaction();
                startedTransaction = !tx.isActive();
                if (startedTransaction) {
                    tx.begin();
                }
                Connection connection = em.unwrap(Connection.class);
                if (connection.getMetaData().getDatabaseMajorVersion() > 7) {
                    dbms = "mysql8";
                } else {
                    dbms = "mysql";
                }
            } catch (Exception ex) {
                throw new RuntimeException("Could not determine the MySQL Server version!", ex);
            } finally {
                if (startedTransaction) {
                    tx.commit();
                }
                em.close();
            }
        } else if (platform.isOracle()) {
            dbms = "oracle";
        } else if (platform.isSQLServer()) {
            dbms = "microsoft";
        } else if (platform.isSybase()) {
            dbms = "sybase";
        } else if (platform.isH2()) {
            dbms = "h2";
        } else {
            dbms = null;
        }

        final Map<Class<?>, String> classTypes = getClassToTypeMap(platform);
        for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey();
            JpqlFunctionGroup dbmsFunctionMap = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionMap.get(dbms);
            
            if (function == null) {
                function = dbmsFunctionMap.get(null);
            }
            if (function == null) {
                LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
            } else {
                addFunction(platformOperators, functionName, function, session, classTypes);
            }
        }
        
        return entityManagerFactory;
    }

    private Map<Class<?>, String> getClassToTypeMap(DatabasePlatform platform) {
        Map<Class, FieldTypeDefinition> fieldTypes = platform.getFieldTypes();
        Map<Class<?>, String> classToTypesMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Class, FieldTypeDefinition> entry : fieldTypes.entrySet()) {
            FieldTypeDefinition fieldTypeDefinition = entry.getValue();
            sb.setLength(0);
            sb.append(fieldTypeDefinition.getName());
            if (fieldTypeDefinition.isSizeRequired()) {
                sb.append('(');
                sb.append(fieldTypeDefinition.getDefaultSize());
                int defaultSubSize = fieldTypeDefinition.getDefaultSubSize();
                if (defaultSubSize != 0) {
                    sb.append(',');
                    sb.append(defaultSubSize);
                }
                sb.append(')');
            }
            classToTypesMap.put(entry.getKey(), sb.toString());
        }
        return classToTypesMap;
    }
    
    private void addFunction(Map<Integer, ExpressionOperator> platformOperators, String name, JpqlFunction function, AbstractSession session, Map<Class<?>, String> classTypes) {
        ExpressionOperator operator = createOperator(name, function, session, classTypes);
        ExpressionOperator.registerOperator(operator.getSelector(), operator.getName());
        ExpressionOperator.addOperator(operator);
        platformOperators.put(Integer.valueOf(operator.getSelector()), operator);
    }
    
    private ExpressionOperator createOperator(String name, JpqlFunction function, AbstractSession session, Map<Class<?>, String> classTypes) {
        ExpressionOperator operator = new JpqlFunctionExpressionOperator(function, session, classTypes);
        operator.setType(ExpressionOperator.FunctionOperator);
        operator.setSelector(functionSelectorCounter++);
        operator.setName(name);
//        Vector v = new Vector();
//        v.add("TRIM(LEADING ");
//        v.add(" FROM ");
//        v.add(")");
//        operator.printsAs(v);
//        operator.bePrefix();
//        int[] argumentIndices = new int[2];
//        argumentIndices[0] = 1;
//        argumentIndices[1] = 0;
//        operator.setArgumentIndices(argumentIndices);
        operator.setNodeClass(ClassConstants.FunctionExpression_Class);
        operator.setIsBindingSupported(false);
        return operator;
    }
}
