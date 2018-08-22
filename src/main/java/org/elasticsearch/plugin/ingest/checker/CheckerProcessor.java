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

package org.elasticsearch.plugin.ingest.checker;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugin.ingest.checker.operator.CheckOperator;
import org.elasticsearch.plugin.ingest.checker.operator.PrepareOperator;

import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readObject;
import static org.elasticsearch.ingest.ConfigurationUtils.readIntProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.newConfigurationException;

public class CheckerProcessor extends AbstractProcessor {

    static final String TYPE = "checker";

    private final String sourceField;
    private final String resultField;
    private final CheckOperator checkOperator;
    private PrepareOperator prepareOperator = null;

    CheckerProcessor(String tag, String sourceField, String resultField, CheckOperator checkOperator) {
        super(tag);

        this.sourceField = sourceField;
        this.resultField = resultField;
        this.checkOperator = checkOperator;
    }

    CheckerProcessor(String tag, String sourceField, String resultField, CheckOperator checkOperator,
                            PrepareOperator prepareOperator) {
        this(tag, sourceField, resultField, checkOperator);

        this.prepareOperator = prepareOperator;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        try {
            Object content = ingestDocument.getFieldValue(sourceField, Object.class);

            if (prepareOperator != null) {
                content = prepareOperator.exec(content);
            }

            ingestDocument.setFieldValue(resultField, checkOperator.exec(content));
        } catch (IllegalArgumentException exception) {
            // do nothing
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        private String tag;

        private class Fields {
            static final String SOURCE = "source_field";
            static final String RESULT = "result_field";
            static final String CHECK_OPERATOR = "check_operator";
            static final String CHECK_OPERATOR_TYPE = "source_field_type";
            static final String CHECK_ARGUMENT = "check_argument";
            static final String PREPARE_OPERATOR = "prepare_operator";
            static final String PREPARE_ARGUMENT = "prepare_argument";
            static final String PREPARE_ITEM = "prepare_item";
        }

        @Override
        public CheckerProcessor create(Map<String, Processor.Factory> factories,
                                       String tag, Map<String, Object> config) {
            this.tag = tag;

            String sourceField = readStringProperty(TYPE, tag, config, Fields.SOURCE);
            String resultField = readStringProperty(TYPE, tag, config, Fields.RESULT);

            String checkOperatorType = readStringProperty(TYPE, tag, config, Fields.CHECK_OPERATOR_TYPE);
            String checkOperatorName = readStringProperty(TYPE, tag, config, Fields.CHECK_OPERATOR);
            Object checkArgument = readObject(TYPE, tag, config, Fields.CHECK_ARGUMENT);
            CheckOperator checkOperator = createCheckOperator(checkOperatorType, checkOperatorName, checkArgument);

            String prepareOperatorName = readOptionalStringProperty(TYPE, tag, config, Fields.PREPARE_OPERATOR);
            if (prepareOperatorName != null) {
                String prepareArgument = readStringProperty(TYPE, tag, config, Fields.PREPARE_ARGUMENT);
                Integer item = readIntProperty(TYPE, tag, config, Fields.PREPARE_ITEM, 0);

                PrepareOperator prepareOperator = createPrepareOperator(prepareOperatorName, prepareArgument, item);

                return new CheckerProcessor(tag, sourceField, resultField, checkOperator, prepareOperator);
            }

            return new CheckerProcessor(tag, sourceField, resultField, checkOperator);
        }

        private CheckOperator createCheckOperator(String type, String name, Object argument) {
            try {
                return new CheckOperator(type, name, argument);
            } catch (CheckOperator.InvalidNameException exception) {
                throw newConfException(Fields.CHECK_OPERATOR, "unknown operator");
            } catch (CheckOperator.InvalidTypeException exception) {
                throw newConfException(Fields.CHECK_OPERATOR_TYPE, "unsupported type");
            } catch (CheckOperator.InvalidArgumentException exception) {
                throw newConfException(Fields.CHECK_ARGUMENT, "invalid type");
            }
        }

        private PrepareOperator createPrepareOperator(String name, String argument, Integer item) {
            try {
                return new PrepareOperator(name, argument, item);
            } catch (PrepareOperator.InvalidNameException exception) {
                throw newConfException(Fields.PREPARE_OPERATOR, "unknown operator");
            }
        }

        private ElasticsearchException newConfException(String property, String message) {
            return newConfigurationException(TYPE, this.tag, property, message);
        }
    }
}
