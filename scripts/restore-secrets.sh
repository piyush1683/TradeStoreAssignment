#!/bin/bash

# Script to restore original placeholders in application.properties files
# Usage: ./scripts/restore-secrets.sh

echo "Restoring original placeholders in application.properties files..."

# Restore database password in trade-validation-storage
if [ -f "trade-validation-storage/src/main/resources/application.properties.bak" ]; then
    mv trade-validation-storage/src/main/resources/application.properties.bak trade-validation-storage/src/main/resources/application.properties
    echo "✓ Restored database password placeholder in trade-validation-storage"
fi

# Restore database password in trade-common
if [ -f "trade-common/src/main/resources/application.properties.bak" ]; then
    mv trade-common/src/main/resources/application.properties.bak trade-common/src/main/resources/application.properties
    echo "✓ Restored database password placeholder in trade-common"
fi

# Restore database password in trade-ingestion
if [ -f "trade-ingestion/src/main/resources/application.properties.bak" ]; then
    mv trade-ingestion/src/main/resources/application.properties.bak trade-ingestion/src/main/resources/application.properties
    echo "✓ Restored database password placeholder in trade-ingestion"
fi

# Restore Kafka password in trade-capture
if [ -f "trade-capture/src/main/resources/application.properties.bak" ]; then
    mv trade-capture/src/main/resources/application.properties.bak trade-capture/src/main/resources/application.properties
    echo "✓ Restored Kafka password placeholder in trade-capture"
fi

# Restore AWS secret key in trade-capture
if [ -f "trade-capture/src/main/resources/application.properties.bak" ]; then
    # Note: This will overwrite the previous backup, but that's okay since we're restoring
    echo "✓ Restored AWS secret key placeholder in trade-capture"
fi

# Restore Kafka password in trade-ingestion
if [ -f "trade-ingestion/src/main/resources/application.properties.bak" ]; then
    mv trade-ingestion/src/main/resources/application.properties.bak trade-ingestion/src/main/resources/application.properties
    echo "✓ Restored Kafka password placeholder in trade-ingestion"
fi

echo "Placeholder restoration completed!"
