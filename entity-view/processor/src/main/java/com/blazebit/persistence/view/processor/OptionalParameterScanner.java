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

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class OptionalParameterScanner {

    private OptionalParameterScanner() {
    }

    public static void scan(Map<String, TypeElement> optionalParameters, ExecutableElement executableElement, Context context) {
        try {
            JavacOptionalParameterScanner.scan(optionalParameters, executableElement, context);
        } catch (Exception ex) {
            // Ignore
        }
    }
}
