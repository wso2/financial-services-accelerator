# User inputs

Use when `OB_VERSION=OB4`.

## Component modes (select one)

- `IS_ONLY`
- `IS+APIM`

## Ask in this order

1. Deployment mode (`IS_ONLY` or `IS+APIM`)
2. IS zip source
3. IAM accelerator zip source
4. If `IS+APIM`: APIM zip source
5. If `IS+APIM`: AM accelerator zip source

## Version detection

- Detect `IS_VERSION` from IS zip name.
- If `IS+APIM`, detect `APIM_VERSION` from APIM zip name.
- Detect IAM accelerator version from IAM accelerator zip name.
- If `IS+APIM`, detect AM accelerator version from AM accelerator zip name.
- If detection fails from zip name, stop and ask the user for the version.
- Validate detected versions against supported versions:
	- IS: `7.0.0` or later
	- APIM: `4.4.0` or later (if selected)
	- IAM accelerator: `≥4.0.0`
	- AM accelerator: `≥4.0.0` (if selected)
- Stop on mismatch.

## Java

- 11, 17, or 21
