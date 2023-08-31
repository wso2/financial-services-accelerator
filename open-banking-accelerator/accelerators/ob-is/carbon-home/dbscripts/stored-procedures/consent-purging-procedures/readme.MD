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

This is a consent data cleanup script with batch wise delete, This procedure includes the cleanup of consents from the respective tables of *OB_CONSENT* , *OB_CONSENT_AUTH_RESOURCE* , *OB_CONSENT_MAPPING* , *OB_CONSENT_FILE* , *OB_CONSENT_ATTRIBUTE* , *OB_CONSENT_STATUS_AUDIT*

1. Compile the stored procedure.
2. Execute the compiled store procedure.

**Data retention feature:**

- If enabled, all the purged consents will be stored in temporary retention tables in the consent database. 
- Later these retention data will move to separate retention database with the configured periodical sync job. (This job can be invoked via API)
- This archived data can be extracted using the consent admin, audit log and file search API by sending the additional query parameter `fetchFromRetentionDatabase` : `true`
- For more details refer : **Data retention**.

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
  - Note: When running backup procedure (consent-cleanup-restore.sql) to restore back the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.

Note: below data retention parameters need to set in the scripts before compiling, Ignore if data retention is not enabled.

- `enableDataRetentionForAuthResourceAndMapping`
  - enable storing auth resource and consent mapping tables for retention data.
- `enableDataRetentionForObConsentFile`
  - enable storing ob_consent_file table for retention data.
- `enableDataRetentionForObConsentAttribute`
  - enable storing ob_consent_attribute table for retention data.
- `enableDataRetentionForObConsentStatusAudit`
  - enable storing ob_consent_status_audit table for retention data.


*OTHER PARAMETERS*

- `backupTables` 
  - enable the backup table to restore later at a later stage. (Please note this backup tables will be overwritten every time you run the cleanup script.). 
  - Also, this would not capture the consents which were created in between the backup task and the cleanup iteration, hence if consents created after the backup task will not be able to restore if its get deleted from the cleanup iteration.

- `enableAudit` 
  - By setting this parameter to true,  will log each of deleted consents in the auditlog_ob_consent_cleanup table for track them in a later time.

Once compiled the stored procedure, We can execute the procedure with input parameters as in below example. (for mysql)

```
WSO2_OB_CONSENT_CLEANUP_SP( consentTypes, clientIds, consentStatuses, purgeConsentsOlderThanXNumberOfDays, 
                                lastUpdatedTime, backupTables, enableAudit, analyzeTables, enableDataRetention );
```

```
Ex: 
mysql> call WSO2_OB_CONSENT_CLEANUP_SP('accounts,payments', 'clientId1,clientId2', 'expired,revoked', 31, NULL, 
                                            TRUE, TRUE, TRUE, FALSE);
```

Please refer the readme.MD file on each DB type for more information.

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

**CALCULATING CONSENTS IN OB_CONSENT TABLE* - This section used prints the breakdown of the consents which should delete and retain.

**BATCH DELETE * TABLE* - This section does the chunk and batch-wise delete for the consent data.

**REBUILDING INDEXES* - As an extra step to optimize the database, this can perform an index rebuilding task for improving the performance, However, it's not recommended to perform on a live system unless you have downtime. Hence this could lock down the whole table.

**ANALYSING TABLES*  - As an extra step you can perform a table analyze for gather the statistics for the tables which had the delete operation. This is also to improve the performance of the database. However, it's also not recommended to perform on a live system unless you have downtime.

**>> consent-cleanup-restore.sql**

This is the stored procedure used to restore the deleted consents from the consent tables. The restoration can be only done if the backupTables property is set to true in the WSO2_OB_CONSENT_CLEANUP_SP procedure.
- Note: When restoring the purged data with the retention feature enabled, make sure to clean retention tables with these un-purged data.

This is only an immediate restoration script for the WSO2_OB_CONSENT_CLEANUP_SP procedure, hence each execution of the WSO2_OB_CONSENT_CLEANUP_SP procedure will replace the backup tables.

**Please note that it is highly recommended to have a complete backup of the production environment before modifying any data, in case of any restoration would be required.**

*You can also schedule a cleanup task that will be automatically run after a given period of time.*

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

## **Data retention**

This feature is used for archival purposes of the data that we purge using the consent purging scripts.

This will,
- Keep the data records like consent and its audit log for 7+ (configurable) years even after purging, In a separate retention database.
- This archived data can be extracted using the consent admin, audit log and file search API by sending the additional query parameter `fetchFromRetentionDatabase` : `true`
  
