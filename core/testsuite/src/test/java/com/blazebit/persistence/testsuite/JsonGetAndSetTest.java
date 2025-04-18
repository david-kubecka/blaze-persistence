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
package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.JsonDocument;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class JsonGetAndSetTest extends AbstractCoreTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] { JsonDocument.class };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        String objectRootJsonDocument = "{ \"K1\": [ " +
                        "{\"K2\": 1}, {\"K2\": \"test\"}, [ 0, 1 ], null, \"null\", true " +
                        "], \"1\": 4, \"key with blanks\": 3 }";
        String arrayRootJsonDocument = "[ 1, {\"K2\": 2} ]";
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                JsonDocument d1 = new JsonDocument(1L, objectRootJsonDocument);
                JsonDocument d2 = new JsonDocument(2L, arrayRootJsonDocument);
                em.persist(d1);
                em.persist(d2);
            }
        });
    }

    @Test
    @Category({ NoH2.class, NoSQLite.class, NoFirebird.class, NoMySQLOld.class })
    public void testJsonGet() throws JsonProcessingException {
        List<Tuple> objectRootResult = cbf.create(em, Tuple.class).from(JsonDocument.class, "d")
                .select("d.content")
                .select("cast_integer(json_get(d.content, 'K1', '0', 'K2'))")
                .select("json_get(d.content, 'K1', '1', 'K2')")
                .select("json_get(d.content, 'K1', '0', 'K3')")
                .select("json_get(d.content, 'K1', '2', 'K3')")
                .select("cast_integer(json_get(d.content, 'K1', '2', '0'))")
                .select("json_get(d.content, 'K1', '3')")
                .select("json_get(d.content, 'K1', '4')")
                .select("CASE WHEN json_get(d.content, 'K1', '5') = 'true' THEN true ELSE false END")
                .select("json_get(d.content, 'K1')")
                .select("json_get(d.content, 'K1', '0')")
                .select("json_get(d.content, '\"1\"')")
                .select("json_get(d.content, 'key with blanks')")
                .where("id").eq(1L)
                .getResultList();
        List<Tuple> arrayRootResult = cbf.create(em, Tuple.class).from(JsonDocument.class, "d")
                .select("d.content")
                .select("cast_integer(json_get(d.content, '0'))")
                .select("cast_integer(json_get(d.content, '1', 'K2'))")
                .where("id").eq(2L)
                .getResultList();

        assertEquals(1, objectRootResult.size());
        JsonNode jsonTestDocument = objectMapper.readTree((String) objectRootResult.get(0).get(0));
        assertEquals(1, objectRootResult.get(0).get(1));
        assertEquals("test", objectRootResult.get(0).get(2));
        assertNull(objectRootResult.get(0).get(3));
        assertNull(objectRootResult.get(0).get(4));
        assertEquals(0, objectRootResult.get(0).get(5));
        assertNull(objectRootResult.get(0).get(6));
        assertEquals("null", objectRootResult.get(0).get(7));
        assertTrue((boolean) objectRootResult.get(0).get(8));
        assertEquals(jsonTestDocument.get("K1"), objectMapper.readTree((String) objectRootResult.get(0).get(9)));
        assertEquals(jsonTestDocument.get("K1").get(0), objectMapper.readTree((String) objectRootResult.get(0).get(10)));
        assertEquals(jsonTestDocument.get("1"), objectMapper.readTree((String) objectRootResult.get(0).get(11)));
        assertEquals(jsonTestDocument.get("key with blanks"), objectMapper.readTree((String) objectRootResult.get(0).get(12)));

        assertEquals(1, arrayRootResult.size());
        assertEquals(1, arrayRootResult.get(0).get(1));
        assertEquals(2, arrayRootResult.get(0).get(2));
    }

    @Test
    @Category({ NoH2.class, NoSQLite.class, NoFirebird.class, NoMySQLOld.class })
    public void testJsonSet() throws JsonProcessingException {
        List<Tuple> objectRootResult = cbf.create(em, Tuple.class).from(JsonDocument.class, "d")
                .select("d.content")
                .select("json_set(d.content, '2', 'K1', '0', 'K2')")
                .select("json_set(d.content, '2.5', 'K1', '0', 'K2')")
                .select("json_set(d.content, '2', 'K1', '0')")
                .select("json_set(d.content, '2', 'K1', '2')")
                .select("json_set(d.content, '2', 'K1', '5')")
                .select("json_set(d.content, '\"test\"', 'K1', '0', 'K2')")
                .select("json_set(d.content, '\"test\"', 'K1', '0')")
                .select("json_set(d.content, 'true', 'K1', '0', 'K2')")
                .select("json_set(d.content, 'true', 'K1', '0')")
                .select("json_set(d.content, '4', 'key with blanks')")
                .where("id").eq(1L)
                .getResultList();
        List<Tuple> arrayRootResult = cbf.create(em, Tuple.class).from(JsonDocument.class, "d")
                .select("d.content")
                .select("json_set(d.content, '{\"K1\": 2}', '0')")
                .select("json_set(d.content, '3', '1', 'K2')")
                .where("id").eq(2L)
                .getResultList();
        assertEquals(1, objectRootResult.size());
        assertEquals(2, objectMapper.readTree((String) objectRootResult.get(0).get(1)).at("/K1/0/K2").intValue());
        assertEquals(2.5, objectMapper.readTree((String) objectRootResult.get(0).get(2)).at("/K1/0/K2").floatValue(), 0.001);
        assertEquals(2, objectMapper.readTree((String) objectRootResult.get(0).get(3)).at("/K1/0").intValue());

        assertEquals(2, objectMapper.readTree((String) objectRootResult.get(0).get(4)).at("/K1/2").intValue());

        assertEquals(2, objectMapper.readTree((String) objectRootResult.get(0).get(5)).at("/K1/5").intValue());

        assertEquals("test", objectMapper.readTree((String) objectRootResult.get(0).get(6)).at("/K1/0/K2").textValue());
        assertEquals("test", objectMapper.readTree((String) objectRootResult.get(0).get(7)).at("/K1/0").textValue());

        assertTrue(objectMapper.readTree((String) objectRootResult.get(0).get(8)).at("/K1/0/K2").booleanValue());
        assertTrue(objectMapper.readTree((String) objectRootResult.get(0).get(9)).at("/K1/0").booleanValue());

        assertEquals(4, objectMapper.readTree((String) objectRootResult.get(0).get(10)).at("/key with blanks").intValue());

        assertEquals(1, arrayRootResult.size());
        assertEquals(2, objectMapper.readTree((String) arrayRootResult.get(0).get(1)).at("/0/K1").intValue());
        assertEquals(3, objectMapper.readTree((String) arrayRootResult.get(0).get(2)).at("/1/K2").intValue());
    }

    @Test
    @Category({ NoH2.class, NoSQLite.class, NoFirebird.class, NoMySQLOld.class, NoOracle.class })
    public void testJsonSetNull() throws JsonProcessingException {
        List<Tuple> objectRootResult = cbf.create(em, Tuple.class).from(JsonDocument.class, "d")
                .select("d.content")
                // json_set with value null not supported for Oracle
                .select("json_set(d.content, 'null', 'K1', 0, 'K2')")
                .select("json_set(d.content, 'null', 'K1', 0)")
                .where("id").eq(1L)
                .getResultList();
        assertEquals(1, objectRootResult.size());
        assertTrue(objectMapper.readTree((String) objectRootResult.get(0).get(1)).at("/K1/0/K2").isNull());
        assertTrue(objectMapper.readTree((String) objectRootResult.get(0).get(2)).at("/K1/0").isNull());
    }
}
