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

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class CheckOperator {
    private OperatorType type;
    private Operator operator;
    private Object argument;

    public static class InvalidTypeException extends IllegalArgumentException {}
    public static class InvalidNameException extends IllegalArgumentException {}
    public static class InvalidArgumentException extends IllegalArgumentException {}
    public static class InvalidValueException extends IllegalArgumentException {}

    enum OperatorType {
        STRING {
            protected Operator[] getOperators() {
                return StringOperator.values();
            }

            String getSupportedClassName() {
                return String.class.getName();
            }
        },
        INTEGER {
            protected Operator[] getOperators() {
                return IntegerOperator.values();
            }

            String getSupportedClassName() {
                return Integer.class.getName();
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

        String getSupportedClassName() {
            return Object.class.getName();
        }
    }

    interface Operator {
        Boolean exec(Object value, Object argument);
        String getArgumentType();
    }

    enum StringOperator implements Operator {
        EQUAL {
            public Boolean exec(Object value, Object argument) {
                return value.toString().equals(argument.toString());
            }
        },
        IN {
            public Boolean exec(Object value, Object argument) {
                return ((ArrayList)argument).contains(value);
            }

            public String getArgumentType() {
                return ArrayList.class.getName();
            }
        },
        MATCH {
            public Boolean exec(Object value, Object argument) {
                return Pattern.compile(argument.toString()).matcher(value.toString()).find();
            }
        },
        CONTAINS {
            public Boolean exec(Object value, Object argument) {
                return value.toString().contains(argument.toString());
            }
        };

        public Boolean exec(Object value, Object argument) {
            return false;
        }

        public String getArgumentType() {
            return String.class.getName();
        }
    }

    enum IntegerOperator implements Operator {
        EQUAL {
            public Boolean exec(Object value, Object argument) {
                return toInteger(value).equals(toInteger(argument));
            }
        },
        IN {
            public Boolean exec(Object value, Object argument) {
                return ((ArrayList)argument).contains(value);
            }

            public String getArgumentType() {
                return ArrayList.class.getName();
            }
        },
        MORE {
            public Boolean exec(Object value, Object argument) {
                return toInteger(value) > toInteger(argument);
            }
        },
        LESS {
            public Boolean exec(Object value, Object argument) {
                return toInteger(value) < toInteger(argument);
            }
        };

        public Boolean exec(Object value, Object argument) {
            return false;
        }

        public String getArgumentType() {
            return Integer.class.getName();
        }

        protected Integer toInteger(Object object) {
            return Integer.valueOf(object.toString());
        }
    }

    public CheckOperator(String type, String name, Object argument) {
        this.type = OperatorType.findByName(type);
        this.operator = this.type.findOperatorByName(name);

        if (!this.operator.getArgumentType().equals(argument.getClass().getName())) {
            throw new InvalidArgumentException();
        }
        this.argument = argument;
    }

    public Boolean exec(Object value) {
        if (value == null) {
            throw new InvalidValueException();
        }

        if (!this.type.getSupportedClassName().equals(value.getClass().getName())) {
            throw new InvalidValueException();
        }

        return operator.exec(value, argument);
    }
}
