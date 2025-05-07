## **HOW TO RUN**

**>> oracle-consent-cleanup-script.sql**


**Compile the stored procedure**

First - Compile the stored procedure using a Oracle client. Following is a sample for CLI based Oracle client.
Make sure to create the procedure in the fs_consentdb (consent DB) database schema only.

```
sqlplus> @\<path>\oracle-consent-cleanup-script.sql;
```

**Execute the stored procedure.**

Then execute the compiled store procedure by using the exec function in the Oracle client. Following is a sample for CLI based Oracle client.

- consentTypes  `VARCHAR2`
- clientIds  `VARCHAR2`
- consentStatuses  `VARCHAR2`
- purgeConsentsOlderThanXNumberOfDays  `NUMBER`
- lastUpdatedTime  `NUMBER`
- backupTables  `BOOLEAN`
- enableAudit  `BOOLEAN`
- enableStsGthrn  `BOOLEAN`
- enableRebuilddexes  `BOOLEAN`
- enableDataRetention `BOOLEAN`

```
WSO2_FS_CONSENT_CLEANUP_SP( consentTypes, clientIds, consentStatuses, purgeConsentsOlderThanXNumberOfDays,
                lastUpdatedTime, backupTables, enableAudit, enableStsGthrn, enableRebuildIndexes, enableDataRetention );
```

```
Ex: 

sqlplus> exec WSO2_FS_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, 
                                            TRUE, TRUE, TRUE, TRUE, FALSE);                
```

**Execute the restore from backup procedure.**

```
EXEC WSO2_FS_CONSENT_CLEANUP_DATA_RESTORE_SP();
```

- Note : If data retention feature is enabled, temporary retention tables will be created and stored the purged consents.
- Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.  
