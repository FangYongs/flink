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

package org.apache.flink.runtime.taskexecutor.slot;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.concurrent.ComponentMainThreadExecutor;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.memory.MemoryManager;
import org.apache.flink.runtime.taskexecutor.SlotReport;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.concurrent.FutureUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Task table with shared slots implementation of {@link TaskSlotTable}. */
public class TaskShareSlotTable<T extends TaskSlotPayload> implements TaskSlotTable<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskShareSlotTable.class);

    /** The table state. */
    private volatile State state;

    /** The closing future is completed when all slot are freed and state is closed. */
    private final CompletableFuture<Void> closingFuture;

    /** The shared pool memory manager. */
    private final MemoryManager memoryManager;

    /** All the tasks in the table grouped by job id. */
    private final Map<JobID, Map<ExecutionAttemptID, T>> taskMappingPerJob;

    /** All the tasks in the table. */
    private final Map<ExecutionAttemptID, T> tasks;

    /** {@link ComponentMainThreadExecutor} to schedule internal calls to the main thread. */
    private ComponentMainThreadExecutor mainThreadExecutor =
            new ComponentMainThreadExecutor.DummyComponentMainThreadExecutor(
                    "TaskSlotTableImpl is not initialized with proper main thread executor, "
                            + "call to TaskSlotTableImpl#start is required");

    public TaskShareSlotTable(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        this.taskMappingPerJob = new HashMap<>(16);
        this.tasks = new HashMap<>(16);
        this.state = State.CREATED;
        this.closingFuture = new CompletableFuture<>();
    }

    @Override
    public void start(
            SlotActions initialSlotActions, ComponentMainThreadExecutor mainThreadExecutor) {
        Preconditions.checkState(
                state == State.CREATED,
                "The %s has to be just created before starting",
                TaskSlotTableImpl.class.getSimpleName());
        this.mainThreadExecutor = Preconditions.checkNotNull(mainThreadExecutor);

        state = State.RUNNING;
    }

    @VisibleForTesting
    public boolean isClosed() {
        return state == State.CLOSED;
    }

    @Override
    public Set<AllocationID> getAllocationIdsPerJob(JobID jobId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<AllocationID> getActiveTaskSlotAllocationIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<AllocationID> getActiveTaskSlotAllocationIdsPerJob(JobID jobId) {
        return Collections.emptySet();
    }

    // ---------------------------------------------------------------------
    // Slot report methods
    // ---------------------------------------------------------------------

    @Override
    public SlotReport createSlotReport(ResourceID resourceId) {
        throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------------------------
    // Slot methods
    // ---------------------------------------------------------------------

    @VisibleForTesting
    @Override
    public boolean allocateSlot(
            int index, JobID jobId, AllocationID allocationId, Time slotTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean allocateSlot(
            int requestedIndex,
            JobID jobId,
            AllocationID allocationId,
            ResourceProfile resourceProfile,
            Time slotTimeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSlotActive(AllocationID allocationId) throws SlotNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSlotInactive(AllocationID allocationId, Time slotTimeout)
            throws SlotNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int freeSlot(AllocationID allocationId, Throwable cause) throws SlotNotFoundException {
        return -1;
    }

    @Override
    public boolean isValidTimeout(AllocationID allocationId, UUID ticket) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAllocated(int index, JobID jobId, AllocationID allocationId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryMarkSlotActive(JobID jobId, AllocationID allocationId) {
        return true;
    }

    @Override
    public boolean isSlotFree(int index) {
        return true;
    }

    @Override
    public boolean hasAllocatedSlots(JobID jobId) {
        return false;
    }

    @Override
    public Iterator<TaskSlot<T>> getAllocatedSlots(JobID jobId) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    public JobID getOwningJob(AllocationID allocationId) {
        throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------------------------
    // Task methods
    // ---------------------------------------------------------------------

    @Override
    public boolean addTask(T task) throws SlotNotFoundException, SlotNotActiveException {
        checkRunning();
        Preconditions.checkNotNull(task);

        if (!tasks.containsKey(task.getExecutionId())) {
            Map<ExecutionAttemptID, T> taskMappings =
                    taskMappingPerJob.computeIfAbsent(task.getJobID(), key -> new HashMap<>());
            taskMappings.put(task.getExecutionId(), task);
            tasks.put(task.getExecutionId(), task);

            return true;
        } else {
            LOG.error(
                    "There are multiple tasks with {} for job {}",
                    task.getExecutionId(),
                    task.getJobID());
            return false;
        }
    }

    @Override
    public T removeTask(ExecutionAttemptID executionAttemptID) {
        checkStarted();

        T task = tasks.remove(executionAttemptID);
        if (task != null) {
            Map<ExecutionAttemptID, T> taskMappings = taskMappingPerJob.get(task.getJobID());
            if (taskMappings != null) {
                taskMappings.remove(executionAttemptID);
                if (taskMappings.isEmpty()) {
                    taskMappingPerJob.remove(task.getJobID());
                }
            }

            return task;
        } else {
            return null;
        }
    }

    @Override
    public T getTask(ExecutionAttemptID executionAttemptID) {
        return tasks.get(executionAttemptID);
    }

    @Override
    public Iterator<T> getTasks(JobID jobId) {
        Map<ExecutionAttemptID, T> tasks = taskMappingPerJob.get(jobId);
        if (tasks == null) {
            return Collections.emptyIterator();
        } else {
            return tasks.values().iterator();
        }
    }

    @Override
    public AllocationID getCurrentAllocation(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryManager getTaskMemoryManager(AllocationID allocationID)
            throws SlotNotFoundException {
        return memoryManager;
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        if (state == State.CREATED || state == State.RUNNING) {
            state = State.CLOSED;
            closingFuture.complete(null);
        }
        return closingFuture;
    }

    // ---------------------------------------------------------------------
    // TimeoutListener methods
    // ---------------------------------------------------------------------

    @Override
    public void notifyTimeout(AllocationID key, UUID ticket) {
        throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------------------------
    // Internal methods
    // ---------------------------------------------------------------------

    @Nullable
    private TaskSlot<T> getTaskSlot(AllocationID allocationId) {
        throw new UnsupportedOperationException();
    }

    private void checkRunning() {
        Preconditions.checkState(
                state == State.RUNNING,
                "The %s has to be running.",
                TaskShareSlotTable.class.getSimpleName());
    }

    private void checkStarted() {
        Preconditions.checkState(
                state != State.CREATED,
                "The %s has to be started (not created).",
                TaskShareSlotTable.class.getSimpleName());
    }

    // ---------------------------------------------------------------------
    // Static utility classes
    // ---------------------------------------------------------------------

    /** The state of the share slot table. */
    private enum State {
        CREATED,
        RUNNING,
        CLOSED
    }
}
