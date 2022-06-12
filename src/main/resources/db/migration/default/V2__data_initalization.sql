INSERT INTO saas.data_source_configs (created_at, created_by, initialize, name, updated_at, updated_by, database_url,
                                      username, password)
VALUES ('2018-09-12 15:21:00.441', NULL, true, 'db-test', '2018-09-12 15:21:00.441', NULL,
        'jdbc:postgresql://localhost:5432/tenant-test', 'postgres', 'postgres');

INSERT INTO saas.tenants (created_at, created_by, name, updated_at, updated_by, databaseUrl,
                          datasource_config_id)
VALUES ('2018-09-12 15:21:00.499', NULL, 'tenant-test', '2018-09-12 15:21:00.499', NULL,
        'https://tenant.databaseUrl.com/', (select id from saas.data_source_configs where name = 'db-test'));
