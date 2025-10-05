# Secret Management Guide

This document explains how to manage secrets in the Trade Store Application.

## Overview

The application uses placeholder values in `application.properties` files that are replaced with actual secrets during the CI/CD process or local development.

## Placeholders Used

| Placeholder | File | Description |
|-------------|------|-------------|
| `{password}` | trade-validation-storage | PostgreSQL database password |
| `{kafka-password}` | trade-capture, trade-ingestion | Kafka authentication password |
| `{aws-secret-key}` | trade-capture | AWS DynamoDB secret key |

## GitHub Actions Setup

### Required Secrets

Add the following secrets to your GitHub repository:

1. Go to your repository on GitHub
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add:

| Secret Name | Description |
|-------------|-------------|
| `DB_PASSWORD` | PostgreSQL database password |
| `KAFKA_PASSWORD` | Kafka authentication password |
| `AWS_SECRET_KEY` | AWS DynamoDB secret key |

### How It Works

The GitHub Actions workflow automatically replaces placeholders with secrets before building:

```yaml
- name: Replace secrets in application.properties
  run: |
    sed -i "s/{password}/${{ secrets.DB_PASSWORD }}/g" trade-validation-storage/src/main/resources/application.properties
    sed -i "s/{kafka-password}/${{ secrets.KAFKA_PASSWORD }}/g" trade-capture/src/main/resources/application.properties
    sed -i "s/{aws-secret-key}/${{ secrets.AWS_SECRET_KEY }}/g" trade-capture/src/main/resources/application.properties
    sed -i "s/{kafka-password}/${{ secrets.KAFKA_PASSWORD }}/g" trade-ingestion/src/main/resources/application.properties
```

## Local Development

### Using Environment Variables

1. Set environment variables:
```bash
export DB_PASSWORD="your_db_password"
export KAFKA_PASSWORD="your_kafka_password"
export AWS_SECRET_KEY="your_aws_secret_key"
```

2. Run the replacement script:
```bash
chmod +x scripts/replace-secrets.sh
./scripts/replace-secrets.sh
```

3. Build and run your application:
```bash
./gradlew build
```

4. Restore placeholders when done:
```bash
chmod +x scripts/restore-secrets.sh
./scripts/restore-secrets.sh
```

### Using .env File (Alternative)

Create a `.env` file in the project root:

```bash
DB_PASSWORD=your_db_password
KAFKA_PASSWORD=your_kafka_password
AWS_SECRET_KEY=your_aws_secret_key
```

Then source it before running the replacement script:

```bash
source .env
./scripts/replace-secrets.sh
```

## Security Best Practices

1. **Never commit real secrets** to version control
2. **Use placeholders** in all configuration files
3. **Rotate secrets regularly** in production
4. **Use different secrets** for different environments (dev, staging, prod)
5. **Limit access** to GitHub secrets to authorized personnel only

## Troubleshooting

### Common Issues

1. **Secrets not replaced**: Check that GitHub secrets are properly configured
2. **Build fails**: Ensure all required secrets are set
3. **Local development issues**: Verify environment variables are set correctly

### Verification

To verify secrets are properly replaced, check the build logs in GitHub Actions or examine the application.properties files after running the replacement script.

## Files Modified

- `.github/workflows/gradle.yml` - Added secret replacement step
- `scripts/replace-secrets.sh` - Local development script
- `scripts/restore-secrets.sh` - Restore placeholders script
- `docs/SECRET_MANAGEMENT.md` - This documentation
