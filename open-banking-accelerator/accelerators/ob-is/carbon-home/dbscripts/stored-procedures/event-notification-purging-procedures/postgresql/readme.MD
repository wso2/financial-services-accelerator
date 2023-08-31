## **HOW TO RUN**

**>> postgresql-event-notification-cleanup-script.sql**


**Compile the stored procedure**

First - Compile the stored procedure using a PostgreSQL client. Following is a sample for CLI based PostgreSQL client.
Make sure to create the procedure in the openbank_openbankingdb (consent DB) database schema only.

**Execute the stored procedure.**

Then execute the compiled stored procedure by using the call function in the PostgreSQL client. Following is a sample for CLI based PostgreSQL client.

- eventStatuses `VARCHAR`
- clientIds `VARCHAR`
- purgeEventsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- purgeNonExistingResourceIds `BOOLEAN`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- enableReindexing `BOOLEAN`
- enableTblAnalyzing `BOOLEAN`

```
WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP( eventStatuses, clientIds, purgeEventsOlderThanXNumberOfDays, lastUpdatedTime, 
                    purgeNonExistingResourceIds, backupTables, enableAudit, enableReindexing, enableTblAnalyzing);
```

```
Ex: 
pgsql> CALL WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 31, NULL, TRUE, TRUE, TRUE, TRUE, TRUE);

```

**Execute the restore from backup procedure.**

```
select WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP();
```



