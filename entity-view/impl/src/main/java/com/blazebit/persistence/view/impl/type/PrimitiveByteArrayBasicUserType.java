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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.Arrays;
import java.util.Base64;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrimitiveByteArrayBasicUserType extends AbstractMutableBasicUserType<byte[]> implements BasicUserType<byte[]> {

    public static final BasicUserType<?> INSTANCE = new PrimitiveByteArrayBasicUserType();

    @Override
    public boolean isDeepEqual(byte[] object1, byte[] object2) {
        return Arrays.equals(object1, object2);
    }

    @Override
    public int hashCode(byte[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public byte[] deepClone(byte[] object) {
        return Arrays.copyOf(object, object.length);
    }

    @Override
    public byte[] fromString(CharSequence sequence) {
        return Base64.getDecoder().decode(sequence.toString());
    }

    @Override
    public String toStringExpression(String expression) {
        return "BASE64(" + expression + ")";
    }
}
