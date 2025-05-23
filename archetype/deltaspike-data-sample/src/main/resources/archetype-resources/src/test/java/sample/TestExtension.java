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

package ${package}.sample;

import ${package}.config.EntityManagerProducer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * In the test, we have to produce the EntityManagerFactory and EntityManager manually
 */
public class TestExtension implements Extension {
    
    <X> void discover(@Observes ProcessAnnotatedType<X> type) {
        if (EntityManagerProducer.class == type.getAnnotatedType().getJavaClass()) {
            type.veto();
        }
    }
}
