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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.entity.ElementToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushCollectionElementByIdDeleter implements PostFlushDeleter {

    private final ElementToEntityMapper elementToEntityMapper;
    private final List<Object> elementIds;

    public PostFlushCollectionElementByIdDeleter(ElementToEntityMapper elementToEntityMapper, List<Object> elementIds) {
        this.elementToEntityMapper = elementToEntityMapper;
        this.elementIds = elementIds;
    }

    @Override
    public void execute(UpdateContext context) {
        for (Object elementId : elementIds) {
            elementToEntityMapper.removeById(context, elementId);
        }
    }
}
