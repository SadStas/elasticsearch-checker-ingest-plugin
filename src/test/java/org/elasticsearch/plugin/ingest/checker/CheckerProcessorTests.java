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

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.plugin.ingest.checker.operator.CheckOperator;
import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

public class CheckerProcessorTests extends ESTestCase {

    public void testThatProcessorWorks() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source", "test");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        CheckerProcessor processor = new CheckerProcessor(
                randomAlphaOfLength(10),
                "source",
                "result",
                new CheckOperator("string","equal", "test")
        );

        processor.execute(ingestDocument);
        Map<String, Object> data = ingestDocument.getSourceAndMetadata();

        assertThat(data, hasKey("source"));
        assertThat(data.get("source"), is("test"));

        assertThat(data, hasKey("result"));
        assertThat(data.get("result"), is(true));
    }
}

