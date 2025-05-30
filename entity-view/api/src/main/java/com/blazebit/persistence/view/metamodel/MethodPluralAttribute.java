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
 * A plural attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <C> The type of the represented collection
 * @param <E> The element type of the represented collection
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodPluralAttribute<X, C, E> extends PluralAttribute<X, C, E>, MethodAttribute<X, C> {

}
