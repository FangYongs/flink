/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.lineage;

import org.apache.flink.api.lineage.JobStatusChangedListener;
import org.apache.flink.api.lineage.JobStatusChangedListenerFactory;

import java.util.Map;

import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * Datahub demo.
 */
public class DatahubLineageListenerFactory implements JobStatusChangedListenerFactory {
    private static final String DATAHUB_REST_ADDRESS = "datahub.rest.url";

    @Override
    public JobStatusChangedListener createListener(Context context) {
        Map<String, String> config = context.getConfiguration().toMap();
        String url = checkNotNull(config.get(DATAHUB_REST_ADDRESS));
        return new DatahubLineageListener(url);
    }
}
