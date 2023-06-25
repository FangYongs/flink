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
import org.apache.flink.api.common.JobType;
import org.apache.flink.configuration.Configuration;

/** Event for job is created but is not submitted to cluster. */
@PublicEvolving
public interface JobCreatedEvent extends JobStatusChangedEvent {
    /**
     * Lineage from sources to sinks for the current job.
     *
     * @return the job lineage information
     */
    JobLineage lineage();

    /**
     * Job type for the current job.
     *
     * @return the job type
     */
    JobType jobType();

    /**
     * Configuration for the job which provides all options such as resource config, state backend
     * type and user customized config in job.
     *
     * @return job configuration
     */
    Configuration configuration();
}
