/*
 * Copyright © 2017-2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cdap.wrangler;

import io.cdap.wrangler.api.Executor;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.Arguments;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.wrangler.api.SchemaResolutionContext;

import java.util.ArrayList;
import java.util.List;

public class AggregateStats implements Executor<List<Row>, List<Row>> {
    private String inputSizeColumn;
    private String inputTimeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        inputSizeColumn = ((io.cdap.wrangler.api.parser.Token) args.value("inputSizeColumn")).value().toString();
        inputTimeColumn = ((io.cdap.wrangler.api.parser.Token) args.value("inputTimeColumn")).value().toString();
        outputSizeColumn = ((io.cdap.wrangler.api.parser.Token) args.value("outputSizeColumn")).value().toString();
        outputTimeColumn = ((io.cdap.wrangler.api.parser.Token) args.value("outputTimeColumn")).value().toString();

        if (inputSizeColumn == null || inputTimeColumn == null || outputSizeColumn == null || outputTimeColumn == null) {
            throw new DirectiveParseException("All column parameters are required");
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {
        if (rows == null || rows.isEmpty()) {
            throw new DirectiveExecutionException("Input rows cannot be null or empty");
        }

        double totalSizeMB = 0.0;
        double totalTimeSec = 0.0;

        for (Row row : rows) {
            Object sizeValue = row.getValue(inputSizeColumn);
            if (sizeValue instanceof ByteSize) {
                long bytes = ((ByteSize) sizeValue).getBytes();
                totalSizeMB += bytes / (1024.0 * 1024.0); // Convert bytes to MB
            } else {
                throw new DirectiveExecutionException("Invalid byte size value in column: " + inputSizeColumn);
            }

            Object timeValue = row.getValue(inputTimeColumn);
            if (timeValue instanceof TimeDuration) {
                double seconds = ((TimeDuration) timeValue).getSeconds();
                totalTimeSec += seconds;
            } else {
                throw new DirectiveExecutionException("Invalid time duration value in column: " + inputTimeColumn);
            }
        }

        // Create a result row with aggregated values
        List<Row> result = new ArrayList<>();
        Row resultRow = new Row();
        resultRow.add(outputSizeColumn, totalSizeMB);
        resultRow.add(outputTimeColumn, totalTimeSec);
        result.add(resultRow);

        return result;
    }

    @Override
    public void destroy() {
        // No resources to clean up
    }

    @Override
    public Schema getOutputSchema(SchemaResolutionContext schemaResolutionContext) {
        return null; // Default implementation, schema inferred from data
    }
}