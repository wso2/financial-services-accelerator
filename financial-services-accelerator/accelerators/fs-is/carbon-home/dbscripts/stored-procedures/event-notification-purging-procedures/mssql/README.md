## **HOW TO RUN**

**>> mssql-event-notification-cleanup-script.sql**

**Compile the stored procedure**

First - Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the fs_consentdb (consent DB) database schema only.

**Execute the stored procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.


- eventStatuses `VARCHAR`
- clientIds `VARCHAR`
- purgeEventsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- purgeNonExistingResourceIds `BOOLEAN`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- rebuildIndexes `BOOLEAN`
- updateStats `BOOLEAN`

```
Ex: 

>> EXEC WSO2_FS_EVENT_NOTIFICATION_CLEANUP_SP @eventStatuses='ACK,ERR', @clientIds='clientId1,clientId2', 
                            @purgeEventsOlderThanXNumberOfDays=31, @lastUpdatedTime=1631867692, @purgeNonExistingResourceIds=TRUE,
                            @backupTables=TRUE, @enableAudit=TRUE, @rebuildIndexes=TRUE,
                            @updateStats=TRUE;
```


**Execute the restore from backup procedure.**

```
EXEC WSO2_FS_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP;
```






