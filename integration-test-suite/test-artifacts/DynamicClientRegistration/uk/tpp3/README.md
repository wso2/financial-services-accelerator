#Using Sample Resources

Following configs can be used in test-config.xml to use sample SSA and keystores for DCR tests.

    <DCR>
        <SSAPath>Path.To.Directory/ssa.txt</SSAPath>
        <!-- SSA SoftwareId -->
        <SoftwareId>jFQuQ4eQbNCMSqdCog21nF</SoftwareId>
        <!-- SSA Redirect Uri -->
        <RedirectUri>https://www.google.com/redirects/redirect1</RedirectUri>
    </DCR>

Use signing.jks in 'signing-keystore' directory as the Application Keystore, and transport.jks in 'transport-keystore'
directory as the Transport Keystore.

Sample Keystore information:

- Signing Kid = 7eJ8S_ZgvlYxFAFSghV9xMJROvk

- Signing keystore alias = tpp7-signing

- Signing keystore password = wso2carbon

- Transport Kid - 7x6UrhU-Yj1Aa9Ird03JJCcDurs

- Transport keystore alias = tpp7-transport

- Transport keystore password = wso2carbon
