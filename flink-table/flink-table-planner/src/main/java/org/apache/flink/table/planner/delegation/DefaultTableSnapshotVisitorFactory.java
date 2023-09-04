package org.apache.flink.table.planner.delegation;

import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.delegation.TableSnapshotVisitorFactory;
import org.apache.flink.table.operations.QueryOperation;
import org.apache.flink.table.operations.QueryOperationVisitor;

import java.util.Map;
import java.util.Set;

public class DefaultTableSnapshotVisitorFactory implements TableSnapshotVisitorFactory {
    @Override
    public QueryOperationVisitor<Set<ObjectIdentifier>> createTableVisitor() {
        return new DefaultCollectTableVisitor();
    }

    @Override
    public QueryOperationVisitor<QueryOperation> createSnapshotVisitor(Map<ObjectIdentifier, Long> tableSnapshots) {
        return new DefaultTableSnapshotVisitor(tableSnapshots);
    }

    @Override
    public String factoryIdentifier() {
        return TableSnapshotVisitorFactory.DEFAULT_IDENTIFIER;
    }
}
