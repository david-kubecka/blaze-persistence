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

package com.blazebit.persistence.impl.function.alias;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.4.1
 */
public class AliasFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "alias";

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
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        int size = context.getArgumentsSize();
        if (size != 2) {
            throw new RuntimeException("The " + FUNCTION_NAME + " function needs exactly two arguments!");
        }
        context.addArgument(0);
        context.addChunk(" as ");
        String alias = context.getArgument(1);
        context.addChunk(alias.substring(1, alias.length() - 1));
    }
}
