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

package org.apache.flink.runtime.scheduler;

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.dispatcher.ResolvedTaskManager;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.jobmanager.scheduler.Locality;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.jobmaster.LogicalSlot;
import org.apache.flink.runtime.jobmaster.SlotRequestId;
import org.apache.flink.runtime.jobmaster.slotpool.JobTaskManagerCounter;
import org.apache.flink.runtime.jobmaster.slotpool.PhysicalSlotProvider;
import org.apache.flink.runtime.jobmaster.slotpool.SingleLogicalSlot;
import org.apache.flink.runtime.jobmaster.slotpool.TaskManagerSelectionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Allocates {@link LogicalSlot}s from local task managers. The allocator maintains task manager
 * list and job can allocate slots from the given list directly.
 */
public class LocalExecutionSlotAllocator implements ExecutionSlotAllocator {
    private final PhysicalSlotProvider physicalSlotProvider;
    private final Map<ResourceID, ResolvedTaskManager> taskManagers;
    private final TaskManagerSelectionStrategy taskManagerSelectionStrategy;
    private final JobTaskManagerCounter jobTaskManagerCounter;
    private final JobMasterId jobMasterId;
    private final Set<ResourceID> allocatedResources;

    public LocalExecutionSlotAllocator(
            PhysicalSlotProvider physicalSlotProvider,
            Map<ResourceID, ResolvedTaskManager> taskManagers,
            TaskManagerSelectionStrategy taskManagerSelectionStrategy,
            JobTaskManagerCounter jobTaskManagerCounter,
            JobMasterId jobMasterId) {
        this.physicalSlotProvider = physicalSlotProvider;
        this.taskManagers = taskManagers;
        this.taskManagerSelectionStrategy = taskManagerSelectionStrategy;
        this.jobTaskManagerCounter = jobTaskManagerCounter;
        this.jobMasterId = jobMasterId;
        this.allocatedResources = new HashSet<>();
    }

    @Override
    public List<ExecutionSlotAssignment> allocateSlotsFor(
            List<ExecutionAttemptID> executionAttemptIds) {
        if (executionAttemptIds.isEmpty()) {
            return new ArrayList<>();
        }

        int taskManagerCount =
                jobTaskManagerCounter.computeTaskManagerCount(executionAttemptIds.size());
        if (taskManagerCount > allocatedResources.size()) {
            int allocateTaskManagerCount = taskManagerCount - allocatedResources.size();
            Set<ResourceID> allocateResourceIds =
                    taskManagerSelectionStrategy.selectTaskManagers(
                            allocateTaskManagerCount, taskManagers);
            allocatedResources.addAll(allocateResourceIds);
        }

        List<ResourceID> useResourceIdList = new ArrayList<>();
        if (taskManagerCount >= allocatedResources.size()) {
            useResourceIdList.addAll(allocatedResources);
        } else {
            List<ResourceID> resourceIdList = new ArrayList<>(allocatedResources);
            Collections.shuffle(resourceIdList);
            for (int i = 0; i < taskManagerCount; i++) {
                useResourceIdList.add(resourceIdList.get(i));
            }
        }

        List<ExecutionSlotAssignment> executionSlotAssignments =
                new ArrayList<>(executionAttemptIds.size());
        for (int i = 0; i < executionAttemptIds.size(); i++) {
            ExecutionAttemptID executionAttemptID = executionAttemptIds.get(i);
            ResourceID resourceId = useResourceIdList.get(i % useResourceIdList.size());
            ResolvedTaskManager resolvedTaskManager = taskManagers.get(resourceId);
            executionSlotAssignments.add(
                    new ExecutionSlotAssignment(
                            executionAttemptID,
                            CompletableFuture.completedFuture(
                                    new SingleLogicalSlot(
                                            new SlotRequestId(),
                                            physicalSlotProvider.allocatePhysicalSlot(
                                                    resolvedTaskManager, jobMasterId),
                                            Locality.UNKNOWN,
                                            null))));
        }

        return executionSlotAssignments;
    }

    @Override
    public void cancel(ExecutionAttemptID executionAttemptId) {}
}
