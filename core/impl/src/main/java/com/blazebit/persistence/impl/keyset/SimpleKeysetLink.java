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

package com.blazebit.persistence.impl.keyset;

import java.util.List;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.impl.OrderByExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SimpleKeysetLink extends AbstractKeysetLink {

    private final Keyset keyset;

    public SimpleKeysetLink(Keyset keyset, KeysetMode keysetMode) {
        super(keysetMode);

        if (keyset == null) {
            throw new NullPointerException("keyset");
        }

        this.keyset = keyset;
    }

    @Override
    public void initialize(List<OrderByExpression> orderByExpressions) {
        validate(keyset, orderByExpressions);
    }

    @Override
    public Keyset getKeyset() {
        return keyset;
    }
}
