#!/bin/bash

echo "[pgvector-install] Starting Linux pgvector OS-level installation..."

# Check if pgvector.so exists in the PostgreSQL extension directory (common check)
PG_EXT_DIR=$(pg_config --pkglibdir)
if [ -f "$PG_EXT_DIR/vector.so" ]; then
    echo "[pgvector-install] ✅ pgvector extension library already installed at $PG_EXT_DIR/vector.so"
    exit 0
fi

# Install dependencies for building extensions (if needed)
sudo apt-get update
sudo apt-get install -y postgresql-server-dev-all build-essential git

# Clone, build, and install pgvector extension
if [ ! -d "pgvector" ]; then
    git clone https://github.com/pgvector/pgvector.git
fi
cd pgvector || exit 1
make
sudo make install

echo "[pgvector-install] Completed Linux pgvector OS-level installation."
