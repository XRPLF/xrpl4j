#!/bin/bash

# Build script for Smart Escrow WASM Functions
# This script compiles each function separately using Cargo features

set -e

echo "Building Smart Escrow WASM Functions..."

# Check if Rust is installed
if ! command -v cargo &> /dev/null; then
    echo "Error: Rust/Cargo not found. Please install Rust from https://rustup.rs/"
    exit 1
fi

# Check if wasm32v1-none target is installed
if ! rustup target list | grep -q "wasm32v1-none (installed)"; then
    echo "Installing wasm32v1-none target..."
    rustup target add wasm32v1-none
fi

# Create output directory for Java integration
OUTPUT_DIR="../xrpl4j-integration-tests/src/test/resources/wasm"
mkdir -p "$OUTPUT_DIR"

# List of all WASM functions to build
FUNCTIONS=(
    "always_succeed"
    "balance_check"
    "credential_check"
    "time_window"
    "data_counter"
    "oracle_price_check"
    "gas_stress_test"
)

# Build each function separately
for FUNCTION in "${FUNCTIONS[@]}"; do
    echo ""
    echo "========================================="
    echo "Building function: $FUNCTION"
    echo "========================================="

    # Build with the specific feature enabled
    cargo build --release --target wasm32v1-none --features "$FUNCTION" --no-default-features

    WASM_FILE="target/wasm32v1-none/release/smart_escrow_wasm_functions.wasm"

    if [ ! -f "$WASM_FILE" ]; then
        echo "Error: WASM file not found at $WASM_FILE"
        exit 1
    fi

    echo "Build successful: $WASM_FILE"
    ls -lh "$WASM_FILE"

    # Copy to output directory with function name
    cp "$WASM_FILE" "$OUTPUT_DIR/${FUNCTION}.wasm"
    echo "Copied to: $OUTPUT_DIR/${FUNCTION}.wasm"

    # Optimize with wasm-opt if available
    if command -v wasm-opt &> /dev/null; then
        echo "Optimizing with wasm-opt..."
        OPTIMIZED_FILE="$OUTPUT_DIR/${FUNCTION}_optimized.wasm"
        wasm-opt -Oz -o "$OPTIMIZED_FILE" "$WASM_FILE"
        echo "Optimized WASM: $OPTIMIZED_FILE"
        ls -lh "$OPTIMIZED_FILE"
    fi
done

if ! command -v wasm-opt &> /dev/null; then
    echo ""
    echo "Note: wasm-opt not found. Install binaryen for smaller WASM files."
    echo "  macOS: brew install binaryen"
    echo "  Ubuntu: apt-get install binaryen"
fi

echo ""
echo "========================================="
echo "Done! All WASM files built successfully:"
echo "========================================="
ls -lh "$OUTPUT_DIR"
