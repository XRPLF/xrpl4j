#!/usr/bin/env bash
#
# build-mpt-crypto.sh
#
# Builds the mpt-crypto C library as a shared library (.dylib / .so / .dll)
# with its dependencies (secp256k1, OpenSSL) statically linked in, so the
# resulting binary is self-contained and can be loaded by JNA without any
# system-level installs.
#
# Usage:
#   ./scripts/build-mpt-crypto.sh                          # build from default repo, main branch
#   ./scripts/build-mpt-crypto.sh --ref v1.0.0             # build a specific tag
#   ./scripts/build-mpt-crypto.sh --ref abc123              # build a specific commit
#   ./scripts/build-mpt-crypto.sh --local /path/to/mpt-crypto  # build from a local checkout
#
# Prerequisites:
#   - Conan 2.x (for dependency management)
#   - CMake 3.16+ (for build configuration)
#   - Ninja (for fast builds)
#   - A C/C++ compiler (apple-clang on macOS, gcc/clang on Linux)
#
# Output:
#   The built shared library is copied to:
#     <xrpl4j-root>/scripts/build-output/<platform>/<library-file>
#   e.g. scripts/build-output/darwin-aarch64/libmptcrypto.dylib

set -euo pipefail

# ──────────────────────────────────────────────────────────────────────────────
# Configuration
# ──────────────────────────────────────────────────────────────────────────────

MPT_CRYPTO_REPO="https://github.com/XRPLF/mpt-crypto.git"
DEFAULT_REF="main"

# Where this script lives — used to resolve paths relative to the xrpl4j root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
XRPL4J_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BUILD_OUTPUT_DIR="${XRPL4J_ROOT}/scripts/build-output"

# Temporary directory for cloning and building (cleaned up on exit).
# All build artifacts go here — never inside the mpt-crypto source tree.
WORK_DIR=""
LOCAL_SOURCE=""
GIT_REF="${DEFAULT_REF}"

# ──────────────────────────────────────────────────────────────────────────────
# Parse arguments
# ──────────────────────────────────────────────────────────────────────────────

while [[ $# -gt 0 ]]; do
    case "$1" in
        --ref)
            GIT_REF="$2"
            shift 2
            ;;
        --local)
            LOCAL_SOURCE="$2"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1"
            echo "Usage: $0 [--ref <tag|commit>] [--local <path>]"
            exit 1
            ;;
    esac
done

# ──────────────────────────────────────────────────────────────────────────────
# Detect platform — JNA uses these names to locate native libraries at runtime.
# The naming convention follows JNA's Platform class:
#   darwin-aarch64, darwin-x86-64, linux-x86-64, linux-aarch64, win32-x86-64
# ──────────────────────────────────────────────────────────────────────────────

detect_platform() {
    local os arch

    case "$(uname -s)" in
        Darwin) os="darwin" ;;
        Linux)  os="linux" ;;
        MINGW*|MSYS*|CYGWIN*) os="win32" ;;
        *)
            echo "ERROR: Unsupported OS: $(uname -s)"
            exit 1
            ;;
    esac

    case "$(uname -m)" in
        arm64|aarch64) arch="aarch64" ;;
        x86_64|amd64)  arch="x86-64" ;;
        *)
            echo "ERROR: Unsupported architecture: $(uname -m)"
            exit 1
            ;;
    esac

    echo "${os}-${arch}"
}

PLATFORM="$(detect_platform)"
echo "==> Detected platform: ${PLATFORM}"

# ──────────────────────────────────────────────────────────────────────────────
# Determine the shared library file name for this OS.
# macOS: libmptcrypto.dylib, Linux: libmptcrypto.so, Windows: mptcrypto.dll
# ──────────────────────────────────────────────────────────────────────────────

case "${PLATFORM}" in
    darwin-*) LIB_FILENAME="libmptcrypto.dylib" ;;
    linux-*)  LIB_FILENAME="libmptcrypto.so" ;;
    win32-*)  LIB_FILENAME="mptcrypto.dll" ;;
esac

# ──────────────────────────────────────────────────────────────────────────────
# Get the mpt-crypto source code.
# Either use a local checkout (--local) or clone from GitHub at the given ref.
# ──────────────────────────────────────────────────────────────────────────────

# Always create a temp working directory for build artifacts.
# This keeps the mpt-crypto source tree completely clean — no build files
# are written into it, whether it's a local checkout or a fresh clone.
WORK_DIR="$(mktemp -d)"
trap 'echo "==> Cleaning up temp dir: ${WORK_DIR}"; rm -rf "${WORK_DIR}"' EXIT
echo "==> Using temp work dir: ${WORK_DIR}"

