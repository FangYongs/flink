package org.apache.flink.table.planner.delegation;

import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalTableScan;

import org.apache.flink.table.api.TableException;
import org.apache.flink.table.calcite.bridge.PlannerExternalQueryOperation;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.operations.AggregateQueryOperation;
import org.apache.flink.table.operations.CalculatedQueryOperation;
import org.apache.flink.table.operations.DataStreamQueryOperation;
import org.apache.flink.table.operations.DistinctQueryOperation;
import org.apache.flink.table.operations.ExternalQueryOperation;
import org.apache.flink.table.operations.FilterQueryOperation;
import org.apache.flink.table.operations.JoinQueryOperation;
import org.apache.flink.table.operations.ProjectQueryOperation;
import org.apache.flink.table.operations.QueryOperation;
import org.apache.flink.table.operations.QueryOperationVisitor;
import org.apache.flink.table.operations.SetQueryOperation;
import org.apache.flink.table.operations.SortQueryOperation;
import org.apache.flink.table.operations.SourceQueryOperation;
import org.apache.flink.table.operations.TableSourceQueryOperation;
import org.apache.flink.table.operations.ValuesQueryOperation;
import org.apache.flink.table.operations.WindowAggregateQueryOperation;
import org.apache.flink.table.planner.operations.InternalDataStreamQueryOperation;
import org.apache.flink.table.planner.operations.PlannerQueryOperation;
import org.apache.flink.table.planner.plan.schema.TableSourceTable;
import org.apache.flink.table.planner.plan.utils.DefaultRelShuttle;

import java.util.Map;

public class DefaultTableSnapshotVisitor implements QueryOperationVisitor<QueryOperation> {
    private final Map<ObjectIdentifier, Long> tableSnapshots;

    public DefaultTableSnapshotVisitor(Map<ObjectIdentifier, Long> tableSnapshots) {
        this.tableSnapshots = tableSnapshots;
    }

    @Override
    public QueryOperation visit(SourceQueryOperation catalogTable) {
        ObjectIdentifier identifier = catalogTable.getContextResolvedTable().getIdentifier();
        Long snapshot = tableSnapshots.get(identifier);
        if (snapshot != null) {
            return new SourceQueryOperation(
                    catalogTable.getContextResolvedTable().copy(snapshot),
                    catalogTable.getDynamicOptions());
        }

        return catalogTable;
    }

    @Override
    public QueryOperation visit(QueryOperation other) {
        if (other instanceof PlannerQueryOperation) {
            PlannerQueryOperation plannerQueryOperation = (PlannerQueryOperation) other;
            return new PlannerQueryOperation(plannerQueryOperation
                    .getCalciteTree()
                    .accept(new TableSnapshotRelShuttle(tableSnapshots)));
        } else if (other instanceof PlannerExternalQueryOperation) {
            PlannerExternalQueryOperation plannerExternalQueryOperation = (PlannerExternalQueryOperation) other;
            return new PlannerExternalQueryOperation(
                    plannerExternalQueryOperation
                            .getCalciteTree()
                            .accept(new TableSnapshotRelShuttle(tableSnapshots)),
                    plannerExternalQueryOperation.getResolvedSchema());
        } else if (other instanceof ExternalQueryOperation
                || other instanceof DataStreamQueryOperation
                || other instanceof InternalDataStreamQueryOperation) {
            return other;
        }

        throw new TableException("Unknown table operation: " + other);
    }

    @Override
    public QueryOperation visit(ProjectQueryOperation projection) {
        return new ProjectQueryOperation(
                projection.getProjectList(),
                projection.getChildren().get(0).accept(this),
                projection.getResolvedSchema());
    }

    @Override
    public QueryOperation visit(AggregateQueryOperation aggregation) {
        return new AggregateQueryOperation(
                aggregation.getGroupingExpressions(),
                aggregation.getAggregateExpressions(),
                aggregation.getChildren().get(0).accept(this),
                aggregation.getResolvedSchema());
    }

    @Override
    public QueryOperation visit(WindowAggregateQueryOperation windowAggregate) {
        return new WindowAggregateQueryOperation(
                windowAggregate.getGroupingExpressions(),
                windowAggregate.getAggregateExpressions(),
                windowAggregate.getWindowPropertiesExpressions(),
                windowAggregate.getGroupWindow(),
                windowAggregate.getChildren().get(0).accept(this),
                windowAggregate.getResolvedSchema());
    }

    @Override
    public QueryOperation visit(JoinQueryOperation join) {
        QueryOperation left = join.getChildren().get(0).accept(this);
        QueryOperation right = join.getChildren().get(1).accept(this);
        return new JoinQueryOperation(
                left,
                right,
                join.getJoinType(),
                join.getCondition(),
                join.isCorrelated());
    }

    @Override
    public QueryOperation visit(SetQueryOperation setOperation) {
        return setOperation;
    }

    @Override
    public QueryOperation visit(FilterQueryOperation filter) {
        return new FilterQueryOperation(
                filter.getCondition(),
                filter.getChildren().get(0).accept(this));
    }

    @Override
    public QueryOperation visit(DistinctQueryOperation distinct) {
        return new DistinctQueryOperation(distinct.getChildren().get(0).accept(this));
    }

    @Override
    public QueryOperation visit(SortQueryOperation sort) {
        return new SortQueryOperation(
                sort.getOrder(),
                sort.getChildren().get(0).accept(this),
                sort.getOffset(),
                sort.getFetch());
    }

    @Override
    public QueryOperation visit(CalculatedQueryOperation calculatedTable) {
        return calculatedTable;
    }

    @Override
    public QueryOperation visit(ValuesQueryOperation values) {
        return values;
    }

    @Override
    public <U> QueryOperation visit(TableSourceQueryOperation<U> tableSourceTable) {
        return tableSourceTable;
    }

    static class TableSnapshotRelShuttle extends DefaultRelShuttle {
        private final Map<ObjectIdentifier, Long> tableSnapshots;

        private TableSnapshotRelShuttle(Map<ObjectIdentifier, Long> tableSnapshots) {
            this.tableSnapshots = tableSnapshots;
        }

        @Override
        public RelNode visit(TableScan scan) {
            if (scan instanceof LogicalTableScan) {
                return resetTableSnapshot((LogicalTableScan) scan);
            }
            return super.visit(scan);
        }

        private RelNode resetTableSnapshot(LogicalTableScan scan) {
            final RelOptTable scanTable = scan.getTable();
            if (!(scanTable instanceof TableSourceTable)) {
                return scan;
            }
            TableSourceTable table = (TableSourceTable) scanTable;
            ObjectIdentifier identifier = table.contextResolvedTable().getIdentifier();
            Long snapshot = tableSnapshots.get(identifier);
            if (snapshot != null) {
                return new LogicalTableScan(
                        scan.getCluster(),
                        scan.getTraitSet(),
                        scan.getHints(),
                        table.copy(snapshot));
            }

            return scan;
        }
    }
}
