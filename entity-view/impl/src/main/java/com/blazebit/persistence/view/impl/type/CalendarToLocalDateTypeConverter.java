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

package com.blazebit.persistence.view.impl.type;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class CalendarToLocalDateTypeConverter<T extends Calendar> extends AbstractLocalDateTypeConverter<T> {

    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    public static final CalendarToLocalDateTypeConverter<Calendar> JAVA_UTIL_CALENDAR_CONVERTER = new CalendarToLocalDateTypeConverter<Calendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return Calendar.class;
        }

        @Override
        public Calendar convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(UTC_TIMEZONE);
            calendar.setTimeInMillis(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
            return calendar;
        }

    };

    public static final CalendarToLocalDateTypeConverter<GregorianCalendar> JAVA_UTIL_GREGORIAN_CALENDAR_CONVERTER = new CalendarToLocalDateTypeConverter<GregorianCalendar>() {

        @Override
        public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
            return GregorianCalendar.class;
        }

        @Override
        public GregorianCalendar convertToUnderlyingType(Object object) {
            if (object == null) {
                return null;
            }
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeZone(UTC_TIMEZONE);
            gregorianCalendar.setTimeInMillis(toEpochDay(object) * MILLISECOND_CONVERSION_FACTOR);
            return gregorianCalendar;
        }

    };

    @Override
    public Object convertToViewType(Calendar object) {
        if (object == null) {
            return null;
        }
        return ofEpochDay(object.getTimeInMillis() / MILLISECOND_CONVERSION_FACTOR);
    }
}