if [[ -n "${LOCAL_SOURCE}" ]]; then
    # Use an existing local checkout — no clone needed
    SOURCE_DIR="${LOCAL_SOURCE}"
    echo "==> Using local source: ${SOURCE_DIR}"
else
    echo "==> Cloning mpt-crypto at ref '${GIT_REF}' into temp dir..."
    git clone --depth 1 --branch "${GIT_REF}" "${MPT_CRYPTO_REPO}" "${WORK_DIR}/mpt-crypto" 2>/dev/null || {
        # --branch doesn't work with arbitrary commit SHAs, so fall back to
        # a full clone + checkout
        echo "    (ref is not a branch/tag — doing full clone + checkout)"
        git clone "${MPT_CRYPTO_REPO}" "${WORK_DIR}/mpt-crypto"
        git -C "${WORK_DIR}/mpt-crypto" checkout "${GIT_REF}"
    }
    SOURCE_DIR="${WORK_DIR}/mpt-crypto"
fi

# ──────────────────────────────────────────────────────────────────────────────
# Create a Conan profile for this build.
#
# We detect the compiler and write a profile that matches the current system.
# This avoids depending on profiles checked into the mpt-crypto repo (which
# may not exist for all platforms).
# ──────────────────────────────────────────────────────────────────────────────

# Build directory lives in the temp work dir, NOT inside the source tree.
# This ensures --local builds leave zero artifacts in the user's checkout.
BUILD_DIR="${WORK_DIR}/build"
PROFILE_PATH="${BUILD_DIR}/conan-profile"
mkdir -p "${BUILD_DIR}"

generate_conan_profile() {
    local compiler compiler_version cppstd libcxx os_name

    case "$(uname -s)" in
        Darwin)
            compiler="apple-clang"
            # Extract major.minor version from apple clang
            compiler_version="$(clang --version | head -1 | sed -E 's/.*version ([0-9]+\.[0-9]+).*/\1/')"
            cppstd="20"
            libcxx="libc++"
            os_name="Macos"
            ;;
        Linux)
            if command -v gcc &>/dev/null; then
                compiler="gcc"
                compiler_version="$(gcc -dumpversion)"
                cppstd="20"
                libcxx="libstdc++11"
                os_name="Linux"
            elif command -v clang &>/dev/null; then
                compiler="clang"
                compiler_version="$(clang --version | head -1 | sed -E 's/.*version ([0-9]+).*/\1/')"
                cppstd="20"
                libcxx="libc++"
                os_name="Linux"
            else
                echo "ERROR: No supported compiler found (gcc or clang)"
                exit 1
            fi
            ;;
        *)
            echo "ERROR: Profile generation not supported for $(uname -s) yet"
            exit 1
            ;;
    esac

    cat > "${PROFILE_PATH}" <<EOF
[settings]
arch=$(uname -m | sed 's/arm64/armv8/')
build_type=Release
compiler=${compiler}
compiler.cppstd=${cppstd}
compiler.libcxx=${libcxx}
compiler.version=${compiler_version}
os=${os_name}
EOF

    echo "==> Generated Conan profile at ${PROFILE_PATH}:"
    cat "${PROFILE_PATH}"
}

generate_conan_profile

# ──────────────────────────────────────────────────────────────────────────────
# Ensure the XRPLF Conan remote is configured.
#
# The mpt-crypto dependencies (secp256k1) are hosted on Ripple's own Conan
# remote, not the public conan-center. We add it if it's not already present.
# ──────────────────────────────────────────────────────────────────────────────

if ! conan remote list | grep -q "xrplf"; then
    echo "==> Adding XRPLF Conan remote..."
    conan remote add --index 0 xrplf https://conan.ripplex.io
else
    echo "==> XRPLF Conan remote already configured"
fi

# ──────────────────────────────────────────────────────────────────────────────
# Step 1: Install dependencies via Conan.
#
# Key flags:
#   -o "&:shared=True"           → Build mpt-crypto itself as a SHARED library.
#                                   The "&:" prefix means "this package" (the consumer).
#                                   Conan's CMakeToolchain will set BUILD_SHARED_LIBS=ON.
#
#   -o "secp256k1/*:shared=False" → Build secp256k1 as a STATIC library so it gets
#   -o "openssl/*:shared=False"     linked INTO the mpt-crypto shared lib. This makes
#                                    the .dylib/.so self-contained.
#
#   -o "secp256k1/*:fPIC=True"   → Ensure static dependencies are compiled with
#   -o "openssl/*:fPIC=True"       Position Independent Code (-fPIC). This is required
#                                   when linking static libs into a shared lib.
#
#   -b missing                   → Build any dependency from source if a pre-built
#                                   binary isn't available on the Conan remote.
# ──────────────────────────────────────────────────────────────────────────────

echo ""
echo "==> Installing dependencies via Conan..."
cd "${SOURCE_DIR}"

