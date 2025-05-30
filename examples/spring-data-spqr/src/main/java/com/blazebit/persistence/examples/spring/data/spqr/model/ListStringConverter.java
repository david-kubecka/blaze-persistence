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

package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class ListStringConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if (strings == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        String[] strings = s.split("\\.");
        List<String> list = new ArrayList<>(strings.length);
        for (String element : strings) {
            list.add(element);
        }
        return list;
    }
}
