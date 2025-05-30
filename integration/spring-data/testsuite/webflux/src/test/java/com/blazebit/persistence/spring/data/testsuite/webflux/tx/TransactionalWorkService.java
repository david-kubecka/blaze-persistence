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

package com.blazebit.persistence.spring.data.testsuite.webflux.tx;

import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Service
@Transactional
public class TransactionalWorkService {

    @Autowired
    private EntityManager em;
    @Autowired
    private EntityViewManager evm;

    public <V> V doTxWork(TxWork<V> work) {
        return work.work(em, evm);
    }

    public void doTxWork(TxVoidWork work) {
        work.work(em, evm);
    }

}
