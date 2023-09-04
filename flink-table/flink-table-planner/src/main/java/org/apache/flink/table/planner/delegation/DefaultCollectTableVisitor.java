package org.apache.flink.table.planner.delegation;

import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.operations.QueryOperation;
import org.apache.flink.table.operations.utils.QueryOperationDefaultVisitor;
import org.apache.flink.table.planner.operations.PlannerQueryOperation;
import org.apache.flink.table.planner.plan.schema.TableSourceTable;
import org.apache.flink.table.planner.plan.utils.DefaultRelShuttle;

import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;

import java.util.HashSet;
import java.util.Set;

/** Default implementation for table visitor. */
public class DefaultCollectTableVisitor
        extends QueryOperationDefaultVisitor<Set<ObjectIdentifier>> {
    private final Set<ObjectIdentifier> tables = new HashSet<>();

    @Override
    public Set<ObjectIdentifier> defaultMethod(QueryOperation other) {
        if (other instanceof PlannerQueryOperation) {
            PlannerQueryOperation plannerQueryOperation = (PlannerQueryOperation) other;
            RelNode relNode = plannerQueryOperation.getCalciteTree();
            relNode.accept(
                    new DefaultRelShuttle() {
                        @Override
                        public RelNode visit(TableScan scan) {
                            RelOptTable table = scan.getTable();
                            if (table instanceof TableSourceTable) {
                                TableSourceTable sourceTable = (TableSourceTable) table;
                                tables.add(sourceTable.contextResolvedTable().getIdentifier());
                            }
                            return super.visit(scan);
                        }
                    });
        } else {
            for (QueryOperation operation : other.getChildren()) {
                tables.addAll(operation.accept(this));
            }
        }
        return tables;
    }
}
