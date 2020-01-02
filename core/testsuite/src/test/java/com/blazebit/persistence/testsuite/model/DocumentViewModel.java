/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.testsuite.model;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class DocumentViewModel {

    private String name;
    private String ownerName;
    private String firstLocalizedItem;
    private String partnerDocumentName;

    public DocumentViewModel(String name) {
        this.name = name;
    }

    public DocumentViewModel(String name, String ownerName, String firstLocalizedItem, String secondLocalizedItem) {
        this.name = name;
        this.ownerName = ownerName;
        this.firstLocalizedItem = firstLocalizedItem;
        this.partnerDocumentName = secondLocalizedItem;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getFirstLocalizedItem() {
        return firstLocalizedItem;
    }

    public String getPartnerDocumentName() {
        return partnerDocumentName;
    }
}
