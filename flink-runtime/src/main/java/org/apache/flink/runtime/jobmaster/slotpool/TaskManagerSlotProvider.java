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

package org.apache.flink.runtime.jobmaster.slotpool;

import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.dispatcher.ResolvedTaskManager;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.runtime.jobmaster.RpcTaskManagerGateway;
import org.apache.flink.runtime.jobmaster.SlotContext;
import org.apache.flink.runtime.jobmaster.SlotRequestId;

import java.util.concurrent.CompletableFuture;

public class TaskManagerSlotProvider implements PhysicalSlotProvider {
    @Override
    public CompletableFuture<PhysicalSlotRequest.Result> allocatePhysicalSlot(
            PhysicalSlotRequest physicalSlotRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SlotContext allocatePhysicalSlot(
            ResolvedTaskManager resolvedTaskManager, JobMasterId jobMasterId) {
        //        AllocationID allocationId,
        //        TaskManagerLocation location,
        //        int physicalSlotNumber,
        //        ResourceProfile resourceProfile,
        //        TaskManagerGateway taskManagerGateway
        return new AllocatedSlot(
                new AllocationID(),
                resolvedTaskManager.getTaskManagerLocation(),
                0,
                ResourceProfile.UNKNOWN,
                new RpcTaskManagerGateway(
                        resolvedTaskManager.getTaskExecutorGateway(), jobMasterId));
    }

    @Override
    public void cancelSlotRequest(SlotRequestId slotRequestId, Throwable cause) {}

    @Override
    public void disableBatchSlotRequestTimeoutCheck() {}
}
