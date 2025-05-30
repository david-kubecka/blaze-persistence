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

import javax.persistence.TypedQuery;

/**
 * A builder for paginated criteria queries.
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface PaginatedCriteriaBuilder<T> extends FullQueryBuilder<T, PaginatedCriteriaBuilder<T>> {

    /**
     * Returns the paginated typed query for the built query.
     * The returned query is already parameterized with all known parameters.
     *
     * @return The paginated typed query for the built query
     */
    @Override
    public PaginatedTypedQuery<T> getQuery();

    /**
     * Returns the count query that selects the count of root elements.
     * This is the same query that is used to compute {@link PaginatedTypedQuery#getTotalCount()}.
     *
     * @return A query for determining the count of the root elements represented by this query builder
     * @since 1.3.0
     */
    @Override
    public TypedQuery<Long> getCountQuery();

    /**
     * Delegates to {@link #getPageCountQueryString()}.
     *
     * @return The query string
     * @since 1.3.0
     */
    @Override
    public String getCountQueryString();

    /**
     * Returns the query string that selects the count of elements.
     *
     * @return The query string
     */
    public String getPageCountQueryString();

    /**
     * Returns the query string that selects the id of the elements.
     *
     * @return The query string
     */
    public String getPageIdQueryString();

    /**
     * Enable or disables keyset extraction which influences whether {@link PagedList#getKeysetPage()} is available.
     * 
     * @param keysetExtraction true to enable, false to disable keyset extraction
     * @return The query builder for chaining calls
     */
    public PaginatedCriteriaBuilder<T> withKeysetExtraction(boolean keysetExtraction);

    /**
     * Returns whether keyset extraction is enabled or not.
     * 
     * @return true when enabled, false otherwise
     */
    public boolean isKeysetExtraction();

    /**
     * Enable or disables keyset extraction for all rows which influences whether {@link PagedList#getKeysetPage()} is available.
     *
     * @param withExtractAllKeysets true to enable, false to disable keyset extraction for all rows
     * @return The query builder for chaining calls
     * @since 1.4.0
     */
    public PaginatedCriteriaBuilder<T> withExtractAllKeysets(boolean withExtractAllKeysets);

    /**
     * Returns whether keyset extraction is enabled or not.
     *
     * @return true when enabled, false otherwise
     * @since 1.4.0
     */
    public boolean isWithExtractAllKeysets();

    /**
     * Enables or disables execution of the count query which determines whether {@link PagedList#getTotalSize()} is available.
     *
     * @param withCountQuery true to enable, false to disable the count query execution
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public PaginatedCriteriaBuilder<T> withCountQuery(boolean withCountQuery);

    /**
     * Returns whether count query execution is enabled or not.
     *
     * @return true when enabled, false otherwise
     * @since 1.2.0
     */
    public boolean isWithCountQuery();

    /**
     * Sets the maximum value up to which a count query should count.
     *
     * @param maximumCount the maximum value up to which should be counted
     * @return The query builder for chaining calls
     * @since 1.5.0
     */
    public PaginatedCriteriaBuilder<T> withBoundedCount(long maximumCount);

    /**
     * Returns the maximum value up to which a count query should count.
     *
     * @return the maximum value up to which a count query should count
     * @since 1.5.0
     */
    public long getBoundedCount();

    /**
     * Forces the use of an id query even if the pagination would not need it.
     *
     * @param withForceIdQuery true to force id query use, false otherwise
     * @return The query builder for chaining calls
     * @since 1.3.0
     */
    public PaginatedCriteriaBuilder<T> withForceIdQuery(boolean withForceIdQuery);

    /**
     * Returns whether id query use is forced.
     *
     * @return true when id query use is forced, false otherwise
     * @since 1.3.0
     */
    public boolean isWithForceIdQuery();

    /**
     * Sets the offset for the highest keyset which influences which element of a page is returned by {@link KeysetPage#getHighest()}.
     * This is usually used when loading N + 1 rows to know there are further rows but only needing N rows.
     *
     * @param offset the offset for the highest keyset relative to the page size
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public PaginatedCriteriaBuilder<T> withHighestKeysetOffset(int offset);

    /**
     * Returns the offset of the highest keyset relative to the page size.
     *
     * @return the offset for the highest keyset relative to the page size
     * @since 1.2.0
     */
    public int getHighestKeysetOffset();

    /**
     * Forces the inlining of an id query into the object query.
     *
     * @param withInlineIdQuery true to inline the id query, false otherwise
     * @return The query builder for chaining calls
     * @since 1.4.1
     */
    public PaginatedCriteriaBuilder<T> withInlineIdQuery(boolean withInlineIdQuery);

    /**
     * Returns whether id query should be inlined.
     *
     * @return true when id query should be inlined, false otherwise
     * @since 1.4.1
     */
    public boolean isWithInlineIdQuery();

    /**
     * Forces the inlining of an count query into the object or id query.
     *
     * @param withInlineCountQuery true to inline the count query, false otherwise
     * @return The query builder for chaining calls
     * @since 1.4.1
     */
    public PaginatedCriteriaBuilder<T> withInlineCountQuery(boolean withInlineCountQuery);

    /**
     * Returns whether count query should be inlined.
     *
     * @return true when count query should be inlined, false otherwise
     * @since 1.4.1
     */
    public boolean isWithInlineCountQuery();

    /**
     * Creates and returns a new {@link CriteriaBuilder} that can be used to query the id values for the current page.
     *
     * @return the {@link CriteriaBuilder} to query id values
     * @since 1.4.1
     */
    public CriteriaBuilder<Object[]> createPageIdQuery();

    /**
     * Execute the query and return the result as a type PagedList.
     *
     * @return The paged list of the results
     */
    @Override
    public PagedList<T> getResultList();

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> copy(Class<Y> resultClass);

    @Override
    public <Y> SelectObjectBuilder<PaginatedCriteriaBuilder<Y>> selectNew(Class<Y> clazz);

    @Override
    public <Y> PaginatedCriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder);

}
