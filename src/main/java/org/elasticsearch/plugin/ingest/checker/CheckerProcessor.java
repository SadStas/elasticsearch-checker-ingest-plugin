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

import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readMap;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalMap;
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
    public void execute(IngestDocument ingestDocument) throws Exception {
        String content = ingestDocument.getFieldValue(sourceField, String.class);

        if (prepareOperator != null) {
            content = String.valueOf(prepareOperator.exec(content));
        }

        ingestDocument.setFieldValue(resultField, checkOperator.exec(content));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        private String tag;

        @Override
        public CheckerProcessor create(Map<String, Processor.Factory> factories,
                                       String tag, Map<String, Object> config) {
            this.tag = tag;

            String sourceField = readStringProperty(TYPE, tag, config, "source_field");
            String resultField = readStringProperty(TYPE, tag, config, "result_field");
            String sourceFieldType = readStringProperty(TYPE, tag, config, "source_field_type");

            Map<String, Object> check = readMap(TYPE, tag, config, "check");
            CheckOperator checkOperator = this.createCheckOperator(check, sourceFieldType);

            Map<String, Object> prepare = readOptionalMap(TYPE, tag, config, "prepare");
            if (prepare != null) {
                return new CheckerProcessor(tag, sourceField, resultField, checkOperator, createPrepareConfig(prepare));
            }

            return new CheckerProcessor(tag, sourceField, resultField, checkOperator);
        }

        private CheckOperator createCheckOperator(Map<String, Object> check, String typeName) {
            if (!check.containsKey("operator")) {
                throw newConfException("check.operator");
            }
            String name = String.valueOf(check.get("operator"));

            if (!check.containsKey("argument")) {
                throw newConfException("check.argument");
            }
            Object argument = check.get("argument");

            try {
                return new CheckOperator(typeName, name, argument);
            } catch (CheckOperator.InvalidNameException exception) {
                throw newConfException("check.operator", "unknown operator");
            } catch (CheckOperator.InvalidTypeException exception) {
                throw newConfException("source_field_type", "unknown type");
            }
        }

        private PrepareOperator createPrepareConfig(Map<String, Object> prepare) {
            if (!prepare.containsKey("operator")) {
                throw newConfException("prepare.operator");
            }
            String name = String.valueOf(prepare.get("operator"));

            if (!(prepare.get("argument") instanceof String)) {
                throw newConfException("prepare.argument", "required property is missing or isn`t a string");
            }
            String argument = prepare.get("argument").toString();

            if (!(prepare.getOrDefault("item", 0) instanceof Integer)) {
                throw newConfException("prepare.item", "isn`t an integer");
            }
            Integer item = Integer.valueOf(prepare.getOrDefault("item", 0).toString());

            return new PrepareOperator(name, argument, item);
        }

        private ElasticsearchException newConfException(String property) {
            return this.newConfException(property, "required property is missing");
        }

        private ElasticsearchException newConfException(String property, String message) {
            return newConfigurationException(TYPE, this.tag, property, message);
        }
    }
}
