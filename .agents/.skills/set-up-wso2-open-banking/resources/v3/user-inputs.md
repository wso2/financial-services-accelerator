# User inputs

Use when `OB_VERSION=OB3`.

OB3 always uses both components: `IS+APIM`.

## Compatibility (validate after detection)

- Combination 01
  - IS: 5.11.0
  - APIM: 4.1.0 or 4.0.0
  - SI (optional): 4.1.0 or 4.0.0
- Combination 02
  - IS: 6.0.0
  - APIM: 4.2.0
  - SI (optional): 4.2.0
- Combination 03
  - IS: 6.1.0
  - APIM: 4.2.0
  - SI (optional): 4.2.0

## Ask in this order

0. Show the compatibility matrix above.
1. IS zip source
2. APIM zip source
3. IAM accelerator zip source
4. AM accelerator zip source
5. Data publishing feature required? (`yes`/`no`)
6. If `yes`: both SI and accelerator zip sources

## Version detection

- Detect `IS_VERSION` from IS zip name.
- Detect `APIM_VERSION` from APIM zip name.
- If data publishing is enabled, detect `SI_VERSION` from SI zip name.
- If detection fails from zip name, stop and ask the user for the version.
- Validate detected versions against combinations above.
- Stop on mismatch.

## Java

- 11 or 17
