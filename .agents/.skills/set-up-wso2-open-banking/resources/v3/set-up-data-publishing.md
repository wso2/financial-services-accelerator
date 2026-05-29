# Set up data publishing

Applicability:

- Valid only for `OB_VERSION=OB3`
- Following steps run only when user explicitly requests data publishing

1. If not already true, enable API Manager analytics in `<APIM_HOME>/repository/conf/deployment.toml`:

```toml
[apim.analytics]
enable = true
```

2. If not already true, enable Open Banking data publishing in both files:
   - `<APIM_HOME>/repository/conf/deployment.toml`
   - `<IS_HOME>/repository/conf/deployment.toml`

```toml
[open_banking.data_publishing]
enable = true
```

3. Run SI certificate exchange using `<SI_HOME>/resources/security` from [exchange certificates](../exchange-certificates.md).
