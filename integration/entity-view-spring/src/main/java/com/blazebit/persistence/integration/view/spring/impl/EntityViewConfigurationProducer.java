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
package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Configuration
public class EntityViewConfigurationProducer {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
    private PlatformTransactionManager tm;

    public EntityViewConfigurationProducer(Set<Class<?>> entityViewClasses, Set<Class<?>> entityViewListenerClasses) {
        for (Class<?> entityViewClass : entityViewClasses) {
            configuration.addEntityView(entityViewClass);
        }
        for (Class<?> entityViewListenerClass : entityViewListenerClasses) {
            configuration.addEntityViewListener(entityViewListenerClass);
        }
    }

    @Autowired(required = false)
    public void setTm(PlatformTransactionManager tm) {
        this.tm = tm;
    }

    @Bean
    public EntityViewConfiguration getEntityViewConfiguration() {
        if (tm != null) {
            configuration.setTransactionSupport(new SpringTransactionSupport(tm));
        }
        return configuration;
    }


}
