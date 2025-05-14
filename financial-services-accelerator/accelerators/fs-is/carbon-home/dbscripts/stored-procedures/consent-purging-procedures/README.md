# Introduction

You can perform data purging by clearing the consent data using the script given here. As the volume of the data stored grows over time, It is highly recommended to perform data purging on servers to mitigate performance issues.

Based on the use cases sometimes the data in the database might grow fast and cleaning them up from the product itself becomes expensive. These stored procedures help us to offload the expensive data cleanups to the database server.

`Tip : It is recommended to run these steps at the time where server traffic is low. Especially, if you are running this in the production environment for the first time, since the data volume to be purged may be higher. However, consider this as a housekeeping task that needs to be run at regular intervals.`

1) Take a backup of the running database.
2) Set up the database dump in a test environment and test it for any issues.

`Tip : We recommend that you test the database dump before the cleanup task as the cleanup can take some time.`

3) Execute the store procedures given for specific DBs.
4) Once the cleanup is over, start WSO2 Servers pointing to the cleaned-up database dump and test throughly for any issues.

## **Usage**

**>> consent-cleanup.sql**

This is a consent data cleanup script with batch wise delete, This procedure includes the cleanup of consents from the respective tables of *FS_CONSENT* , *FS_CONSENT_AUTH_RESOURCE* , *FS_CONSENT_MAPPING* , *FS_CONSENT_FILE* , *FS_CONSENT_ATTRIBUTE* , *FS_CONSENT_STATUS_AUDIT*

1. Compile the stored procedure.
2. Execute the compiled store procedure.

**Data retention feature:**

- If enabled, all the purged consents will be stored in temporary retention tables in the consent database.

*- Logical condition and configs used for consent purging and data retention.*

*CONSENT DATA PURGING PARAMETERS*
- `consentTypes` 
  - consent_types which should be eligible for purging. (ex : 'accounts,payments', leave as empty to skip)
- `clientIds`                  
  - client_ids which should be eligible for purging. (leave as empty to skip)
- `consentStatuses`           
  - consent_statuses which should be eligible for purging. (ex : 'expired,revoked')
- `purgeConsentsOlderThanXNumberOfDays` 
  - time period to delete consents older than `n` days. (check below for more info.)
- `lastUpdatedTime`   
  - last_updated_time for purging, (if consent's updated time is older than this value then it's eligible for purging, check below for more info.)

- Here if we wish to purge consents with last updated_time older than 31 days (1 month), we can configure `purgeConsentsOlderThanXNumberOfDays` = `31`
this value is in no. of days.
- If we wish to configure exact timestamp of the `lastUpdatedTime` rather than a time period, we can ignore configuring `purgeConsentsOlderThanXNumberOfDays` = `NULL`,
and only configure lastUpdatedTime with exact unix timestamp.
    - `ex : lastupdatedtime = 1660737878;`

*CONSENT DATA RETENTION PARAMETERS*
- `enableDataRetention`
  - set true for enable data retention (archive purged data, default : false)
  - Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables to avoid duplicates when purging again.

Note: below data retention parameters need to set in the scripts before compiling, Ignore if data retention is not enabled.

- `enableDataRetentionForAuthResourceAndMapping`
  - enable storing auth resource and consent mapping tables for retention data.
- `enableDataRetentionForFsConsentFile`
  - enable storing fs_consent_file table for retention data.
- `enableDataRetentionForFsConsentAttribute`
  - enable storing fs_consent_attribute table for retention data.
- `enableDataRetentionForFsConsentStatusAudit`
  - enable storing fs_consent_status_audit table for retention data.


*OTHER PARAMETERS*

- `backupTables` 
  - enable the backup table to restore later at a later stage. (Please note this backup tables will be overwritten every time you run the cleanup script.). 
  - Also, this would not capture the consents which were created in between the backup task and the cleanup iteration, hence if consents created after the backup task will not be able to restore if its get deleted from the cleanup iteration.

- `enableAudit` 
  - By setting this parameter to true,  will log each of deleted consents in the auditlog_fs_consent_cleanup table for track them in a later time.

Once compiled the stored procedure, We can execute the procedure with input parameters as in below example. (for mysql)

```
WSO2_FS_CONSENT_CLEANUP_SP( consentTypes, clientIds, consentStatuses, purgeConsentsOlderThanXNumberOfDays, 
                                lastUpdatedTime, backupTables, enableAudit, analyzeTables, enableDataRetention );
```

