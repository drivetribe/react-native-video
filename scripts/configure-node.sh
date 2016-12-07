#!/bin/bash -e

# Install npm 3
npm install -g npm@$NPM_VERSION

# Verify node and npm versions
node -v && npm -v

# Spinners make npm install slow
npm config set spin=false
npm config set progress=false
