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

import org.apache.flink.runtime.resourcemanager.TaskExecutorRegistration;
import org.apache.flink.runtime.taskexecutor.TaskExecutorGateway;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;

import java.net.UnknownHostException;

/** Contain task manager information in Dispatcher, such as task manager address and gateway. */
public class ResolvedTaskManager {
    private final TaskExecutorRegistration taskExecutorRegistration;
    private final TaskManagerLocation taskManagerLocation;
    private final TaskExecutorGateway taskExecutorGateway;

    public ResolvedTaskManager(
            boolean retrieveTaskManagerHostName,
            TaskExecutorRegistration taskExecutorRegistration,
            TaskExecutorGateway taskExecutorGateway) {
        this.taskExecutorRegistration = taskExecutorRegistration;
        this.taskExecutorGateway = taskExecutorGateway;

        try {
            this.taskManagerLocation =
                    TaskManagerLocation.fromUnresolvedLocation(
                            taskExecutorRegistration.getUnresolvedTaskManagerLocation(),
                            retrieveTaskManagerHostName
                                    ? TaskManagerLocation.ResolutionMode.RETRIEVE_HOST_NAME
                                    : TaskManagerLocation.ResolutionMode.USE_IP_ONLY);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {}

    public TaskExecutorRegistration getTaskExecutorRegistration() {
        return taskExecutorRegistration;
    }

    public TaskExecutorGateway getTaskExecutorGateway() {
        return taskExecutorGateway;
    }

    public TaskManagerLocation getTaskManagerLocation() {
        return taskManagerLocation;
    }

    public void close() {}
}
