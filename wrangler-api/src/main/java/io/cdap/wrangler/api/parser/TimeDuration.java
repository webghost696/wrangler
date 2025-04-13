/*
 * Copyright © 2025 The Apache Software Foundation
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
 */
package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeDuration implements Token {
    private final String rawValue; // e.g., "150ms"
    private final long nanoseconds; // Value in nanoseconds

    public TimeDuration(String value) {
        this.rawValue = value;
        this.nanoseconds = parseToNanoseconds(value);
    }

    private long parseToNanoseconds(String value) {
        Pattern pattern = Pattern.compile("^(\\d*\\.?\\d*)\\s*([a-zA-Z]+)$");
        Matcher matcher = pattern.matcher(value.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + value);
        }

        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        switch (unit) {
            case "NS":
                return (long) number;
            case "MS":
                return (long) (number * 1_000_000);
            case "S":
                return (long) (number * 1_000_000_000);
            case "M":
            case "MIN":
                return (long) (number * 60 * 1_000_000_000);
            case "H":
                return (long) (number * 3600 * 1_000_000_000);
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public double getSeconds() {
        return nanoseconds / 1_000_000_000.0;
    }

    public String value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "TIME_DURATION");
        json.addProperty("value", rawValue);
        json.addProperty("nanoseconds", nanoseconds);
        return json;
    }
}