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

package com.blazebit.persistence.view.impl.update.listener;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostRollbackListener;
import com.blazebit.persistence.view.ViewTransition;
import com.blazebit.persistence.view.ViewTransitionListener;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewTransitionPostRollbackListenerImpl<T> implements PostRollbackListener<T> {

    private final ViewTransitionListener<T> listener;

    public ViewTransitionPostRollbackListenerImpl(ViewTransitionListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void postRollback(EntityViewManager entityViewManager, EntityManager entityManager, T view, ViewTransition transition) {
        listener.call(view, transition);
    }
}