```
Ex: 
mysql> call WSO2_FS_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, 
                                            TRUE, TRUE, TRUE, FALSE);
```

Please refer the README.md file on each DB type for more information.

*CONFIGURABLE ATTRIBUTES*

Following are some important variables you need to consider, You can configure these parameters directly in the script.

**batchSize* - This variable defines how many records will be deleted per batch for one iteration.

**chunkSize* - If you have millions of data in the table, this variable allows to handle them chunk wise , which is a larger set to the batch where the batch processes each of the chunk (ex if you have 20 million data in the particular table, the chunk will initially take half million of such data and provided it into the batch delete as 10000 records per batch. Once that chunk is completed it will get another half million and proceed.)

**checkCount* - If the consent were kept expiring / revoking or eligible for purging while the cleanup scripts run, it will be stuck in an endless loop. Hence, this defines a safe margin for the cleanup script to complete its job if the eligible Consents for delete are less than checkCount.

**sleepTime* - Used to define the wait time for each iteration of the batch deletes to avoid table locks.

**enableLog* - Parameter use for enable or disable the logs.

**logLevel* - Parameter used to set the log levels.

**Functions**

**BACKUP CONSENT TABLE* - This section acts to back up all the required table in case of restoration to be performed.

**CREATING AUDITLOG TABLES FOR DELETING CONSENTS* - This section creates the initial audit logs table for persisting the deleted consents.

**CALCULATING CONSENTS IN FS_CONSENT TABLE* - This section used prints the breakdown of the consents which should delete and retain.

**BATCH DELETE * TABLE* - This section does the chunk and batch-wise delete for the consent data.

**REBUILDING INDEXES* - As an extra step to optimize the database, this can perform an index rebuilding task for improving the performance, However, it's not recommended to perform on a live system unless you have downtime. Hence this could lock down the whole table.

**ANALYSING TABLES*  - As an extra step you can perform a table analyze for gather the statistics for the tables which had the delete operation. This is also to improve the performance of the database. However, it's also not recommended to perform on a live system unless you have downtime.

**>> consent-cleanup-restore.sql**

This is the stored procedure used to restore the deleted consents from the consent tables. The restoration can be only done if the backupTables property is set to true in the WSO2_FS_CONSENT_CLEANUP_SP procedure.
- Note: When restoring the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.

This is only an immediate restoration script for the WSO2_FS_CONSENT_CLEANUP_SP procedure, hence each execution of the WSO2_FS_CONSENT_CLEANUP_SP procedure will replace the backup tables.

**Please note that it is highly recommended to have a complete backup of the production environment before modifying any data, in case of any restoration would be required.**

*You can also schedule a cleanup task that will be automatically run after a given period of time.*

Ex: For Mysql

```
USE 'WSO2_FS_CONSENT_DB';
DROP EVENT IF EXISTS consent_cleanup;
CREATE EVENT consent_cleanup
    ON SCHEDULE
        EVERY 1 WEEK STARTS '2015-01-01 00:00.00'
    DO
        CALL `WSO2_FS_CONSENT_DB`.WSO2_FS_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, TRUE, TRUE, TRUE, FALSE);

-- 'Turn on the event_scheduler'
SET GLOBAL event_scheduler = ON;

```

## **Data retention**

This feature is used for archival purposes of the data that we purge using the consent purging scripts.
  
**Instructions for enabling data retention**

- Configure in the consent data purging script with types of data to be archived as below,
```
    SET enableDataRetentionForAuthResourceAndMapping = TRUE;
    SET enableDataRetentionForFsConsentFile = FALSE;
    SET enableDataRetentionForFsConsentAttribute = FALSE;
    SET enableDataRetentionForFsConsentStatusAudit = TRUE; 
```
- Execute purging script with data retention enabled,

```
WSO2_FS_CONSENT_CLEANUP_SP(
                consentTypes,
                clientIds,
                consentStatuses,
                purgeConsentsOlderThanXNumberOfDays,
                lastUpdatedTime,
                backupTables,
                enableAudit,
                analyzeTables,
                enableDataRetention
);
```

- This will create temporary retention consent tables in consent DB with `RET_` prefix and store the purged data temporary to these tables.



