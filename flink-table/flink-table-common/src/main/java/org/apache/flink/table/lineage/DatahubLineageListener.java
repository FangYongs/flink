package org.apache.flink.table.lineage;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.lineage.JobCreatedEvent;
import org.apache.flink.api.lineage.JobExecutionStatusEvent;
import org.apache.flink.api.lineage.JobLineage;
import org.apache.flink.api.lineage.JobStatusChangedEvent;
import org.apache.flink.api.lineage.JobStatusChangedListener;

import org.apache.flink.api.lineage.LineageEntity;
import org.apache.flink.api.lineage.LineageRelation;

import org.apache.flink.table.catalog.CatalogContext;

import org.apache.flink.table.catalog.ObjectIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.List;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class DatahubLineageListener implements JobStatusChangedListener {
    private static final Logger LOG = LoggerFactory.getLogger(DatahubLineageListener.class);
    private final String url;

    public DatahubLineageListener(String url) {
        this.url = url;
    }

    @Override
    public void onEvent(JobStatusChangedEvent event) {
        if (event instanceof JobCreatedEvent) {
            JobCreatedEvent createdEvent = (JobCreatedEvent) event;
            JobLineage jobLineage = createdEvent.lineage();
            for (LineageRelation relation : jobLineage.relations()) {
                List<LineageEntity> sources = relation.sources();
                LineageEntity sink = relation.sink();
                checkArgument(
                        sink instanceof TableLineageEntity,
                        String.format(
                                "Only support table sink lineage entity: %s",
                                sink.getClass().getName()));
                TableLineageEntity tableSink = (TableLineageEntity) sink;
                String sinkPhysicalPath = getPhysicalPath(tableSink);
                ObjectIdentifier sinkIdentifier = tableSink.identifier();

                for (LineageEntity source : sources) {
                    checkArgument(
                            source instanceof TableLineageEntity,
                            String.format(
                                    "Only support table source lineage entity: %s",
                                    source.getClass().getName()));
                    TableLineageEntity tableSource = (TableLineageEntity) source;

                    // Get physical table path for paimon catalog.
                    String sourcePhysicalPath = getPhysicalPath(tableSource);
                    ObjectIdentifier sourceIdentifier = tableSource.identifier();
                    createRelation(
                            createdEvent.jobId(),
                            createdEvent.jobName(),
                            sourcePhysicalPath,
                            sourceIdentifier,
                            sinkPhysicalPath,
                            sinkIdentifier);
                }
            }
        } else if (event instanceof JobExecutionStatusEvent) {
            JobExecutionStatusEvent executionStatusEvent = (JobExecutionStatusEvent) event;
            if (executionStatusEvent.oldStatus().isGloballyTerminalState()) {
                deleteRelation(executionStatusEvent.jobId(), executionStatusEvent.jobName(), executionStatusEvent.exception());
            }
        } else {
            LOG.error(
                    "Receive unsupported job status changed event: {}",
                    event.getClass().getName());
        }
    }

    private String getPhysicalPath(TableLineageEntity lineageEntity) {
        CatalogContext sinkCatalogContext = lineageEntity.catalogContext();
        String sinkCatalogIdentifier = sinkCatalogContext
                .getFactoryIdentifier()
                .orElseThrow(() -> {
                    throw new UnsupportedOperationException(
                            "Only support catalog table connector.");
                });
        checkArgument(
                sinkCatalogIdentifier.equals("paimon"),
                "Only support paimon connector for lineage");
        return checkNotNull(sinkCatalogContext
                .getConfiguration()
                .toMap()
                .get("path"));
    }

    private void createRelation(
            JobID jobId,
            String jobName,
            String sourcePhysicalPath,
            ObjectIdentifier sourceIdentifier,
            String sinkPhysicalPath,
            ObjectIdentifier sinkIdentifier) {
        // Create dataset from physical path and identifier for source and sink, then create relation from source to sink for given job
    }

    private void deleteRelation(JobID jobId, String jobName, @Nullable Throwable exception) {
        // Delete relation created by given job
    }
}
