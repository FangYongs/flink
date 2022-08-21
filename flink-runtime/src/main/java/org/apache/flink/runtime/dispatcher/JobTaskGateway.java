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

package org.apache.flink.runtime.dispatcher;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.executiongraph.ExecutionJobVertex;
import org.apache.flink.runtime.io.network.partition.ResultPartitionID;
import org.apache.flink.runtime.jobgraph.IntermediateDataSetID;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.jobmaster.SerializedInputSplit;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.operators.coordination.CoordinationRequest;
import org.apache.flink.runtime.operators.coordination.CoordinationResponse;
import org.apache.flink.runtime.operators.coordination.OperatorEvent;
import org.apache.flink.runtime.taskexecutor.slot.SlotOffer;
import org.apache.flink.runtime.taskmanager.TaskExecutionState;
import org.apache.flink.util.FlinkException;
import org.apache.flink.util.SerializedValue;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Gateway for task connect to job master.
 */
public interface JobTaskGateway {

    /**
     * Updates the task execution state for a given task.
     *
     * @param jobId The given job id
     * @param taskExecutionState New task execution state for a given task
     * @return Future flag of the task execution state update result
     */
    CompletableFuture<Acknowledge> updateTaskExecutionState(
            final JobID jobId, final TaskExecutionState taskExecutionState);

    /**
     * Requests the next input split for the {@link ExecutionJobVertex}. The next input split is
     * sent back to the sender as a {@link SerializedInputSplit} message.
     *
     * @param vertexID The job vertex id
     * @param executionAttempt The execution attempt id
     * @return The future of the input split. If there is no further input split, will return an
     *     empty object.
     */
    CompletableFuture<SerializedInputSplit> requestNextInputSplit(
            final JobID jobId, final JobVertexID vertexID, final ExecutionAttemptID executionAttempt);

    /**
     * Requests the current state of the partition. The state of a partition is currently bound to
     * the state of the producing execution.
     *
     * @param intermediateResultId The execution attempt ID of the task requesting the partition
     *     state.
     * @param partitionId The partition ID of the partition to request the state of.
     * @return The future of the partition state
     */
    CompletableFuture<ExecutionState> requestPartitionState(
            final JobID jobId, final IntermediateDataSetID intermediateResultId, final ResultPartitionID partitionId);

    /**
     * Update the aggregate and return the new value.
     *
     * @param aggregateName The name of the aggregate to update
     * @param aggregand The value to add to the aggregate
     * @param serializedAggregationFunction The function to apply to the current aggregate and
     *     aggregand to obtain the new aggregate value, this should be of type {@link
     *     AggregateFunction}
     * @return The updated aggregate
     */
    CompletableFuture<Object> updateGlobalAggregate(
            final JobID jobId, String aggregateName, Object aggregand, byte[] serializedAggregationFunction);

    /**
     * Send operator event from task to coordinator.
     *
     * @param jobId The given job id
     * @param task The task to send the event
     * @param operatorID The operator id of the task
     * @param event The event to be sent
     * @return Future flag of the event which is sent to coordinator
     */
    CompletableFuture<Acknowledge> sendOperatorEventToCoordinator(
            JobID jobId, ExecutionAttemptID task, OperatorID operatorID, SerializedValue<OperatorEvent> event);

    /**
     * Send operator request to coordinator.
     *
     * @param jobId The given job id
     * @param operatorID The operator id of the task
     * @param request The request to be sent
     * @return Future flag of the request which is sent to coordinator
     */
    CompletableFuture<CoordinationResponse> sendRequestToCoordinator(
            JobID jobId, OperatorID operatorID, SerializedValue<CoordinationRequest> request);

    /**
     * Get the address of job master.
     *
     * @return The address
     */
    String getAddress();

    /**
     * Get the job master gateway from job task gateway.
     *
     * @return The job master gateway
     */
    JobMasterGateway getJobMasterGateway();
}
