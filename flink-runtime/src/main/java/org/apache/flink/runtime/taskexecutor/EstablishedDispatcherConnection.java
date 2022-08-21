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

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.dispatcher.DispatcherGateway;
import org.apache.flink.runtime.dispatcher.DispatcherId;

import javax.annotation.Nonnull;

/** Container for the dispatcher connection instances used by the {@link TaskExecutor}. */
public class EstablishedDispatcherConnection {
    @Nonnull
    private final DispatcherGateway dispatcherGateway;

    @Nonnull private final ResourceID dispatcherResourceId;

    @Nonnull private final DispatcherId dispatcherId;

    public EstablishedDispatcherConnection(
            @Nonnull DispatcherGateway dispatcherGateway,
            @Nonnull ResourceID dispatcherResourceId,
            @Nonnull DispatcherId dispatcherId) {
        this.dispatcherGateway = dispatcherGateway;
        this.dispatcherResourceId = dispatcherResourceId;
        this.dispatcherId = dispatcherId;
    }

    @Nonnull
    public DispatcherGateway getDispatcherGateway() {
        return dispatcherGateway;
    }

    @Nonnull
    public ResourceID getDispatcherResourceId() {
        return dispatcherResourceId;
    }

    @Nonnull
    public DispatcherId getDispatcherId() {
        return dispatcherId;
    }
}
