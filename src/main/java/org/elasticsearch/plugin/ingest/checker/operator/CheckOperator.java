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

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import java.util.Locale;

public class CheckOperator {
    private Operator operator;
    private Object argument;

    public static class InvalidTypeException extends IllegalArgumentException {}
    public static class InvalidNameException extends IllegalArgumentException {}

    enum OperatorType {
        STRING {
            protected Operator[] getOperators() {
                return StringOperator.values();
            }
        },
        INTEGER {
            protected Operator[] getOperators() {
                return IntegerOperator.values();
            }
        };

        protected Operator[] getOperators() {
            return new Operator[]{};
        }

        Operator findOperatorByName(String name) {
            for (Operator operator : this.getOperators()) {
                if (operator.toString().toLowerCase(Locale.US).equals(name)) {
                    return operator;
                }
            }

            throw new InvalidNameException();
        }

        static OperatorType findByName(String typeName) {
            for (OperatorType operatorType : OperatorType.values()) {
                if (operatorType.name().toLowerCase(Locale.US).equals(typeName)) {
                    return operatorType;
                }
            }

            throw new InvalidTypeException();
        }
    }

    interface Operator {
        Boolean exec(Object value, Object argument);
    }

    enum StringOperator implements Operator {
        EQUAL {
            public Boolean exec(Object value, Object argument) {
                return value.toString().equals(argument.toString());
            }
        };

        public Boolean exec(Object value, Object argument) {
            return false;
        }
    }

    enum IntegerOperator implements Operator {
        EQUAL {
            Boolean exec(Integer value, Integer argument) {
                return value.equals(argument);
            }
        };

        public Boolean exec(Object value, Object argument) {
            return false;
        }
    }

    public CheckOperator(String type, String name, Object argument) {
        this.operator = OperatorType.findByName(type).findOperatorByName(name);
        this.argument = argument;
    }

    public Boolean exec(String value) {
        return operator.exec(value, argument.toString());
    }

    public Boolean exec(Integer value) {
        return operator.exec(value, Integer.valueOf(argument.toString()));
    }
}
