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

package com.blazebit.persistence;

/**
 * A builder for cte criteria queries. This is the entry point for building cte queries.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface BaseCTECriteriaBuilder<X extends BaseCTECriteriaBuilder<X>> extends CommonQueryBuilder<X>, FromBuilder<X>, KeysetQueryBuilder<X>, WhereBuilder<X>, OrderByBuilder<X>, GroupByBuilder<X>, DistinctBuilder<X>, LimitBuilder<X>, WindowContainerBuilder<X> {
// TODO: maybe also add CTEBuilder?
}
