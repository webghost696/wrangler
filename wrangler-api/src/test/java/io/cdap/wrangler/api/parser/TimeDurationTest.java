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


package io.cdap.wrangler.api.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeDurationTest {
    @Test
    public void testTimeDurationParsing() {
        TimeDuration td1 = new TimeDuration("150ms");
        assertEquals(0.150, td1.getSeconds(), 0.001);

        TimeDuration td2 = new TimeDuration("2s");
        assertEquals(2.0, td2.getSeconds(), 0.001);

        TimeDuration td3 = new TimeDuration("500ms");
        assertEquals(0.500, td3.getSeconds(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("invalid");
    }
}