**Instructions for enabling data retention**

- Create a separate database to store the retention data. Execute the default consent db creation scripts in `<OB_IS_ACCELERATOR_HOME>/carbon-home/dbscripts/open-banking/consent/` to create tables.
- Add below configs to introduce the new datasource to the deployment toml and to enable data retention support from the OB IAM server.

Ex: 

```
[[open_banking.jdbc_retention_data_persistence_manager]]
data_source.name = "WSO2OB_RET_DB"
connection_verification_timeout=1

[open_banking.consent.data_retention]
enabled=true

[[datasource]]
id="WSO2OB_RET_DB"
url = ".. jdbc url for retention database.."
username = "user"
password = "password"
driver = "com.mysql.jdbc.Driver"
jmx_enable=false
pool_options.maxActive = "150"
pool_options.maxWait = "60000"
pool_options.minIdle = "5"
pool_options.testOnBorrow = true
pool_options.validationQuery="SELECT 1"
#Use below for oracle
#validationQuery="SELECT 1 FROM DUAL"
pool_options.validationInterval="30000"
pool_options.defaultAutoCommit=false
```

- By enabling the data retention in the OB IAM server, we can use the `fetchFromRetentionDatabase` query param to fetch the data from the retention database in admin API's.
- Configure in the consent data purging script with types of data to be archived as below,
```
    SET enableDataRetentionForAuthResourceAndMapping = TRUE;
    SET enableDataRetentionForObConsentFile = FALSE;
    SET enableDataRetentionForObConsentAttribute = FALSE;
    SET enableDataRetentionForObConsentStatusAudit = TRUE; 
```
- Execute purging script with data retention enabled,

```
WSO2_OB_CONSENT_CLEANUP_SP(
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
- Later these temporary retention data will move to the retention database which is created in step 1, by a server db sync task. All the moved consents will be deleted from the temporary retention tables. (This task can be invoked via API or configured as periodical job) 
  
    - For retention data DB sync via API :
      ```
      curl --location --request GET 'https://localhost:9446/api/openbanking/consent/admin/sync-temporary-retention-data' \
      --header 'Authorization: Basic YWRtaW5Ad3NvMi5jb206d3NvMjEyMw=='
      ```

    - For retention data DB sync via periodical sync job :
      - Add below configs to deployment.toml file

      ```
      [open_banking.consent.data_retention]
      enabled=true
      enable_db_sync_periodical_job=true
      db_sync_cron_value="0 0/2 * ? * * *"         
      ```

- Finally, to purge the consents in the retention database, we can use these purging scripts in the retention database by only configuring the last update time to 7+ years.
  
  Ex:
  
  ```
  WSO2_OB_CONSENT_CLEANUP_SP('', '', '', 31*12*7,NULL,FALSE,FALSE,TRUE,FALSE);
  ```

- This archived data can be extracted using the consent admin search API by sending the additional query parameter `fetchFromRetentionDatabase` : `true`

  Ex: 
  - Consent search API:
  ``` 
  curl --location --request GET 'https://localhost:9446/api/openbanking/consent/admin/search/?fromTime=1638993429&toTime=1638993429&consentStatuses=valid&userIDs=admin@wso2.com@carbon.super&consentIDs=234ba17f-c3ac-4493-9049-d71f99c36dc2&clientIDs=V92mG4mQwiMqSE8oWS5_VvaVgb4a&limit=25&offset=0&fetchFromRetentionDatabase=true&consentTypes=accounts' \
  --header 'Authorization: Basic YWRtaW5Ad3NvMi5jb206d3NvMjEyMw==' \
  --data-raw ''
  ```
  
  - Consent status audit search API:
  ``` 
  curl --location --request GET 'https://localhost:9446/api/openbanking/consent/admin/search/consent-status-audit?limit=2&offset=10&consentIDs=d239d1be-4e81-46d6-8a2e-76345370dbf3,f18b6ce1-15f7-44f7-9633-4e66fa6ceb88&fetchFromRetentionDatabase=true' \
  --header 'Authorization: Basic YWRtaW5Ad3NvMi5jb206d3NvMjEyMw==' \
  --data-raw ''
  ```

  - Consent file search API:
  ``` 
  curl --location --request GET 'https://localhost:9446/api/openbanking/consent/admin/search/consent-file?consentId=05ac14f9-b980-45c1-913c-614cb1469a90' \
  --header 'Authorization: Basic YWRtaW5Ad3NvMi5jb206d3NvMjEyMw==' \
  --data-raw ''
  ```



