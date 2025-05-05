## **HOW TO RUN**

**>> mysql-event-notification-cleanup-script.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a mysql client. Following is a sample for CLI based mysql client.
Make sure to create the procedure in the fs_consentdb (consent DB) database schema only.

```
mysql> source \<path>\mysql-event-notification-cleanup-script.sql;
```
or
```
bash:~$ mysql -u yourusername -p yourpassword yourdatabase < \<path>\mysql-event-notification-cleanup-script.sql
```

**Compile the stored procedure**

Then execute the compiled store procedure by using the call function in the mysql client. Following is a sample for CLI based mysql client.

- eventStatuses `VARCHAR`
- clientIds `VARCHAR`
- purgeEventsOlderThanXNumberOfDays `INT`
- lastUpdatedTime `BIGINT`
- purgeNonExistingResourceIds `BOOLEAN`
- backupTables `BOOLEAN`
- enableAudit `BOOLEAN`
- analyzeTables `BOOLEAN`

```
WSO2_FS_EVENT_NOTIFICATION_CLEANUP_SP( eventStatuses, clientIds, purgeEventsOlderThanXNumberOfDays, lastUpdatedTime,
                purgeNonExistingResourceIds, backupTables, enableAudit, analyzeTables );
```

```
Ex: 
mysql> call WSO2_FS_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 10, NULL, TRUE, TRUE, TRUE, TRUE);

```


**Execute the restore from backup procedure.**

```
EXEC WSO2_FS_EVENT_NOTIFICATION_CLEANUP_DATA_RESTORE_SP();
```

**You can also schedule a cleanup task that will be automatically run after a given period of time.**

Ex: Mysql

```
USE 'WSO2_FS_CONSENT_DB';
DROP EVENT IF EXISTS event_notification_cleanup;
CREATE EVENT event_notification_cleanup
    ON SCHEDULE
        EVERY 1 WEEK STARTS '2015-01-01 00:00.00'
    DO
        CALL `WSO2_FS_CONSENT_DB`.WSO2_FS_EVENT_NOTIFICATION_CLEANUP_SP('ACK,ERR', 'clientId1,clientId2', 10, NULL, TRUE, TRUE, TRUE, TRUE);

-- 'Turn on the event_scheduler'
SET GLOBAL event_scheduler = ON;

```