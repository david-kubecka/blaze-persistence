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

package com.blazebit.persistence.parser.util;

/**
 * A contract for converting values of one type to another as well as rendering as JPQL literal.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TypeConverter<T> {

    /**
     * Converts the given value to the type for which the converter was registered.
     *
     * @param value The value to convert.
     * @return The converted value
     * @throws IllegalArgumentException If the conversion is not possible
     */
    T convert(Object value);

    /**
     * Returns the JPQL literal representation of the given value as string.
     *
     * @param value The value
     * @return The JPQL literal
     */
    String toString(T value);

    /**
     * Appends the JPQL literal representation of the given value to the given string builder.
     *
     * @param value The value
     * @param stringBuilder The string builder
     */
    void appendTo(T value, StringBuilder stringBuilder);
}