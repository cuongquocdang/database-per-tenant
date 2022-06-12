`
docker run --name database-per-tenant -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=database-per-tenant -d postgres
`

`
create database "tenant-test";
`