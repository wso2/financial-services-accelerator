## **HOW TO RUN**

**>> mysql-consent-cleanup-script.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a mysql client. Following is a sample for CLI based mysql client.
Make sure to create the procedure in the openbank_openbankingdb (consent DB) database schema only.

```
mysql> source \<path>\mysql-consent-cleanup-script.sql;
```
or
```
bash:~$ mysql -u yourusername -p yourpassword yourdatabase < \<path>\mysql-consent-cleanup-script.sql
```

**Compile the stored procedure**

Then execute the compiled store procedure by using the call function in the mysql client. Following is a sample for CLI based mysql client.

- consentTypes `VARCHAR`
- clientIds `VARCHAR`
- consentStatuses `VARCHAR`
- purgeConsentsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- analyzeTables `BOOLEAN`
- enableDataRetention `BOOLEAN`

```
WSO2_OB_CONSENT_CLEANUP_SP( consentTypes, clientIds, consentStatuses, purgeConsentsOlderThanXNumberOfDays, 
                                lastUpdatedTime, backupTables, enableAudit, analyzeTables, enableDataRetention );
```

```
Ex: 
mysql> call WSO2_OB_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, 
                                            TRUE, TRUE, TRUE, FALSE);
```


**Execute the restore from backup procedure.**

```
call WSO2_OB_CONSENT_CLEANUP_DATA_RESTORE_SP();
```
- Note : If data retention feature is enabled, temporary retention tables will be created and stored the purged consents
  until the server job (can run periodical or invoke via API) sync the temporary data to retention database.
- Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.  

**You can also schedule a cleanup task that will be automatically run after a given period of time.**

Ex: For Mysql

```
USE 'WSO2_OB_CONSENT_DB';
DROP EVENT IF EXISTS consent_cleanup;
CREATE EVENT consent_cleanup
    ON SCHEDULE
        EVERY 1 WEEK STARTS '2015-01-01 00:00.00'
    DO
        CALL `WSO2_OB_CONSENT_DB`.WSO2_OB_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, TRUE, TRUE, TRUE, FALSE);

-- 'Turn on the event_scheduler'
SET GLOBAL event_scheduler = ON;

```