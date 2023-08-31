# Introduction

You can perform the data purging by clearing the event notification data using the script given here. As the volume of the data stored grows over time, It is highly recommended to perform data purging on servers to mitigate performance issues.

Based on the use cases sometimes the data in the database might grow fast and cleaning them up from the product itself becomes expensive. These stored procedures help us to offload the expensive data cleanups to the database server.

This script is similar to the consent data purging script, However has different parameters to purge the event notification data.

`Tip : It is recommended to run these steps at the time where server traffic is low. Especially, if you are running this in the production environment for the first time, since the data volume to be purged may be higher. However, consider this as a housekeeping task that needs to be run at regular intervals.`

1) Take a backup of the running database.
2) Set up the database dump in a test environment and test it for any issues.

`Tip : We recommend that you test the database dump before the cleanup task as the cleanup can take some time.`

3) Execute the store procedures given for specific DBs.
4) Once the cleanup is over, start WSO2 Servers pointing to the cleaned-up database dump and test throughly for any issues.

## **Usage**

**>> event-notification-cleanup.sql**

This is the event-notification data cleanup script with batch wise delete, This procedure includes the cleanup of event notifications from the respective tables of *OB_NOTIFICATION* , *OB_NOTIFICATION_EVENT* , *OB_NOTIFICATION_ERROR*

1. Compile the stored procedure.
2. Execute the compiled store procedure.

*- Logical condition and configs used for event-notification purging.*

*EVENT NOTIFICATION DATA PURGING PARAMETERS*

- `eventStatuses` 
  - event statuses which should be eligible for purging. (ex : 'ACK,ERR') 
- `clientIds`                  
  - client_ids which should be eligible for purging. (leave as empty to skip)
- `purgeEventsOlderThanXNumberOfDays` 
  - time period to delete event-notifications older than `n` days. (check below for more info.)
- `lastUpdatedTime`   
  - last_updated_time for purging, (if event-notification's updated timestamp is older than this value then it's eligible for purging, check below for more info.)
- `purgeNonExistingResourceIds`
  - flag to enable the purging of untraceable event notifications (if the `ob_notification's` `resource_id` is not exists in the `ob_consent` table's `consent_id` (untraceable), then it's eligible for purging)
  - setting this to `true` will purge the untraceable notification event regard less filtering the other purging parameters.


- Here if we wish to purge event-notifications with last updated_time older than 31 days (1 month), we can configure `purgeEventsOlderThanXNumberOfDays` = `31`
this value is in number of days.
- If we wish to configure exact timestamp of the `lastUpdatedTime` rather than a time period, we can ignore configuring `purgeEventsOlderThanXNumberOfDays` = `NULL`,
and only configure lastUpdatedTime with exact unix timestamp.
    - `ex : lastupdatedtime = 1660737878;`

*OTHER PARAMETERS*

- `backupTables` 
  - enable the backup table to restore later at a later stage. (Please note this backup tables will be overwritten every time you run the cleanup script.). 
  - Also, this would not capture the event notifications which were created in between the backup task and the cleanup iteration, hence if event notifications created after the backup task will not be able to restore if its get deleted from the cleanup iteration.

- `enableAudit` 
  - By setting this parameter to true,  will log each of deleted event notification in the auditlog_ob_event_notification_cleanup table for track them in a later time.

Once compiled the stored procedure, We can execute the procedure with input parameters as in below example. (for mysql)

```
WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP( eventStatuses, clientIds, purgeEventsOlderThanXNumberOfDays, lastUpdatedTime,
                purgeNonExistingResourceIds, backupTables, enableAudit, analyzeTables );
```

```
Ex: 
mysql> call WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 10, NULL, TRUE, TRUE, TRUE, TRUE);

```

Please refer the readme.MD file on each DB type for more information.

*CONFIGURABLE ATTRIBUTES*

Following are some important variables you need to consider, You can configure these parameters directly in the script.

**batchSize* - This variable defines how many records will be deleted per batch for one iteration.

**chunkSize* - If you have millions of data in the table, this variable allows to handle them chunk wise , which is a larger set to the batch where the batch processes each of the chunk (ex if you have 20 million data in the particular table, the chunk will initially take half million of such data and provided it into the batch delete as 10000 records per batch. Once that chunk is completed it will get another half million and proceed.)

**checkCount* - If the event notifications were kept eligible for purging while the cleanup scripts run, it will be stuck in an endless loop. Hence, this defines a safe margin for the cleanup script to complete its job if the eligible event-notifications for delete are less than checkCount.

**sleepTime* - Used to define the wait time for each iteration of the batch deletes to avoid table locks.

**enableLog* - Parameter use for enable or disable the logs.

**logLevel* - Parameter used to set the log levels.

**Functions**

**BACKUP EVENT NOTIFICATION TABLES* - This section acts to back up all the required table in case of restoration to be performed.

**CREATING AUDITLOG TABLES FOR DELETING EVENT NOTIFICATIONS* - This section creates the initial audit logs table for persisting the deleted event-notifications.

**CALCULATING EVENT NOTIFICATIONS IN OB_NOTIFICATION TABLE* - This section used prints the breakdown of the event-notification which should delete and retain.

**BATCH DELETE * TABLE* - This section does the chunk and batch-wise delete for the event-notification data.

**REBUILDING INDEXES* - As an extra step to optimize the database, this can perform an index rebuilding task for improving the performance, However, it's not recommended to perform on a live system unless you have downtime. Hence this could lock down the whole table.

**ANALYSING TABLES*  - As an extra step you can perform a table analyze for gather the statistics for the tables which had the delete operation. This is also to improve the performance of the database. However, it's also not recommended to perform on a live system unless you have downtime.

**>> event-notification-cleanup-restore.sql**

This is the stored procedure used to restore the deleted event-notification from the event-notification tables. The restoration can be only done if the backupTables property is set to true in the WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP procedure.

This is only an immediate restoration script for the WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP procedure, hence each execution of the WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP procedure will replace the backup tables.

**Please note that it is highly recommended to have a complete backup of the production environment before modifying any data, in case of any restoration would be required.**


*You can also schedule a cleanup task that will be automatically run after a given period of time.*

Ex: For Mysql

```
USE 'WSO2_OB_CONSENT_DB';
DROP EVENT IF EXISTS event_notification_cleanup;
CREATE EVENT event_notification_cleanup
    ON SCHEDULE
        EVERY 1 WEEK STARTS '2015-01-01 00:00.00'
    DO
        CALL `WSO2_OB_CONSENT_DB`.WSO2_OB_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 10, NULL, TRUE, TRUE, TRUE, TRUE);

-- 'Turn on the event_scheduler'
SET GLOBAL event_scheduler = ON;

```