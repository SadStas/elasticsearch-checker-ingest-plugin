/*
 * Copyright [2017] [Sadovyi Stanislav]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.checker.operator;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrepareOperator {
    private Operator operator;
    private String argument;
    private Integer item;

    enum Operator {
        SPLIT {
            String exec(String value, String argument, Integer item) {
                String[] items = value.split(argument);
                return item < items.length ? items[item] : null;
            }
        },
        MATCH {
            String exec(String value, String argument, Integer item) {
                Matcher matcher = Pattern.compile(argument).matcher(value);
                Boolean isMatches = matcher.find() && matcher.groupCount() <= item + 1;

                return isMatches ? matcher.group(item + 1) : null;
            }
        };

        String exec(String value, String argument, Integer item) {
            return null;
        }
    }

    public static class InvalidNameException extends IllegalArgumentException {}
    public static class InvalidValueException extends IllegalArgumentException {}

    public PrepareOperator(String name, String argument, Integer item) {
        this.operator = Operator.valueOf(name.toUpperCase(Locale.US));
        this.argument = argument;
        this.item = item;
    }

    public Object exec(Object value) {
        if (!(value instanceof String)) {
            throw new InvalidValueException();
        }

        return operator.exec(value.toString(), argument, item);
    }
}
