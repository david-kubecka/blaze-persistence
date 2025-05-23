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

package com.blazebit.persistence.impl.function.stringxmlagg;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.groupconcat.AbstractGroupConcatFunction;
import com.blazebit.persistence.impl.function.replace.ReplaceFunction;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class OracleGroupConcatBasedStringXmlAggFunction extends GroupConcatBasedStringXmlAggFunction {

    public OracleGroupConcatBasedStringXmlAggFunction(AbstractGroupConcatFunction groupConcatFunction, ReplaceFunction replaceFunction, ConcatFunction concatFunction) {
        super(groupConcatFunction, replaceFunction, concatFunction);
    }

    @Override
    protected String coalesceStart() {
        return "nullif(";
    }

    @Override
    protected String coalesceEnd(String field) {
        return "," + concatFunction.startConcat() + "'<'" + concatFunction.concatSeparator() + field + concatFunction.concatSeparator() + "'></'" + concatFunction.concatSeparator() + field + concatFunction.concatSeparator() + "'>'" + concatFunction.endConcat() + ")";
    }

}