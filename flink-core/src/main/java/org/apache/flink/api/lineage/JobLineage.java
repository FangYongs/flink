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

package org.apache.flink.api.lineage;

import org.apache.flink.annotation.PublicEvolving;

import java.util.List;

/** Lineage from sources to sinks in the job. */
@PublicEvolving
public interface JobLineage {
    /**
     * Source lineage entity list for the job.
     *
     * @return source lineage entity list
     */
    List<LineageEntity> sources();

    /**
     * Sink lineage entity list for the job.
     *
     * @return sink lineage entity list
     */
    List<LineageEntity> sinks();

    /**
     * Lineage relations from sources to sinks for the job.
     *
     * @return lineage relation list
     */
    List<LineageRelation> relations();
}
