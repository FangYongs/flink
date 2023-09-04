package org.apache.flink.table.delegation;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.factories.Factory;
import org.apache.flink.table.operations.QueryOperation;
import org.apache.flink.table.operations.QueryOperationVisitor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/** Factory to create query operation table visitor. */
public interface TableSnapshotVisitorFactory extends Factory {
    /** {@link #factoryIdentifier()} for the default {@link QueryOperationVisitor}. */
    String DEFAULT_IDENTIFIER = "default";

    QueryOperationVisitor<Set<ObjectIdentifier>> createTableVisitor();

    QueryOperationVisitor<QueryOperation> createSnapshotVisitor(Map<ObjectIdentifier, Long> tableSnapshots);

    @Override
    default Set<ConfigOption<?>> requiredOptions() {
        return Collections.emptySet();
    }

    @Override
    default Set<ConfigOption<?>> optionalOptions() {
        return Collections.emptySet();
    }
}
