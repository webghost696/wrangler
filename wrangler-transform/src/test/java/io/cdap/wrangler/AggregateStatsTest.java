/*
 * Copyright © 2017-2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.cdap.wrangler;

import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.DirectiveParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AggregateStatsTest {
    private AggregateStats aggregateStats;

    @Before
    public void setUp() {
        aggregateStats = new AggregateStats();
        try {
            aggregateStats.initialize(new Arguments() {
                @Override
                public <T extends Token> T value(String name) {
                    @SuppressWarnings("unchecked")
                    T token = (T) mock(Token.class);
                    when(token.value()).thenReturn(name); // Stub value() to return the name
                    return token;
                }

                @Override
                public int size() {
                    return 4;
                }

                @Override
                public boolean contains(String name) {
                    return true;
                }

                @Override
                public TokenType type(String name) {
                    return TokenType.IDENTIFIER;
                }

                @Override
                public int line() {
                    return 1;
                }

                @Override
                public int column() {
                    return 1;
                }

                @Override
                public String source() {
                    return "aggregate-stats :data_size :response_time total_size_mb total_time_sec";
                }

                @Override
                public JsonElement toJson() {
                    JsonObject json = new JsonObject();
                    json.addProperty("inputSizeColumn", ":data_size");
                    json.addProperty("inputTimeColumn", ":response_time");
                    json.addProperty("outputSizeColumn", "total_size_mb");
                    json.addProperty("outputTimeColumn", "total_time_sec");
                    return json;
                }
            });
        } catch (DirectiveParseException e) {
            fail("Initialization failed with DirectiveParseException: " + e.getMessage());
        }
    }

    @Test
    public void testAggregateStats() {
        List<Row> inputRows = new ArrayList<>();
        Row row1 = new Row();
        row1.add(":data_size", new ByteSize("10KB"));
        row1.add(":response_time", new TimeDuration("150ms"));
        Row row2 = new Row();
        row2.add(":data_size", new ByteSize("1MB"));
        row2.add(":response_time", new TimeDuration("2s"));
        inputRows.add(row1);
        inputRows.add(row2);

        List<Row> result;
        try {
            result = aggregateStats.execute(inputRows, mock(ExecutorContext.class));
        } catch (DirectiveExecutionException | ErrorRowException | ReportErrorAndProceed e) {
            fail("Unexpected exception: " + e.getMessage());
            return;
        }

        assertEquals(1, result.size());
        Row resultRow = result.get(0);
        double totalSizeMB = (double) resultRow.getValue("total_size_mb");
        double totalTimeSec = (double) resultRow.getValue("total_time_sec");

        assertEquals(1.0097656, totalSizeMB, 0.0001); // (10KB + 1MB) / (1024 * 1024) ≈ 1.0097656 MB
        assertEquals(2.15, totalTimeSec, 0.001);      // 150ms + 2s = 2.15s
    }

    @Test(expected = DirectiveExecutionException.class)
    public void testInvalidByteSize() throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {
        List<Row> inputRows = new ArrayList<>();
        Row row = new Row();
        row.add(":data_size", "invalid"); // Invalid ByteSize
        row.add(":response_time", new TimeDuration("1s"));
        inputRows.add(row);
        aggregateStats.execute(inputRows, mock(ExecutorContext.class));
    }
}