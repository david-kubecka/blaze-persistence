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

package com.blazebit.persistence.view.impl.change;

import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodMapAttribute;
import com.blazebit.persistence.view.metamodel.MethodPluralAttribute;
import com.blazebit.persistence.view.metamodel.MethodSingularAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractImmutablePluralChangeModel<C, V> extends AbstractImmutableChangeModel<C, V> implements PluralChangeModel<C, V> {

    public AbstractImmutablePluralChangeModel(ManagedViewTypeImplementor<V> type, BasicTypeImpl<V> basicType, C initial, C current) {
        super(type, basicType, initial, current);
    }

    @Override
    public boolean isDirty(String attributePath) {
        validateAttributePath(type, attributePath);
        return false;
    }

    @Override
    public boolean isChanged(String attributePath) {
        validateAttributePath(type, attributePath);
        return false;
    }

    @Override
    public <X> List<? extends ChangeModel<X>> get(String attributePath) {
        validateAttributePath(type, attributePath);
        return Collections.emptyList();
    }

    @Override
    public <X> List<SingularChangeModel<X>> get(SingularAttribute<V, X> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <E, C extends Collection<E>> List<PluralChangeModel<C, E>> get(PluralAttribute<V, C, E> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <K, E> List<MapChangeModel<K, E>> get(MapAttribute<V, K, E> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <X> List<SingularChangeModel<X>> get(MethodSingularAttribute<V, X> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <E, C extends Collection<E>> List<PluralChangeModel<C, E>> get(MethodPluralAttribute<V, C, E> attribute) {
        return Collections.emptyList();
    }

    @Override
    public <K, E> List<MapChangeModel<K, E>> get(MethodMapAttribute<V, K, E> attribute) {
        return Collections.emptyList();
    }

    @Override
    protected <X> ChangeModel<X> get(AbstractMethodAttribute<?, ?> methodAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SingularChangeModel<V>> getElementChanges() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<V>> getAddedElements() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<V>> getRemovedElements() {
        return Collections.emptyList();
    }

    @Override
    public List<SingularChangeModel<V>> getMutatedElements() {
        return Collections.emptyList();
    }
}
