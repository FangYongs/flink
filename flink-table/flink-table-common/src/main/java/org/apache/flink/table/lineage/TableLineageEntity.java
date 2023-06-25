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

package org.apache.flink.table.lineage;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.lineage.LineageEntity;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.CatalogContext;
import org.apache.flink.table.catalog.ObjectIdentifier;

/** Table lineage entity for table connector. */
@PublicEvolving
public interface TableLineageEntity extends LineageEntity {
    /* The catalog context of the table lineage entity. */
    CatalogContext catalogContext();

    /* The table of the table lineage entity. */
    CatalogBaseTable table();

    /**
     * Identifier for the table in table lineage entity.
     */
    ObjectIdentifier identifier();
}
