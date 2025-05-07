## **HOW TO RUN**

**>> mssql-consent-cleanup-script.sql**

**Compile the stored procedure**

First - Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the fs_consentdb (consent DB) database schema only.

**Execute the stored procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.


- consentTypes `VARCHAR`
- clientIds `VARCHAR`
- consentStatuses `VARCHAR`
- purgeConsentsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- rebuildIndexes `BOOLEAN`
- updateStats `BOOLEAN`
- enableDataRetention `BOOLEAN`

```
Ex: 

>> EXEC WSO2_FS_CONSENT_CLEANUP_SP @consentTypes='accounts,payments', @clientIds='clientId1,clientId2', 
                            @consentStatuses='expired,revoked', @purgeConsentsOlderThanXNumberOfDays=31, 
                            @lastUpdatedTime=1631867692, @backupTables=TRUE, @enableAudit=TRUE, @rebuildIndexes=TRUE,
                            @updateStats=TRUE, @enableDataRetention=FALSE;
```


**Execute the restore from backup procedure.**

```
EXEC WSO2_FS_CONSENT_CLEANUP_DATA_RESTORE_SP;
```

- Note : If data retention feature is enabled, temporary retention tables will be created and stored the purged consents.
- Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.
