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

package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.deployment.TaskDeploymentDescriptor;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.taskexecutor.slot.TaskSlotTable;
import org.apache.flink.runtime.taskmanager.Task;
import org.apache.flink.runtime.taskmanager.TaskExecutionState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Manage the deployed olap job and tasks. */
public class JobDeploymentManager {
    // The task execution attempt ids for the job.
    private final Set<ExecutionAttemptID> executionAttemptIds;
    // The terminated tasks for the job.
    private final Set<ExecutionAttemptID> terminatedTasks;
    // The job master id of the job.
    private final JobMasterId jobMasterId;
    private final JobID jobId;
    // The task slot table which is used by given job.
    private final TaskSlotTable<Task> taskSlotTable;
    private final boolean useShareSlotTable;
    private final long receiveTime;
    private long startDeployTime;

    public JobDeploymentManager(
            JobID jobId,
            JobMasterId jobMasterId,
            TaskSlotTable<Task> taskSlotTable,
            boolean useShareSlotTable) {
        this.executionAttemptIds = new HashSet<>();
        this.terminatedTasks = new HashSet<>();
        this.jobId = jobId;
        this.jobMasterId = jobMasterId;
        this.taskSlotTable = taskSlotTable;
        this.useShareSlotTable = useShareSlotTable;
        this.receiveTime = System.currentTimeMillis();
    }

    /**
     * Add new task execution attempt id to the executionAttemptIds.
     *
     * @param executionAttemptId the added task execution attempt id
     */
    public boolean addExecutionAttemptId(ExecutionAttemptID executionAttemptId) {
        return executionAttemptIds.add(executionAttemptId);
    }

    private boolean isJobFinished() {
        return terminatedTasks.size() == executionAttemptIds.size() &&
                terminatedTasks.equals(executionAttemptIds);
    }
    public boolean finishTask(ExecutionAttemptID executionAttemptId) {
        terminatedTasks.add(executionAttemptId);
        return isJobFinished();
    }

    public JobMasterId getJobMasterId() {
        return jobMasterId;
    }

    public TaskSlotTable<Task> getTaskSlotTable() {
        return taskSlotTable;
    }

    public boolean isUseShareSlotTable() {
        return useShareSlotTable;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public long getStartDeployTime() {
        return startDeployTime;
    }

    public void setStartDeployTime(long startDeployTime) {
        this.startDeployTime = startDeployTime;
    }
}
