#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <server folder>"
  echo "Example: $0 ./run-1.21.6"
  exit 1
fi

cd "$1"

# Requires https://github.com/mikefarah/yq/ (go-yq in Arch repos)

set -v
# Configure server.properties
yq -i '.enable-command-block=true' server.properties

# Configure CoordinateOffset
yq -i '.bypassByPermission=false' plugins/CoordinateOffset/config.yml
yq -i '.verbose=true' plugins/CoordinateOffset/config.yml
