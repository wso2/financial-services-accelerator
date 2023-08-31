## **HOW TO RUN**

**>> postgresql-consent-cleanup-script.sql**


**Compile the stored procedure**

First - Compile the stored procedure using a PostgreSQL client. Following is a sample for CLI based PostgreSQL client.
Make sure to create the procedure in the openbank_openbankingdb (consent DB) database schema only.

**Execute the stored procedure.**

Then execute the compiled stored procedure by using the call function in the PostgreSQL client. Following is a sample for CLI based PostgreSQL client.

- consentTypes `VARCHAR`
- clientIds `VARCHAR`
- consentStatuses `VARCHAR`
- purgeConsentsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- enableReindexing `BOOLEAN`
- enableTblAnalyzing `BOOLEAN`
- enableDataRetention `BOOLEAN`

```
WSO2_OB_CONSENT_CLEANUP_SP( consentTypes, clientIds, consentStatuses, purgeConsentsOlderThanXNumberOfDays, 
            lastUpdatedTime, backupTables, enableAudit, enableReindexing, enableTblAnalyzing, enableDataRetention);
```
```
Ex: 
pgsql> CALL WSO2_OB_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, 
                                            TRUE, TRUE, TRUE, TRUE, FALSE);
```

**Execute the restore from backup procedure.**

```
select WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP();
```
- Note : If data retention feature is enabled, temporary retention tables will be created and stored the purged consents
  until the server job (can run periodical or invoke via API) sync the temporary data to retention database.
- Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.  

