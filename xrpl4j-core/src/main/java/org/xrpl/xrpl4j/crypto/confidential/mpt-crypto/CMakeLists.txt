cmake_minimum_required(VERSION 3.16)
project(mpt-crypto C CXX)

# --- Find Dependencies ---
find_package(OpenSSL REQUIRED)
find_package(secp256k1 REQUIRED)

# --- Define The Library ---
add_library(mpt-crypto
            src/bulletproof_aggregated.c
            src/commitments.c
            src/elgamal.c
            src/equality_proof.c
            src/mpt_scalar.c
            src/proof_link.c
            src/proof_pok_sk.c
            src/proof_same_plaintext.c
            src/proof_same_plaintext_multi.c
            src/proof_same_plaintext_multi_shared_r.c
            src/utility/mpt_utility.cpp)

# --- Set Include Directories ---
target_include_directories(mpt-crypto PUBLIC include)

# --- Set Compile Definitions ---
include(CheckTypeSize)
# Check if the compiler supports __int128 (required for the optimized 64-bit code)
check_type_size("__int128" HAVE___INT128)

if (HAVE___INT128)
    message(STATUS "Build: Detected 128-bit integer support.")
    target_compile_definitions(mpt-crypto PRIVATE SECP256K1_WIDEMUL_INT128)
else ()
    message(STATUS "Build: No 128-bit support detected.")
    target_compile_definitions(mpt-crypto PRIVATE SECP256K1_WIDEMUL_INT64)
endif ()

# --- Link Dependencies ---
target_link_libraries(mpt-crypto PUBLIC secp256k1::secp256k1 OpenSSL::Crypto)

# --- Testing ---
option(ENABLE_TESTS "Enable building tests" ON)
if (ENABLE_TESTS)
    enable_testing()
    add_subdirectory(tests)
endif ()
