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

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedMapping;
import org.hibernate.query.ReturnableType;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.type.descriptor.java.JavaType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class HibernateSqmFunctionDescriptorAdapter implements JpqlFunction {

    private final SessionFactoryImplementor sfi;
    private final SqmFunctionDescriptor function;

    public HibernateSqmFunctionDescriptorAdapter(SessionFactoryImplementor sfi, SqmFunctionDescriptor function) {
        this.sfi = sfi;
        this.function = function;
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        if (firstArgumentType == null) {
            return null;
        }
        SqmExpressible<?> type = sfi.getTypeConfiguration().getBasicTypeForJavaType(firstArgumentType);

        if (type == null) {
            final JavaType<Object> javaType = sfi.getTypeConfiguration().getJavaTypeRegistry().getDescriptor(firstArgumentType);
            type = new ReturnableType<Object>() {
                @Override
                public JavaType<Object> getExpressibleJavaType() {
                    return javaType;
                }

                @Override
                public Class<Object> getBindableJavaType() {
                    return (Class<Object>) firstArgumentType;
                }

                @Override
                public PersistenceType getPersistenceType() {
                    return PersistenceType.BASIC;
                }

                @Override
                public Class<Object> getJavaType() {
                    return (Class<Object>) firstArgumentType;
                }
            };
        }

        List<SqmTypedNode<?>> arguments = new ArrayList<>(1);
        arguments.add(new CustomSqmTypedNode<>(type));
        SqmExpressible<?> expressionType = function.generateSqmExpression(arguments, null, sfi.getQueryEngine(), sfi.getTypeConfiguration())
                .getNodeType();

        return expressionType == null ? null : expressionType.getBindableJavaType();
    }

    @Override
    public void render(FunctionRenderContext context) {
        throw new UnsupportedOperationException("Rendering functions through this API is not possible!");
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static class CustomSqmTypedNode<T> implements SqmTypedNode<T>, SqmVisitableNode {

        private final SqmExpressible<T> type;

        private CustomSqmTypedNode(SqmExpressible<T> type) {
            this.type = type;
        }

        public <X> X accept(SemanticQueryWalker<X> walker) {
            return (X) new QueryLiteral(null, (BasicValuedMapping) type);
        }

        @Override
        public SqmTypedNode<T> copy(SqmCopyContext context) {
            return this;
        }

        @Override
        public SqmExpressible<T> getNodeType() {
            return type;
        }

        @Override
        public NodeBuilder nodeBuilder() {
            return null;
        }

        @Override
        public void appendHqlString(StringBuilder sb) {
            // No-op
        }
    }

}
