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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import java.util.List;
import ${package}.model.Cat;
import ${package}.model.Person;
import ${package}.view.CatSimpleView;
import ${package}.view.CatWithOwnerView;
import ${package}.view.PersonSimpleView;
import org.junit.Assert;
import org.junit.Test;

public class SampleTest extends AbstractSampleTest {

    @Test
    public void sampleTest() {
        transactional(em -> {
            CriteriaBuilder<Cat> catCriteriaBuilder = cbf.create(em, Cat.class);
            catCriteriaBuilder.from(Cat.class, "cat");

            EntityViewSetting<CatWithOwnerView, CriteriaBuilder<CatWithOwnerView>> setting = EntityViewSetting.create(CatWithOwnerView.class);
            CriteriaBuilder<CatWithOwnerView> cb = evm.applySetting(setting, catCriteriaBuilder);
            List<CatWithOwnerView> list = cb.getResultList();

            System.out.println(list);
            Assert.assertEquals(6, list.size());
        });
    }
}
