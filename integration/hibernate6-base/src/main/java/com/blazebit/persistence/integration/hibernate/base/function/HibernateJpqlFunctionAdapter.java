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

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.model.domain.DomainType;
import org.hibernate.metamodel.model.domain.PersistentAttribute;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionRenderingSupport;
import org.hibernate.query.sqm.produce.function.FunctionReturnTypeResolver;
import org.hibernate.query.sqm.produce.function.StandardFunctionArgumentTypeResolvers;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.AbstractSqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateJpqlFunctionAdapter extends AbstractSqmSelfRenderingFunctionDescriptor implements FunctionRenderingSupport {

    private final JpqlFunction function;

    public HibernateJpqlFunctionAdapter(SessionFactoryImplementor sfi, JpqlFunction function) {
        super(null, null, new FunctionReturnTypeResolver() {
            @Override
            public ReturnableType<?> resolveFunctionReturnType(ReturnableType<?> impliedType, List<? extends SqmTypedNode<?>> arguments, TypeConfiguration typeConfiguration) {
                Class<?> argumentClass = null;
                if (arguments.isEmpty()) {
                    Class<?> returnType = function.getReturnType(null);
                    return returnType == null ? null : typeConfiguration.getBasicTypeForJavaType(returnType);
                } else {
                    final SqmTypedNode<?> specifiedArgument = arguments.get(0);
                    argumentClass = specifiedArgument.getNodeJavaType() == null ? null : specifiedArgument.getNodeJavaType().getJavaTypeClass();
                    Class<?> returnType = function.getReturnType(argumentClass);
                    if (returnType == null) {
                        return null;
                    } else if (returnType == argumentClass) {
                        SqmExpressible<?> nodeType = specifiedArgument.getNodeType();
                        if (nodeType instanceof PersistentAttribute<?, ?>) {
                            DomainType<?> type = ((PersistentAttribute<?, ?>) nodeType).getValueGraphType();
                            if (type instanceof ReturnableType<?>) {
                                return (ReturnableType<?>) type;
                            }
                            return null;
                        }
                        return (ReturnableType<?>) nodeType;
                    }
                    return typeConfiguration.getBasicTypeForJavaType(returnType);
                }
            }

            @Override
            public BasicValuedMapping resolveFunctionReturnType(Supplier<BasicValuedMapping> impliedTypeAccess, List<? extends SqlAstNode> arguments) {
                final SqlAstNode specifiedArgument = arguments.get(0);
                final JdbcMappingContainer specifiedArgType = specifiedArgument instanceof Expression
                        ? ( (Expression) specifiedArgument ).getExpressionType()
                        : null;

                Class<?> argumentClass;
                if ( specifiedArgType instanceof BasicValuedMapping ) {
                    argumentClass = ((BasicValuedMapping) specifiedArgType).getExpressibleJavaType().getJavaTypeClass();
                } else {
                    argumentClass = null;
                }

                Class<?> returnType = function.getReturnType(argumentClass);

                if (returnType == null) {
                    return null;
                } else if (argumentClass == returnType) {
                    return (BasicValuedMapping) specifiedArgType;
                }

                return sfi.getTypeConfiguration().getBasicTypeForJavaType(returnType);
            }
            // I hate to do this, but I have to hardcode this for now
        }, "com.blazebit.persistence.impl.function.entity.EntityFunction".equals(function.getClass().getName()) ? StandardFunctionArgumentTypeResolvers.IMPLIED_RESULT_TYPE : null);
        this.function = function;
    }

    public JpqlFunction unwrap() {
        return function;
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, SqlAstTranslator<?> walker) {
        // todo: this implementation needs to be improved i.e. don't do intermediate rendering if not required and handle parameter binders specially
        AbstractSqlAstTranslator<?> sqlAstTranslator = (AbstractSqlAstTranslator<?>) walker;
        CustomStandardSqlAstTranslator<?> customStandardSqlAstTranslator = new CustomStandardSqlAstTranslator<>(sqlAstTranslator.getSessionFactory());
        List<String> sqlArguments = new ArrayList<>(sqlAstArguments.size());
        for (SqlAstNode sqlAstArgument : sqlAstArguments) {
            customStandardSqlAstTranslator.getStringBuilder().setLength(0);
            sqlAstArgument.accept(customStandardSqlAstTranslator);
            sqlArguments.add(customStandardSqlAstTranslator.getSql());
        }

        sqlAstTranslator.getParameterBinders().addAll(customStandardSqlAstTranslator.getParameterBinders());
        HibernateFunctionRenderContext context = new HibernateFunctionRenderContext(sqlArguments);
        function.render(context);
        sqlAppender.appendSql(context.renderToString());
    }

}
