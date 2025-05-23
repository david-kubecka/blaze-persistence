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

package com.blazebit.persistence.integration.graphql.spqr;

import com.blazebit.persistence.integration.graphql.spqr.model.Cat;
import com.blazebit.persistence.integration.graphql.spqr.model.Person;
import com.blazebit.persistence.integration.graphql.spqr.repository.CatJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@SpringBootApplication
@EnableSpringDataWebSupport
@ComponentScan(basePackages = "com.blazebit.persistence.integration.graphql.spqr")
public class Application {

    @Autowired
    EntityManager em;
    @Autowired
    CatJpaRepository catJpaRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class).getBean(Application.class).run();
    }

    @Transactional
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Person p = new Person("Person " + i);
            people.add(p);
            em.persist(p);
        }
        for (int i = 0; i < 100; i++) {
            Cat c = new Cat("Cat " + i, random.nextInt(20), people.get(random.nextInt(4)));
            catJpaRepository.save(c);
        }
    }
}
