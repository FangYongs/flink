/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.jobmaster.slotpool;

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.dispatcher.ResolvedTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.flink.util.Preconditions.checkArgument;

/** Select random task managers from given task manager list. */
public class RandomTaskManagerSelectionStrategy implements TaskManagerSelectionStrategy {
    @Override
    public Set<ResourceID> selectTaskManagers(
            int taskManagerCount, Map<ResourceID, ResolvedTaskManager> resolvedTaskManagers) {
        checkArgument(taskManagerCount > 0 && !resolvedTaskManagers.isEmpty());
        if (resolvedTaskManagers.size() <= taskManagerCount) {
            return resolvedTaskManagers.keySet();
        }

        List<ResourceID> resourceIdList = new ArrayList<>(resolvedTaskManagers.keySet());
        Collections.shuffle(resourceIdList);
        Set<ResourceID> resultResourceIds = new HashSet<>(taskManagerCount);
        for (int i = 0; i < taskManagerCount; i++) {
            resultResourceIds.add(resourceIdList.get(i));
        }

        return resultResourceIds;
    }
}
