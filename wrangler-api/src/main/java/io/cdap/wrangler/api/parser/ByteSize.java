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

public class ByteSize implements Token {
    private final String rawValue; // e.g., "10KB"
    private final long bytes; // Value in bytes

    public ByteSize(String value) {
        this.rawValue = value;
        this.bytes = parseToBytes(value);
    }

    private long parseToBytes(String value) {
        Pattern pattern = Pattern.compile("^(\\d*\\.?\\d*)\\s*([a-zA-Z]+)$");
        Matcher matcher = pattern.matcher(value.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + value);
        }

        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        switch (unit) {
            case "B":
                return (long) number;
            case "KB":
                return (long) (number * 1024);
            case "MB":
                return (long) (number * 1024 * 1024);
            case "GB":
                return (long) (number * 1024 * 1024 * 1024);
            case "TB":
                return (long) (number * 1024 * 1024 * 1024 * 1024);
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
    }

    public String value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "BYTE_SIZE");
        json.addProperty("value", rawValue);
        json.addProperty("bytes", bytes);
        return json;
    }
}