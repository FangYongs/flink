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

import org.apache.flink.runtime.dispatcher.DispatcherGateway;
import org.apache.flink.runtime.dispatcher.DispatcherId;
import org.apache.flink.runtime.registration.RegisteredRpcConnection;
import org.apache.flink.runtime.registration.RegistrationResponse;
import org.apache.flink.runtime.registration.RetryingRegistration;
import org.apache.flink.runtime.registration.RetryingRegistrationConfiguration;
import org.apache.flink.runtime.rpc.RpcService;

import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** The connection between a TaskExecutor and the Dispatcher. */
public class TaskExecutorToDispatcherConnection
        extends RegisteredRpcConnection<
                DispatcherId,
                DispatcherGateway,
                RegistrationResponse.Success,
                TaskExecutorRegistrationRejection> {

    private final RpcService rpcService;

    private final RetryingRegistrationConfiguration retryingRegistrationConfiguration;

    public TaskExecutorToDispatcherConnection(
            Logger log,
            RpcService rpcService,
            RetryingRegistrationConfiguration retryingRegistrationConfiguration,
            String targetAddress,
            DispatcherId fencingToken,
            Executor executor) {
        super(log, targetAddress, fencingToken, executor);
        this.rpcService = rpcService;
        this.retryingRegistrationConfiguration = retryingRegistrationConfiguration;
    }

    @Override
    protected RetryingRegistration<
                DispatcherId,
                DispatcherGateway,
                RegistrationResponse.Success,
                TaskExecutorRegistrationRejection>
            generateRegistration() {
        return new TaskExecutorToDispatcherConnection.DispatcherRegistration(
                log,
                rpcService,
                getTargetAddress(),
                getTargetLeaderId(),
                retryingRegistrationConfiguration);
    }

    @Override
    protected void onRegistrationSuccess(RegistrationResponse.Success success) {

    }

    @Override
    protected void onRegistrationRejection(TaskExecutorRegistrationRejection rejection) {

    }

    @Override
    protected void onRegistrationFailure(Throwable failure) {

    }

    // ------------------------------------------------------------------------
    //  Utilities
    // ------------------------------------------------------------------------

    private static class DispatcherRegistration
            extends RetryingRegistration<
            DispatcherId,
            DispatcherGateway,
            RegistrationResponse.Success,
            TaskExecutorRegistrationRejection> {

        DispatcherRegistration(
                Logger log,
                RpcService rpcService,
                String targetAddress,
                DispatcherId dispatcherId,
                RetryingRegistrationConfiguration retryingRegistrationConfiguration) {

            super(
                    log,
                    rpcService,
                    "Dispatcher",
                    DispatcherGateway.class,
                    targetAddress,
                    dispatcherId,
                    retryingRegistrationConfiguration);
        }

        @Override
        protected CompletableFuture<RegistrationResponse> invokeRegistration(
                DispatcherGateway dispatcher,
                DispatcherId fencingToken,
                long timeoutMillis)
                throws Exception {
            return CompletableFuture.completedFuture(new RegistrationResponse.Success());
        }
    }
}
