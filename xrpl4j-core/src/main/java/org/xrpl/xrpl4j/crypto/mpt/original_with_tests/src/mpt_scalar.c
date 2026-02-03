/**
 * @file mpt_scalar.c
 * @brief Scalar Field Arithmetic Abstraction Layer.
 *
 * This module provides a safe, portable interface for performing arithmetic
 * in the scalar field of the secp256k1 curve (integers modulo \f$ n \f$, the group order).
 *
 * @details
 * **Purpose:**
 * While `libsecp256k1` exposes point operations via its public API, it does not
 * typically expose low-level scalar arithmetic. However, protocols like Bulletproofs
 * and ElGamal require extensive scalar math (e.g., polynomial evaluation, inner products)
 * to be performed by the client.
 *
 * **Implementation:**
 * This file includes internal `libsecp256k1` headers (`scalar.h`, `scalar_impl.h`)
 * to access the optimized, constant-time scalar implementations.
 *
 * **Operations:**
 * All operations are performed modulo the curve order \f$ n \f$:
 * - Addition: \f$ a + b \pmod{n} \f$
 * - Multiplication: \f$ a \cdot b \pmod{n} \f$
 * - Inversion: \f$ a^{-1} \pmod{n} \f$
 * - Negation: \f$ -a \pmod{n} \f$
 *
 * **Platform Specifics:**
 * Includes logic for 128-bit integer support (`int128.h`) required for efficient
 * computation on modern architectures (e.g., ARM64/Apple Silicon).
 *
 * @warning These functions operate on 32-byte big-endian scalars. Inputs must be
 * properly reduced or handled by `secp256k1_mpt_scalar_reduce32` before use if they
 * might exceed \f$ n \f$.
 */

#include "secp256k1_mpt.h"
#include <string.h>
#include <openssl/crypto.h>

/* 1. Backend Configuration Definitions */
#ifndef USE_SCALAR_8X32
#define USE_SCALAR_8X32
#endif
#ifndef USE_FIELD_10X26
#define USE_FIELD_10X26
#endif

/* 2. Include low-level utilities first.
      On ARM64/Apple Silicon, the scalar math depends on 128-bit
      integer helpers defined in these headers. */
#include "util.h"
#include "int128.h"
#include "int128_impl.h"

/* 3. Include the actual scalar implementations */
#include "scalar.h"
#include "scalar_impl.h"

/* --- Implementation --- */

void secp256k1_mpt_scalar_add(unsigned char *res, const unsigned char *a, const unsigned char *b) {
    secp256k1_scalar s_res, s_a, s_b;
    secp256k1_scalar_set_b32(&s_a, a, NULL);
    secp256k1_scalar_set_b32(&s_b, b, NULL);
    secp256k1_scalar_add(&s_res, &s_a, &s_b);
    secp256k1_scalar_get_b32(res, &s_res);

    /* SECURE CLEANUP */
    OPENSSL_cleanse(&s_a, sizeof(s_a));
    OPENSSL_cleanse(&s_b, sizeof(s_b));
    OPENSSL_cleanse(&s_res, sizeof(s_res));
}

void secp256k1_mpt_scalar_mul(unsigned char *res, const unsigned char *a, const unsigned char *b) {
    secp256k1_scalar s_res, s_a, s_b;
    secp256k1_scalar_set_b32(&s_a, a, NULL);
    secp256k1_scalar_set_b32(&s_b, b, NULL);
    secp256k1_scalar_mul(&s_res, &s_a, &s_b);
    secp256k1_scalar_get_b32(res, &s_res);

    /* SECURE CLEANUP */
    OPENSSL_cleanse(&s_a, sizeof(s_a));
    OPENSSL_cleanse(&s_b, sizeof(s_b));
    OPENSSL_cleanse(&s_res, sizeof(s_res));
}

void secp256k1_mpt_scalar_inverse(unsigned char *res, const unsigned char *in) {
    secp256k1_scalar s;
    secp256k1_scalar_set_b32(&s, in, NULL);
    secp256k1_scalar_inverse(&s, &s);
    secp256k1_scalar_get_b32(res, &s);

    /* SECURE CLEANUP */
    OPENSSL_cleanse(&s, sizeof(s));
}

void secp256k1_mpt_scalar_negate(unsigned char *res, const unsigned char *in) {
    secp256k1_scalar s;
    secp256k1_scalar_set_b32(&s, in, NULL);
    secp256k1_scalar_negate(&s, &s);
    secp256k1_scalar_get_b32(res, &s);

    /* SECURE CLEANUP */
    OPENSSL_cleanse(&s, sizeof(s));
}

void secp256k1_mpt_scalar_reduce32(unsigned char out32[32], const unsigned char in32[32]) {
    secp256k1_scalar s;
    secp256k1_scalar_set_b32(&s, in32, NULL);
    secp256k1_scalar_get_b32(out32, &s);

    /* SECURE CLEANUP */
    OPENSSL_cleanse(&s, sizeof(s));
}
