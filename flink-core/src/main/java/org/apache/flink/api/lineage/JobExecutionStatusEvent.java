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
import org.apache.flink.api.common.JobStatus;

import javax.annotation.Nullable;

/**
 * Event for job status changed in job master, there will be exception information in the event when
 * job fails.
 */
@PublicEvolving
public interface JobExecutionStatusEvent extends JobStatusChangedEvent {
    /**
     * Old status of the current job.
     *
     * @return job old status
     */
    JobStatus oldStatus();

    /**
     * New status of the current job.
     *
     * @return job new status
     */
    JobStatus newStatus();

    /**
     * The exception thrown by the current job when it fails, otherwise the exception will be null.
     *
     * @return the thrown exception
     */
    @Nullable
    Throwable exception();
}
