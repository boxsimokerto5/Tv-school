#!/usr/bin/env bash
set -e

echo "=== Preparing Keystores ==="

# 1. Restore release keystore (my-upload-key.jks)
if [ -n "$KEYSTORE_BASE64" ]; then
  echo "Restoring from KEYSTORE_BASE64 environment variable..."
  echo "$KEYSTORE_BASE64" | tr -d ' \r\n\t' | base64 --decode > my-upload-key.jks
elif [ -f "my-upload-key.jks" ] && [ -s "my-upload-key.jks" ]; then
  echo "my-upload-key.jks already exists."
elif [ -f "my-upload-key.jks.base64" ] && [ -s "my-upload-key.jks.base64" ]; then
  echo "Restoring my-upload-key.jks from my-upload-key.jks.base64..."
  tr -d ' \r\n\t' < my-upload-key.jks.base64 | base64 --decode > my-upload-key.jks
fi

# Ensure my-upload-key.jks exists and is valid
if [ ! -s "my-upload-key.jks" ]; then
  echo "Error: my-upload-key.jks is missing or empty!"
  exit 1
fi

echo "Upload keystore is ready:"
ls -lh my-upload-key.jks

# 2. Ensure debug.keystore exists
if [ ! -f "debug.keystore" ]; then
  if [ -f "debug.keystore.base64" ] && [ -s "debug.keystore.base64" ]; then
    echo "Restoring debug.keystore from debug.keystore.base64..."
    tr -d ' \r\n\t' < debug.keystore.base64 | base64 --decode > debug.keystore
  else
    echo "Creating debug.keystore..."
    keytool -genkeypair -alias androiddebugkey -keypass android -keystore debug.keystore -storepass android -dname "CN=Android Debug,O=Android,C=US" -validity 10000 -keyalg RSA -keysize 2048
  fi
fi

echo "=== Keystores ready ==="
