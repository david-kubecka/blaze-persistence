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

package com.blazebit.persistence.spring.data.testsuite.webflux.controller;

import com.blazebit.persistence.spring.data.testsuite.webflux.repository.ModificationPersonRepository;
import com.blazebit.persistence.spring.data.testsuite.webflux.repository.PersonRepository;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonView;
import com.blazebit.persistence.spring.data.webflux.EntityViewId;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@RestController
public class PersonController {

    private final PersonRepository personRepository;
    private final ModificationPersonRepository modificationPersonRepository;

    public PersonController(PersonRepository personRepository, ModificationPersonRepository modificationPersonRepository) {
        this.personRepository = personRepository;
        this.modificationPersonRepository = modificationPersonRepository;
    }

    @PutMapping(
            value = "/persons/{id1}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PersonView> updatePerson(@EntityViewId("id1") @RequestBody PersonUpdateView personUpdate) {
        return updatePerson0(personUpdate);
    }

    private ResponseEntity<PersonView> updatePerson0(PersonUpdateView personUpdate) {
        modificationPersonRepository.updatePerson(personUpdate);
        PersonView personView = personRepository.findOne(personUpdate.getId().toString());
        return ResponseEntity.ok(personView);
    }
}
