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
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
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
import org.apache.flink.runtime.taskmanager.TaskExecutionState;
import org.apache.flink.util.SerializedValue;

import java.util.concurrent.CompletableFuture;

/**
 * Delegate of job master gateway for {@link JobTaskGateway}.
 */
public class JobTaskMasterGateway implements JobTaskGateway {
    private JobMasterGateway jobMasterGateway;

    public JobTaskMasterGateway(JobMasterGateway jobMasterGateway) {
        this.jobMasterGateway = jobMasterGateway;
    }

    @Override
    public CompletableFuture<Acknowledge> updateTaskExecutionState(
            JobID jobId,
            TaskExecutionState taskExecutionState) {
        return jobMasterGateway.updateTaskExecutionState(taskExecutionState);
    }

    @Override
    public CompletableFuture<SerializedInputSplit> requestNextInputSplit(
            JobID jobId,
            JobVertexID vertexID,
            ExecutionAttemptID executionAttempt) {
        return jobMasterGateway.requestNextInputSplit(vertexID, executionAttempt);
    }

    @Override
    public CompletableFuture<ExecutionState> requestPartitionState(
            JobID jobId,
            IntermediateDataSetID intermediateResultId,
            ResultPartitionID partitionId) {
        return jobMasterGateway.requestPartitionState(intermediateResultId, partitionId);
    }

    @Override
    public CompletableFuture<Object> updateGlobalAggregate(
            JobID jobId,
            String aggregateName,
            Object aggregand,
            byte[] serializedAggregationFunction) {
        return jobMasterGateway.updateGlobalAggregate(aggregateName, aggregand, serializedAggregationFunction);
    }

    @Override
    public CompletableFuture<Acknowledge> sendOperatorEventToCoordinator(
            JobID jobId,
            ExecutionAttemptID task,
            OperatorID operatorID,
            SerializedValue<OperatorEvent> event) {
        return jobMasterGateway.sendOperatorEventToCoordinator(task, operatorID, event);
    }

    @Override
    public CompletableFuture<CoordinationResponse> sendRequestToCoordinator(
            JobID jobId,
            OperatorID operatorID,
            SerializedValue<CoordinationRequest> request) {
        return jobMasterGateway.sendRequestToCoordinator(operatorID, request);
    }

    @Override
    public String getAddress() {
        return jobMasterGateway.getAddress();
    }

    @Override
    public JobMasterGateway getJobMasterGateway() {
        return jobMasterGateway;
    }
}
