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
 * A dirty tracker that exposes the captured initial state.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@SuppressWarnings("checkstyle:methodname")
public interface DirtyStateTrackable extends MutableStateTrackable {

    /**
     * Returns the initial state as array. Null if not partially updatable.
     * The order is the same as the metamodel attribute order of updatable attributes.
     * 
     * @return the initial state as array
     */
    public Object[] $$_getInitialState();

}
