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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.CustomCollectionPersister;
import com.blazebit.persistence.integration.hibernate.base.SubselectLoaderUtils;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.persister.collection.BasicCollectionPersister;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class CustomBasicCollectionPersister extends BasicCollectionPersister implements CustomCollectionPersister {

    private final String mappedByProperty;

    public CustomBasicCollectionPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
        super(collection, cacheAccessStrategy, cfg, factory);
        String referencedPropertyName = collection.getReferencedPropertyName();
        if (referencedPropertyName == null && collection.isInverse()) {
            referencedPropertyName = findMappedByProperty(collection);
        }
        this.mappedByProperty = referencedPropertyName;
    }

    @SuppressWarnings("unchecked")
    private String findMappedByProperty(Collection collection) {
        String ownerEntityName = collection.getOwnerEntityName();
        Iterator<Column> columnIterator = collection.getKey().getColumnIterator();
        List<String> columnNames = new ArrayList<>();
        while (columnIterator.hasNext()) {
            Column column = columnIterator.next();
            columnNames.add(column.getName());
        }

        OneToMany oneToMany = (OneToMany) collection.getElement();
        Iterator propertyIterator = oneToMany.getAssociatedClass().getPropertyIterator();
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            if (property.getValue() instanceof ToOne) {
                ToOne toOne = (ToOne) property.getValue();
                if (ownerEntityName.equals(toOne.getReferencedEntityName())
                        && matches(columnNames, collection.getKey().getColumnIterator())) {
                    return property.getName();
                }
            }
        }

        return null;
    }

    private boolean matches(List<String> columns, Iterator<Column> iter) {
        for (int i = 0; iter.hasNext(); i++) {
            Column column = iter.next();
            if (i == columns.size() || !columns.get(i).equals(column.getName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getMappedByProperty() {
        return mappedByProperty;
    }

    @Override
    protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
        return new CustomSubselectCollectionLoader(
                this,
                SubselectLoaderUtils.getSubselectQueryForHibernatePre5(subselect.toSubselectString(getCollectionType().getLHSPropertyName())),
                subselect.getResult(),
                subselect.getQueryParameters(),
                subselect.getNamedParameterLocMap(),
                session.getFactory(),
                session.getLoadQueryInfluencers()
        );
    }
}