conan install . \
    -of "${BUILD_DIR}" \
    -b missing \
    -s "build_type=Release" \
    -o "&:shared=True" \
    -o "&:tests=False" \
    -o "secp256k1/*:shared=False" \
    -o "secp256k1/*:fPIC=True" \
    -o "openssl/*:shared=False" \
    -o "openssl/*:fPIC=True" \
    --profile:all "${PROFILE_PATH}"

# ──────────────────────────────────────────────────────────────────────────────
# Step 2: Configure CMake.
#
# The Conan toolchain file (conan_toolchain.cmake) sets up:
#   - CMAKE_BUILD_TYPE=Release
#   - BUILD_SHARED_LIBS=ON (because we passed shared=True)
#   - Find-package paths so CMake can locate secp256k1 and OpenSSL
#
# We use Ninja as the build system for faster builds.
#
# Note: The conanfile.py uses cmake_layout() which puts generators under
# <output-folder>/build/generators/, not <output-folder>/generators/.
# ──────────────────────────────────────────────────────────────────────────────

echo ""
echo "==> Configuring CMake..."

# Locate the generated toolchain file — Conan's cmake_layout may place it
# in different subdirectories depending on the conanfile.py configuration
TOOLCHAIN_FILE=$(find "${BUILD_DIR}" -name "conan_toolchain.cmake" -print -quit)
if [[ -z "${TOOLCHAIN_FILE}" ]]; then
    echo "ERROR: Could not find conan_toolchain.cmake under ${BUILD_DIR}"
    exit 1
fi
echo "    Using toolchain: ${TOOLCHAIN_FILE}"

cmake -B "${BUILD_DIR}" -S "${SOURCE_DIR}" \
    -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE:FILEPATH="${TOOLCHAIN_FILE}" \
    -DCMAKE_BUILD_TYPE=Release

# ──────────────────────────────────────────────────────────────────────────────
# Step 3: Build the shared library.
# ──────────────────────────────────────────────────────────────────────────────

echo ""
echo "==> Building mpt-crypto shared library..."
cmake --build "${BUILD_DIR}" --config Release

# ──────────────────────────────────────────────────────────────────────────────
# Step 4: Find and copy the built shared library to the output directory.
#
# The library name from CMake is "mpt-crypto" (with a hyphen), which produces:
#   macOS:   libmpt-crypto.dylib
#   Linux:   libmpt-crypto.so
#   Windows: mpt-crypto.dll
#
# We rename it to "mptcrypto" (no hyphen) because JNA uses Native.load("mptcrypto")
# and hyphens in library names can cause issues with JNA's naming conventions.
# ──────────────────────────────────────────────────────────────────────────────

echo ""
echo "==> Locating built library..."

# Find the actual built library file (name has hyphen from CMake target name)
BUILT_LIB=$(find "${BUILD_DIR}" -maxdepth 1 \( \
    -name "libmpt-crypto.dylib" -o \
    -name "libmpt-crypto.so" -o \
    -name "mpt-crypto.dll" \
    \) -print -quit)

if [[ -z "${BUILT_LIB}" ]]; then
    echo "ERROR: Could not find built shared library in ${BUILD_DIR}"
    echo "Contents of build dir:"
    ls -la "${BUILD_DIR}"
    exit 1
fi

echo "    Found: ${BUILT_LIB}"

# Create the platform-specific output directory and copy with the clean name
OUTPUT_DIR="${BUILD_OUTPUT_DIR}/${PLATFORM}"
mkdir -p "${OUTPUT_DIR}"
cp "${BUILT_LIB}" "${OUTPUT_DIR}/${LIB_FILENAME}"

echo ""
echo "==> Build complete!"
echo "    Library: ${OUTPUT_DIR}/${LIB_FILENAME}"
echo "    Platform: ${PLATFORM}"

# Show library details — helpful for verifying the build
echo ""
echo "==> Library details:"
case "${PLATFORM}" in
    darwin-*)
        # Show linked libraries — should NOT list secp256k1 or libcrypto as
        # external dylibs, confirming they're statically linked in
        echo "    Linked libraries (should only show system libs):"
        otool -L "${OUTPUT_DIR}/${LIB_FILENAME}" | tail -n +2 | sed 's/^/      /'
        echo ""
        echo "    File size: $(du -h "${OUTPUT_DIR}/${LIB_FILENAME}" | cut -f1)"
        ;;
    linux-*)
        echo "    Linked libraries (should only show system libs):"
        ldd "${OUTPUT_DIR}/${LIB_FILENAME}" 2>/dev/null | sed 's/^/      /' || true
        echo ""
        echo "    File size: $(du -h "${OUTPUT_DIR}/${LIB_FILENAME}" | cut -f1)"
        ;;
esac
