#!/bin/bash

if [ "$EUID" -eq 0 ]; then
	echo "Do not run this script as sudo"
	exit
fi

# Build the CLI
cd ..
./gradlew :phase-cli:shadowJar
cd scripts

# Install the CLI
sudo mkdir -p /usr/local/bin/phase-cli
sudo cp ../phase-cli/build/libs/phase-cli-*-all.jar /usr/local/bin/phase-cli/phase-cli.jar
sudo chmod +x /usr/local/bin/phase-cli/phase-cli.jar
sudo cp run_wrapper.sh /usr/local/bin/phase
sudo chmod +x /usr/local/bin/phase
