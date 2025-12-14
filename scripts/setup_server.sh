#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [<packetevents version>]"
  echo "Example: $0 1.21.11 2.11.0"
  exit 1
fi

if [[ ! -d example-api-plugin ]]; then
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

# Link CoordinateOffset snapshot jar
if [[ ! -e "plugins/CoordinateOffset-SNAPSHOT.jar" ]]; then
  ln -s ../../paper/build/CoordinateOffset-Paper-SNAPSHOT.jar plugins/CoordinateOffset-Paper-SNAPSHOT.jar
fi

# Download packetevents
if [[ -n "${2:-}" ]]; then
  echo Downloading PacketEvents v$2 ...
  curl -fLO --output-dir "plugins" "https://github.com/retrooper/packetevents/releases/download/v$2/packetevents-spigot-$2.jar"
else
  echo "No PacketEvents version specified. Be sure to install it in the plugins folder."
fi

# Verify CoordinateOffset is built
if [[ ! -e ../paper/build/CoordinateOffset-Paper-SNAPSHOT.jar ]]; then
  echo "No plugin build at paper/build/CoordinateOffset-Paper-SNAPSHOT.jar. Be sure to run './gradlew build' before testing."
fi

echo "Server is ready at $(realpath .). Download a server JAR there and run it once."
echo "Configure with configure_server.sh"
