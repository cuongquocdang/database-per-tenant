package io.tenants.database.saas;

import io.tenants.database.admin.model.DataSourceConfig;
import io.tenants.database.admin.model.Tenant;
import io.tenants.database.admin.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private final transient DataSource datasource;
    private final transient DataSourceProperties dsProperties;
    private final transient TenantRepository tenantRepository;

    private final Map<String, DataSource> tenantDataSources = new HashMap<>();

    @Override
    protected DataSource selectAnyDataSource() {
        return datasource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return tenantDataSources.computeIfAbsent(tenantIdentifier, this::getTenantDatabase);
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.info("Get connection for tenant {}", tenantIdentifier);
        Connection connection = super.getConnection(tenantIdentifier);
        connection.setSchema(TenantSchemaResolver.DEFAULT_SCHEMA);
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        log.info("Release connection for tenant {}", tenantIdentifier);
        releaseAnyConnection(connection);
    }

    private DataSource getTenantDatabase(String tenantIdentifier) {
        Tenant tenant = tenantRepository.findByName(tenantIdentifier)
                .orElseThrow(() -> new RuntimeException("Invalid Tenant"));
        DataSourceConfig dataSourceConfig = tenant.getDataSourceConfig();
        DataSource dataSource = tenantDataSources.computeIfAbsent(tenant.getName(), s -> createDataSource(dataSourceConfig));
        executeFlywayMigration(dataSource);
        return dataSource;
    }

    @PostConstruct
    public void configureDataSources() {
        List<Tenant> tenants = tenantRepository.findAll();
        tenants.forEach(tenant -> {
            DataSource dataSource = createDataSource(tenant.getDataSourceConfig());
            executeFlywayMigration(dataSource);
            tenantDataSources.put(tenant.getName(), dataSource);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DataSource createDataSource(DataSourceConfig dataSourceConfig) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
                .driverClassName(dsProperties.getDriverClassName())
                .url(dataSourceConfig.getDatabaseUrl())
                .username(dataSourceConfig.getUsername())
                .password(dataSourceConfig.getPassword());
        if (dsProperties.getType() != null) {
            dataSourceBuilder.type(dsProperties.getType());
        }
        return dataSourceBuilder.build();
    }

    private void executeFlywayMigration(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(TenantSchemaResolver.DEFAULT_SCHEMA)
                .locations("db/migration/tenants")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }

}
