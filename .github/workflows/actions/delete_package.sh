#!/bin/bash

#
# Copyright (c) SRG SSR. All rights reserved.
# License information is available from the LICENSE file.
#

ORG="$1"
PACKAGE_NAME="$2"
VERSION_NAME="$3"
PACKAGE_TYPE="maven"

# Check that we have the necessary inputs
if [[ -z "$ORG" || -z "$PACKAGE_NAME" || -z "$VERSION_NAME" ]]; then
  echo "Usage: $0 <organization> <package_name> <version_name>"
  exit 1
fi

# Get the list of recent versions published for PACKAGE_NAME
VERSIONS=$(gh api \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "/orgs/$ORG/packages/$PACKAGE_TYPE/$PACKAGE_NAME/versions")

if [[ -z "$VERSIONS" ]]; then
  echo "Failed to retrieve package versions for $ORG/$PACKAGE_NAME"
  exit 1
fi

# Get the version id corresponding to the VERSION_NAME we want to delete
VERSION_ID=$(echo $VERSIONS | jq -r --arg version "$VERSION_NAME" 'map(select(.name == $version) | .id)[0] // ""')

if [[ -z "$VERSION_ID" ]]; then
  echo "Version '$VERSION_NAME' not found for $ORG/$PACKAGE_NAME"
  exit 1
fi

# Delete PACKAGE_NAME version VERSION_NAME (id VERSION_ID)
gh api \
  --method DELETE \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "/orgs/$ORG/packages/$PACKAGE_TYPE/$PACKAGE_NAME/versions/$VERSION_ID"

if [[ "$?" -ne 0 ]]; then
    echo "Failed to delete version $VERSION_NAME of $ORG/$PACKAGE_NAME"
    exit 1
fi

echo "Successfully deleted version '$VERSION_NAME' of $ORG/$PACKAGE_NAME"
