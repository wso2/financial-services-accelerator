# Set up IS connector for APIM

Use when `OB_VERSION=OB3`.

## 1) Download the connector

Pick connector using detected APIM version from APIM zip name/folder:

- APIM 4.2.0: https://apim.docs.wso2.com/en/4.2.0/assets/attachments/administer/wso2is-extensions-1.6.8.zip
- APIM 4.1.0: https://apim.docs.wso2.com/en/4.1.0/assets/attachments/administer/wso2is-extensions-1.4.2.zip
- APIM 4.0.0: https://apim.docs.wso2.com/en/4.0.0/assets/attachments/administer/wso2is-extensions-1.2.10.zip

If the detected APIM version is not listed above, stop and report an unsupported APIM version to the user.

## 2) Extract the connector

- Extract the downloaded `wso2is-extensions-*` zip.
- Use `<IS_EXTENSION>` to refer to the extracted connector directory.

Pre-check before copy:

- Ensure source files exist in `<IS_EXTENSION>`.
- Ensure destination directories exist in `<IS_HOME>`.

## 3) Copy required files to IS

Copy from `<IS_EXTENSION>` into `<IS_HOME>`:

1. From `<IS_EXTENSION>/dropins` to `<IS_HOME>/repository/components/dropins`:
   - `wso2is.key.manager.core*.jar`
   - `wso2is.notification.event.handlers*.jar`
2. From `<IS_EXTENSION>/webapps` to `<IS_HOME>/repository/deployment/server/webapps`:
   - `keymanager-operations.war`
