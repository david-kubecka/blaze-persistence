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

package com.blazebit.persistence.view.metamodel;

/**
 * Represents an attribute of a view type specified by a constructor parameter.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ParameterAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the index of the parameter within the constructor.
     *
     * @return The index of the parameter within the constructor
     */
    public int getIndex();

    /**
     * Returns the declaring constructor.
     *
     * @return The declaring constructor
     */
    public MappingConstructor<X> getDeclaringConstructor();

    /**
     * Returns whether the parameter is a "self" parameter i.e. annotated with {@link com.blazebit.persistence.view.Self}.
     *
     * @return Whether the parameter is a self parameter
     * @since 1.5.0
     */
    public boolean isSelfParameter();
}
