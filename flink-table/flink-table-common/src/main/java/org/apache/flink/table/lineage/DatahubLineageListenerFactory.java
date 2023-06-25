package org.apache.flink.table.lineage;

import org.apache.flink.api.lineage.JobStatusChangedListener;
import org.apache.flink.api.lineage.JobStatusChangedListenerFactory;

import java.util.Map;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class DatahubLineageListenerFactory implements JobStatusChangedListenerFactory {
    private static final String DATAHUB_REST_ADDRESS = "datahub.rest.url";

    @Override
    public JobStatusChangedListener createListener(Context context) {
        Map<String, String> config = context.getConfiguration().toMap();
        String url = checkNotNull(config.get(DATAHUB_REST_ADDRESS));
        return new DatahubLineageListener(url);
    }
}
