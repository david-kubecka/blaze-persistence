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

package com.blazebit.persistence.view.spi.type;

/**
 * The default basic user type implementation for unknown types.
 *
 * @param <X> The type of the user type
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MutableBasicUserType<X> extends AbstractMutableBasicUserType<X> {

    public static final BasicUserType<?> INSTANCE = new MutableBasicUserType<>();

    @Override
    public boolean supportsDeepEqualChecking() {
        return false;
    }

    @Override
    public boolean supportsDeepCloning() {
        return false;
    }

    @Override
    public boolean isEqual(X initial, X current) {
        return false;
    }

    @Override
    public boolean isDeepEqual(X initial, X current) {
        return false;
    }

    @Override
    public int hashCode(X object) {
        return 0;
    }

    @Override
    public X deepClone(X object) {
        return object;
    }

    @Override
    public X fromString(CharSequence sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toStringExpression(String expression) {
        throw new UnsupportedOperationException();
    }
}
