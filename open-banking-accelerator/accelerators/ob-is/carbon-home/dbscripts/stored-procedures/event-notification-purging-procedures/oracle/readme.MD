## **HOW TO RUN**

**>> oracle-event-notification-cleanup-script.sql**


**Compile the stored procedure**

First - Compile the stored procedure using a Oracle client. Following is a sample for CLI based Oracle client.
Make sure to create the procedure in the openbank_openbankingdb (consent DB) database schema only.

```
sqlplus> @\<path>\oracle-event-notification-cleanup-script.sql;
```

**Execute the stored procedure.**

Then execute the compiled store procedure by using the exec function in the Oracle client. Following is a sample for CLI based Oracle client.

- eventStatuses  `VARCHAR2`
- clientIds  `VARCHAR2`
- purgeEventsOlderThanXNumberOfDays  `NUMBER`
- lastUpdatedTime  `NUMBER`
- purgeNonExistingResourceIds `BOOLEAN`
- backupTables  `BOOLEAN`
- enableAudit  `BOOLEAN`
- enableStsGthrn  `BOOLEAN`
- enableRebuilddexes  `BOOLEAN`

```
WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP( eventStatuses, clientIds, purgeEventsOlderThanXNumberOfDays, lastUpdatedTime,
                                purgeNonExistingResourceIds, backupTables, enableAudit, enableStsGthrn, enableRebuildIndexes );
```

```
Ex: 

sqlplus> exec WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 31, NULL, 
                                            TRUE, TRUE, TRUE, TRUE, TRUE);                
```

**Execute the restore from backup procedure.**

```
EXEC WSO2_OB_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP();
```
