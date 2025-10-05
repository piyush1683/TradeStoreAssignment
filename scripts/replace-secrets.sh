#!/bin/bash

# Script to replace secrets in application.properties files
# Usage: ./scripts/replace-secrets.sh

echo "Replacing secrets in application.properties files..."

# Check if environment variables are set
if [ -z "$DB_PASSWORD" ]; then
    echo "Warning: DB_PASSWORD environment variable is not set"
fi

if [ -z "$KAFKA_PASSWORD" ]; then
    echo "Warning: KAFKA_PASSWORD environment variable is not set"
fi

if [ -z "$AWS_SECRET_KEY" ]; then
    echo "Warning: AWS_SECRET_KEY environment variable is not set"
fi

# Replace database password in trade-validation-storage
if [ ! -z "$DB_PASSWORD" ]; then
    sed -i.bak "s/{password}/$DB_PASSWORD/g" trade-validation-storage/src/main/resources/application.properties
    echo "✓ Replaced database password in trade-validation-storage"
fi

# Replace database password in trade-common
if [ ! -z "$DB_PASSWORD" ]; then
    sed -i.bak "s/{password}/$DB_PASSWORD/g" trade-common/src/main/resources/application.properties
    echo "✓ Replaced database password in trade-common"
fi

# Replace database password in trade-ingestion
if [ ! -z "$DB_PASSWORD" ]; then
    sed -i.bak "s/{password}/$DB_PASSWORD/g" trade-ingestion/src/main/resources/application.properties
    echo "✓ Replaced database password in trade-ingestion"
fi

# Replace Kafka password in trade-capture
if [ ! -z "$KAFKA_PASSWORD" ]; then
    sed -i.bak "s/{kafka-password}/$KAFKA_PASSWORD/g" trade-capture/src/main/resources/application.properties
    echo "✓ Replaced Kafka password in trade-capture"
fi

# Replace AWS secret key in trade-capture
if [ ! -z "$AWS_SECRET_KEY" ]; then
    sed -i.bak "s/{aws-secret-key}/$AWS_SECRET_KEY/g" trade-capture/src/main/resources/application.properties
    echo "✓ Replaced AWS secret key in trade-capture"
fi

# Replace Kafka password in trade-ingestion
if [ ! -z "$KAFKA_PASSWORD" ]; then
    sed -i.bak "s/{kafka-password}/$KAFKA_PASSWORD/g" trade-ingestion/src/main/resources/application.properties
    echo "✓ Replaced Kafka password in trade-ingestion"
fi

echo "Secret replacement completed!"
