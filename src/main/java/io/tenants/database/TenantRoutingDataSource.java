package io.tenants.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        TenantContext context = TenantContextHolder.getContext();
        if (context != null && context.getTenant() != null) {
            return context.getTenant().getDataSourceConfig().getId();
        }
        return null;
    }
}
