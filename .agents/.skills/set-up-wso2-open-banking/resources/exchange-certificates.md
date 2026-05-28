# Exchange certificates

Run this for **IS always**. Also run for **APIM** if selected. Also run for **SI** if selected.

For each selected component, do the steps below **in that component's security directory**:

- IS: `<IS_HOME>/repository/resources/security`
- APIM: `<APIM_HOME>/repository/resources/security`
- SI: `<SI_HOME>/resources/security`

Keystore/truststore files may be either `.p12` or `.jks` depending on the product pack. Use the file that exists:

- Keystore: `wso2carbon.p12` or `wso2carbon.jks`
- Truststore: `client-truststore.p12` or `client-truststore.jks`

Note: use the store password configured for that pack (commonly `wso2carbon`). If the password is unknown, stop and ask the user to manually run the command.

## 1) Replace product certificate (per selected component)

Goal: regenerate each product's `wso2carbon` keypair so its certificate CN matches the component hostname, then make all selected products trust each other's public certs.

1. Determine `<HOSTNAME>` from the component host URL:
   - Use `IS_HOST` for IS, `APIM_HOST` for APIM, `SI_HOST` for SI.
   - Strip scheme and port.
   - Example: `https://localhost:9446` → `localhost`.

2. Backup the existing keystore and truststore once:
   - Identify the keystore file: use `wso2carbon.p12` if it exists, else `wso2carbon.jks` (if neither exists, stop).
   - Identify the truststore file: use `client-truststore.p12` if it exists, else `client-truststore.jks` (if neither exists, stop).
   - Create one-time backups by copying to the same name with `.backup` inserted before the extension (examples: `wso2carbon.backup.p12`, `client-truststore.backup.jks`). Do not overwrite existing backups.

3. For each selected component (IS, optional APIM, optional SI), do the following in that component's security directory:
    - Using `keytool` on the keystore file (`wso2carbon.*`):
       - If alias `wso2carbon` exists, delete alias `wso2carbon`.
         ```bash
         keytool -delete -alias wso2carbon -keystore wso2carbon.p12 -storepass wso2carbon
         ```
       - Generate a new RSA 2048 keypair.
         ```bash
         keytool -genkeypair -alias wso2carbon -keyalg RSA -keysize 2048 -validity 3650 \
            -keystore wso2carbon.p12 -storetype PKCS12 -storepass wso2carbon -keypass wso2carbon \
            -dname "CN=<HOSTNAME>, OU=OB, O=WSO2, L=Colombo, S=WP, C=LK"
         ```
       - Keep the keystore type as-is (do not convert between `.p12` and `.jks`).
    - Export the regenerated certificate (alias `wso2carbon`) to `wso2carbon.pem` in the same directory.
      ```bash
      keytool -export -alias wso2carbon -keystore wso2carbon.p12 -storepass wso2carbon -rfc -file wso2carbon.pem
      ```

4. After all selected components have their `wso2carbon.pem`, make them trust each other:
    - Alias mapping: IS → `is_wso2carbon`, APIM → `apim_wso2carbon`, SI → `si_wso2carbon`.
    - Into each selected component's truststore (`client-truststore.*`), import the `wso2carbon.pem` of every **other** selected component using the alias mapping above.
      ```bash
      keytool -import -trustcacerts -alias <ALIAS> -file <SOURCE_SECURITY_DIR>/wso2carbon.pem \
         -keystore ./client-truststore.p12 -storepass wso2carbon -noprompt
      ```
    - Also import each component's own `wso2carbon.pem` into its own truststore using its own alias from the mapping above.
      ```bash
      keytool -import -trustcacerts -alias <ALIAS> -file ./wso2carbon.pem \
         -keystore ./client-truststore.p12 -storepass wso2carbon -noprompt
      ```
      - Before each import, if the alias already exists in that truststore, delete it.
      ```bash
      keytool -delete -alias <ALIAS> -keystore client-truststore.p12 -storepass wso2carbon
      ```
      - Use non-interactive import.

## 2) Import OB root and issuer CA certificates (per selected component)

Goal: trust OB sandbox CA certificates in the client truststore.

1. Download the OB root and issuing CA cert files once (reuse the same downloaded files for all components):
   - Root CA URL: `https://openbanking.atlassian.net/wiki/download/attachments/252018873/OB_SandBox_PP_Root%20CA.cer?version=1&modificationDate=1525354123970&cacheVersion=1&api=v2&download=true`
   - Issuing CA URL: `https://openbanking.atlassian.net/wiki/download/attachments/252018873/OB_SandBox_PP_Issuing%20CA.cer?version=1&modificationDate=1525354091771&cacheVersion=1&api=v2&download=true`
   - Save as `ob_root.pem` and `ob_issuer.pem`.

2. If download fails, stop and ask the user to provide the root and issuer certificate file paths.

3. For each selected component, in its security directory, using `keytool` on `client-truststore.*`:
   - If aliases `ob_root` or `ob_issuer` exist, delete them.
   ```bash
   keytool -delete -alias (ob_root|ob_issuer) -keystore client-truststore.p12 -storepass wso2carbon
   ```
   - Import `ob_root.pem` under alias `ob_root`.
   ```bash
   keytool -import -alias ob_root -file ob_root.pem -keystore client-truststore.p12 -storepass wso2carbon -noprompt
   ```
   - Import `ob_issuer.pem` under alias `ob_issuer`.
   ```bash
   keytool -import -alias ob_issuer -file ob_issuer.pem -keystore client-truststore.p12 -storepass wso2carbon -noprompt
   ```
