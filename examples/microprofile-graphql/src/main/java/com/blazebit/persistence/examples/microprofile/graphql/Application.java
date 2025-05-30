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

package com.blazebit.persistence.examples.microprofile.graphql;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.examples.microprofile.graphql.model.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class Application {

    @Inject
    EntityManager em;

    @Transactional
    void onStart(@Observes @Initialized(ApplicationScoped.class) Object ev) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Person p = new Person("Person " + i);
            people.add(p);
            em.persist(p);
        }
        for (int i = 0; i < 100; i++) {
            Cat c = new Cat("Cat " + i, random.nextInt(20), people.get(random.nextInt(4)));
            em.persist(c);
        }
    }
}
