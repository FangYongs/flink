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

package org.apache.flink.table.planner.utils;

import java.util.concurrent.atomic.AtomicInteger;

/** Counter for codegen in each job. */
public class CodeGenCounterManager {
    private static final ThreadLocal<CodeGenCounterManager> THREAD_LOCAL_COUNTER =
            ThreadLocal.withInitial(CodeGenCounterManager::new);

    private final AtomicInteger index = new AtomicInteger(1);

    public void resetIndex() {
        this.index.set(1);
    }

    public int getAndIncrement() {
        return index.getAndIncrement();
    }

    public static CodeGenCounterManager getInstance() {
        return THREAD_LOCAL_COUNTER.get();
    }

    public static void initialize() {
        getInstance().resetIndex();
    }
}
