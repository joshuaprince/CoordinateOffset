#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 1.21.6"
  exit 1
fi

if [[ ! -d src ]]; then
  echo "Please run this from the CoordinateOffset root directory."
  exit 1
fi

mkdir -p "run-$1"
cd "run-$1"

# EULA agree
echo "eula=true" > eula.txt

# Disable bStats
mkdir -p "plugins/bStats"
echo "enabled: false" > plugins/bStats/config.yml

# Copy plugins
if [[ ! -e "plugins/CoordinateOffset-SNAPSHOT.jar" ]]; then
  ln -s ../../build/CoordinateOffset-SNAPSHOT.jar plugins/CoordinateOffset-SNAPSHOT.jar
fi

# Verify CoordinateOffset is built
if [[ ! -e ../build/CoordinateOffset-SNAPSHOT.jar ]]; then
  echo "No plugin build at build/CoordinateOffset-SNAPSHOT.jar. Be sure to run './gradlew build' before testing."
fi

echo "Server is ready at $(realpath .). Download a server JAR there and run it once."
echo "Configure with configure_server.sh"
