#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <secp256k1.h>


#define N_BITS 64



/**
 * Generates a  secure 32-byte scalar (private key).
 * NOTE: This is a TEMPORARY duplication of a helper that will be moved to proof_util.c.
 * Returns 1 on success, 0 on failure.
 */
static int generate_random_scalar(
        const secp256k1_context* ctx,
        unsigned char* scalar_bytes)
{
    do {
        if (RAND_bytes(scalar_bytes, 32) != 1) {
            return 0; // Randomness failure
        }
    } while (secp256k1_ec_seckey_verify(ctx, scalar_bytes) != 1);
    return 1;
}

// --- End of temporary function ---

static int compute_amount_point(const secp256k1_context* ctx, secp256k1_pubkey* mG, uint64_t amount);


/*
================================================================================
|                          INTERNAL HELPER FUNCTIONS                           |
================================================================================
*/

/* --- The H_point is now dynamic (Pk_base) and passed as an argument --- */

/**
 * Computes the modular sum of a vector of 32-byte scalars (for A/S commitment).
 */
int secp256k1_bulletproof_sum_scalar_vector(
        const secp256k1_context* ctx,
        unsigned char* sum_out,
        const unsigned char vector[][32],
        size_t N)
{
    size_t i;
    memset(sum_out, 0, 32);
    for (i = 0; i < N; ++i) {
        if (secp256k1_ec_seckey_tweak_add(ctx, sum_out, (const unsigned char*)vector[i]) != 1) {
            return 0;
        }
    }
    return 1;
}

/**
 * Computes the point M = amount * G. (Needed by commitment helper).
 */
int compute_amount_point(
        const secp256k1_context* ctx,
        secp256k1_pubkey* mG,
        uint64_t amount)
{
    unsigned char amount_scalar[32] = {0};
    assert(amount != 0);

    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    return secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
}

/**
 * Computes the modular dot product c = <a, b> = sum(a[i] * b[i]) mod q.
 * This function calculates the inner product of two scalar vectors.
 * ctx       The context.
 * out       Output 32-byte scalar (the inner product result).
 * a         Input scalar vector A (n * 32 bytes).
 * b         Input scalar vector B (n * 32 bytes).
 * n         The length of the vectors.
 * 1 on success, 0 on failure.
 */
int secp256k1_bulletproof_ipa_dot(
        const secp256k1_context* ctx,
        unsigned char* out,
        const unsigned char* a,
        const unsigned char* b,
        size_t n)
{
    unsigned char acc[32]; // Accumulator for the sum
    unsigned char term[32]; // Temporary product (a[i] * b[i])
    size_t i;
    int all_ok = 1;

    // Initialize accumulator to zero
    memset(acc, 0, 32);

    for (i = 0; i < n; ++i) {
        /* 1. Compute term = a[i] * b[i] mod q */
        // Copy a[i] into 'term' (the first operand to be modified)
        memcpy(term, a + i * 32, 32);

        // Multiply term by b[i] using the tweak function
        if (secp256k1_ec_seckey_tweak_mul(ctx, term, b + i * 32) != 1) {
            all_ok = 0;
            break;
        }

        /* 2. Accumulate acc = acc + term mod q */
        // Add the product 'term' to the accumulator 'acc'
        if (secp256k1_ec_seckey_tweak_add(ctx, acc, term) != 1) {
            all_ok = 0;
            break;
        }
    }
    // Copy the final accumulator result to the output buffer
    memcpy(out, acc, 32);

    return all_ok;
}

// NOTE: We need this helper for multiscalar multiplication function below

int secp256k1_bulletproof_add_point_to_accumulator(
    const secp256k1_context* ctx,
    secp256k1_pubkey* acc,
    const secp256k1_pubkey* term)
{
    const secp256k1_pubkey* points[2] = {acc, term};
    secp256k1_pubkey temp_sum;

    if (secp256k1_ec_pubkey_combine(ctx, &temp_sum, points, 2) != 1) return 0;
    *acc = temp_sum;
    return 1;
}

/**
 * Computes Multiscalar Multiplication (MSM): R = sum(s[i] * P[i]).
 * ctx       The context.
 * r_out     Output point (the sum R).
 * points    Array of N input points (secp256k1_pubkey).
 * scalars   Flat array of N 32-byte scalars.
 * n         The number of terms (N).
 * return    1 on success, 0 on failure.
 */
int secp256k1_bulletproof_ipa_msm(
        const secp256k1_context* ctx,
        secp256k1_pubkey* r_out,
        const secp256k1_pubkey* points,
        const unsigned char* scalars,
        size_t n
) {
    secp256k1_pubkey acc; // Accumulator
    secp256k1_pubkey current_term;
    size_t i;
    int all_ok = 1;

    // --- Initialization: Compute the first term (i=0) ---
    current_term = points[0];
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &current_term, scalars) != 1) return 0;
    acc = current_term;

    // --- Accumulation Loop (starting from i=1) ---
    for (i = 1; i < n; ++i) {
        // 1. Compute the current term T_i = scalars[i] * points[i]
        current_term = points[i];
        if (secp256k1_ec_pubkey_tweak_mul(ctx, &current_term, scalars + i * 32) != 1) {
            all_ok = 0; break;
        }

        // 2. Accumulate: acc = acc + T_i (using a helper to prevent aliasing)
        if (secp256k1_bulletproof_add_point_to_accumulator(ctx, &acc, &current_term) != 1) {
            all_ok = 0; break;
        }
    }

    *r_out = acc;
    return all_ok;
}


/**
 * Helper to compute Point = Scalar * Point (using public API)
 */
int secp256k1_bulletproof_point_scalar_mul(
        const secp256k1_context* ctx,
        secp256k1_pubkey* r_out,
        const secp256k1_pubkey* p_in,
        const unsigned char* s_scalar)
{
    *r_out = *p_in;
    return secp256k1_ec_pubkey_tweak_mul(ctx, r_out, s_scalar);
}

/**
 * Computes the cross-term commitments L and R.
 * L = <a_L, G_R> + <b_R, H_L> + c_L * ux * g
 * R = <a_R, G_L> + <b_L, H_R> + c_R * ux * g
 *
 * ctx       The context.
 * L         Output: Commitment point L_j.
 * R         Output: Commitment point R_j.
 * half_n    Length of the input vector halves.
 * g         The blinding generator point (Pk_base in our case).
 * return    1 on success, 0 on failure.
 */
int secp256k1_bulletproof_ipa_compute_LR(
        const secp256k1_context* ctx,
        secp256k1_pubkey* L, secp256k1_pubkey* R,
        const unsigned char* a_L, const unsigned char* a_R,
        const unsigned char* b_L, const unsigned char* b_R,
        const secp256k1_pubkey* G_L, const secp256k1_pubkey* G_R,
        const secp256k1_pubkey* H_L, const secp256k1_pubkey* H_R,
        const secp256k1_pubkey* g,
        const unsigned char* ux,
        size_t half_n
) {
    unsigned char c_L_scalar[32], c_R_scalar[32]; // Cross-term scalars
    unsigned char cL_ux_scalar[32], cR_ux_scalar[32]; // Blinding term scalars
    secp256k1_pubkey T1, T2; // Intermediate points
    const secp256k1_pubkey* points_to_add[2];
    int all_ok = 1;

    /* 1. Compute Cross-Term Scalars: c_L = <a_L, b_R>, c_R = <a_R, b_L> */
    if (!secp256k1_bulletproof_ipa_dot(ctx, c_L_scalar, a_L, b_R, half_n)) all_ok = 0;
    if (all_ok && !secp256k1_bulletproof_ipa_dot(ctx, c_R_scalar, a_R, b_L, half_n)) all_ok = 0;

    /* 2. Compute L: L = (<a_L, G_R>) + (<b_R, H_L>) + (c_L * ux * g) */
    if (all_ok && !secp256k1_bulletproof_ipa_msm(ctx, L, G_R, a_L, half_n)) all_ok = 0; // Term 1: <a_L, G_R>
    if (all_ok && !secp256k1_bulletproof_ipa_msm(ctx, &T1, H_L, b_R, half_n)) all_ok = 0; // Term 2: <b_R, H_L>
    if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, L, &T1)) all_ok = 0; // L = Term 1 + Term 2

    /* 3. Compute Blinding Term for L: c_L * ux * g */
    if (all_ok) {
        memcpy(cL_ux_scalar, c_L_scalar, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, cL_ux_scalar, ux)) all_ok = 0; // cL_ux = c_L * ux
        if (all_ok && !secp256k1_bulletproof_point_scalar_mul(ctx, &T2, g, cL_ux_scalar)) all_ok = 0; // T2 = cL_ux * g
        if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, L, &T2)) all_ok = 0; // L = L + T2
    }

    /* 4. Compute R: R = (<a_R, G_L>) + (<b_L, H_R>) + (c_R * ux * g) */
    if (all_ok && !secp256k1_bulletproof_ipa_msm(ctx, R, G_L, a_R, half_n)) all_ok = 0; // Term 1: <a_R, G_L>
    if (all_ok && !secp256k1_bulletproof_ipa_msm(ctx, &T1, H_R, b_L, half_n)) all_ok = 0; // Term 2: <b_L, H_R>
    if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, R, &T1)) all_ok = 0; // R = Term 1 + Term 2

    /* 5. Compute Blinding Term for R: c_R * ux * g */
    if (all_ok) {
        memcpy(cR_ux_scalar, c_R_scalar, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, cR_ux_scalar, ux)) all_ok = 0; // cR_ux = c_R * ux
        if (all_ok && !secp256k1_bulletproof_point_scalar_mul(ctx, &T2, g, cR_ux_scalar)) all_ok = 0; // T2 = cR_ux * g
        if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, R, &T2)) all_ok = 0; // R = R + T2
    }

    return all_ok;
}


/**
 * Executes one IPA compression step (the vector update).
 * This computes the new compressed vectors (a', b', G', H') and overwrites the
 * first half of the input arrays (in-place).
 *
 * ctx       The context.
 * a, b      IN/OUT: Scalar vectors (a and b).
 * G, H      IN/OUT: Generator vectors (G and H).
 * half_n    The length of the new, compressed vectors (N/2).
 * x         The challenge scalar x.
 * x_inv     The challenge scalar inverse x^-1.
 * return    1 on success, 0 on failure.
 */
int secp256k1_bulletproof_ipa_compress_step(
        const secp256k1_context* ctx,
        unsigned char* a,
        unsigned char* b,
        secp256k1_pubkey* G,
        secp256k1_pubkey* H,
        size_t half_n,
        const unsigned char* x,
        const unsigned char* x_inv
) {
    size_t i;
    int all_ok = 1;

    // Temporary variables for intermediate results
    unsigned char t1_scalar[32], t2_scalar[32];
    secp256k1_pubkey G_term, H_term;
    const secp256k1_pubkey* points_to_add[2];

    for (i = 0; i < half_n; ++i) {

        // --- SCALAR VECTORS: a'[i] = a[i] * x + a[i + half_n] * x_inv ---
        {
            unsigned char* a_L = a + i * 32;          // a[i]
            unsigned char* a_R = a + (i + half_n) * 32; // a[i + half_n]

            // t1_scalar = a_L * x
            memcpy(t1_scalar, a_L, 32);
            if (!secp256k1_ec_seckey_tweak_mul(ctx, t1_scalar, x)) all_ok = 0;

            // t2_scalar = a_R * x_inv
            memcpy(t2_scalar, a_R, 32);
            if (!secp256k1_ec_seckey_tweak_mul(ctx, t2_scalar, x_inv)) all_ok = 0;

            // a[i] = t1_scalar + t2_scalar (Done in-place on a[i])
            memcpy(a_L, t1_scalar, 32);
            if (!secp256k1_ec_seckey_tweak_add(ctx, a_L, t2_scalar)) all_ok = 0;
        }

        // --- SCALAR VECTORS: b'[i] = b[i] * x_inv + b[i + half_n] * x ---
        {
            unsigned char* b_L = b + i * 32;
            unsigned char* b_R = b + (i + half_n) * 32;

            // t1_scalar = b_L * x_inv
            memcpy(t1_scalar, b_L, 32);
            if (!secp256k1_ec_seckey_tweak_mul(ctx, t1_scalar, x_inv)) all_ok = 0;

            // t2_scalar = b_R * x
            memcpy(t2_scalar, b_R, 32);
            if (!secp256k1_ec_seckey_tweak_mul(ctx, t2_scalar, x)) all_ok = 0;

            // b[i] = t1_scalar + t2_scalar (Done in-place on b[i])
            memcpy(b_L, t1_scalar, 32);
            if (!secp256k1_ec_seckey_tweak_add(ctx, b_L, t2_scalar)) all_ok = 0;
        }

        // --- POINT VECTORS: G'[i] = G_L[i] * x_inv + G_R[i] * x ---
        {
            // G_term = G_L[i] * x_inv
            G_term = G[i];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &G_term, x_inv)) all_ok = 0;

            // H_term = G_R[i] * x
            H_term = G[i + half_n];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &H_term, x)) all_ok = 0;

            // G[i] = G_term + H_term (Done in-place on G[i])
            points_to_add[0] = &G_term;
            points_to_add[1] = &H_term;
            if (!secp256k1_bulletproof_add_point_to_accumulator(ctx, &G[i], &H_term)) all_ok = 0;
        }

        // --- POINT VECTORS: H'[i] = H_L[i] * x + H_R[i] * x_inv ---
        {
            // G_term = H_L[i] * x
            G_term = H[i];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &G_term, x)) all_ok = 0;

            // H_term = H_R[i] * x_inv
            H_term = H[i + half_n];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &H_term, x_inv)) all_ok = 0;

            // H[i] = G_term + H_term (Done in-place on H[i])
            points_to_add[0] = &G_term;
            points_to_add[1] = &H_term;
            if (!secp256k1_bulletproof_add_point_to_accumulator(ctx, &H[i], &H_term)) all_ok = 0;
        }

        if (!all_ok) break; // Break loop if any step failed
    }

    return all_ok;
}


/**
 * Derives a challenge scalar 'ux' (e.g., e, x, u_j) from a hash of inputs.
 * ux = H(commit_inp || dot) mod q.
 *
 * ctx           The context.
 * ux_out        Output 32-byte scalar (the challenge result).
 * commit_inp_32 Input 32-byte commitment/nonce input.
 * dot_32        Input 32-byte scalar representing the inner product.
 * return        1 on success, 0 on failure (if resulting scalar is zero or invalid).
 */
int secp256k1_bulletproof_ipa_derive_challenge(
        const secp256k1_context* ctx,
        unsigned char* ux_out,
        const unsigned char* commit_inp_32,
        const unsigned char* dot_32)
{
    unsigned char hash_input[64];
    unsigned char hash_output[32];

    /* 1. Prepare hash input: commit_inp || dot */
    memcpy(hash_input, commit_inp_32, 32);
    memcpy(hash_input + 32, dot_32, 32);

    /* 2. Compute hash (SHA256) */
    SHA256(hash_input, 64, hash_output);

    /* 3. Convert hash output to scalar ux and verify */
    memcpy(ux_out, hash_output, 32);

    /* Check if the resulting scalar is a valid private key (non-zero and < curve order).
       This acts as our modulus reduction and zero check. */
    if (secp256k1_ec_seckey_verify(ctx, ux_out) != 1) {
        return 0;
    }
    return 1;
}

/**
 * Executes the core recursive Inner Product Argument (IPA) Prover.
 * This function iteratively compresses the scalar and generator vectors down to
 * the final two scalars (a_final, b_final), while recording the L/R proof points.
 *
 * ctx           The context.
 * g             The special blinding generator point (Pk_recipient).
 * G_vec, H_vec  IN/OUT: Generator vectors (compressed in-place).
 * a_vec, b_vec  IN/OUT: Scalar vectors (compressed in-place).
 * n             The starting length of the vectors (must be power of two, e.g., 64).
 * commit_inp    32-byte initial commitment input for the transcript.
 * dot_out       Output: The final initial inner product <a,b>.
 * L_out, R_out  Output: Arrays to store the log2(n) L/R proof points.
 * a_final, b_final Output: The final scalar components.
 * return        1 on success, 0 on failure.
 */
int secp256k1_bulletproof_run_ipa_prover(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* g,
        secp256k1_pubkey* G_vec,
        secp256k1_pubkey* H_vec,
        unsigned char* a_vec,
        unsigned char* b_vec,
        size_t n,
        const unsigned char commit_inp[32],
        unsigned char* dot_out,
        secp256k1_pubkey* L_out,
        secp256k1_pubkey* R_out,
        unsigned char* a_final,
        unsigned char* b_final
) {
    size_t rounds = 0;
    size_t cur_n;
    unsigned char current_dot[32]; // Temporary dot product
    unsigned char x_scalar[32], x_inv[32];
    unsigned char Lr_bytes[33], Rr_bytes[33];
    int all_ok = 1;
    size_t len;

    /* 1. Initial Checks and Round Count */
    if (n == 0 || (n & (n - 1)) != 0) return 0; /* n must be power of two */

    cur_n = n;
    while (cur_n > 1) {
        cur_n >>= 1;
        rounds++;
    }
    cur_n = n; // Reset cur_n to original n

    /* 2. Compute Initial Dot Product (for Protocol 2 Challenge) */
    if (!secp256k1_bulletproof_ipa_dot(ctx, current_dot, a_vec, b_vec, n)) return 0;
    memcpy(dot_out, current_dot, 32); // Save initial dot product

    /* 3. Compute Initial Challenge ux = H(commit_inp || dot_out) */
    if (!secp256k1_bulletproof_ipa_derive_challenge(ctx, x_scalar, commit_inp, dot_out)) return 0;

    /* 4. Recursive Protocol 1 IPA Loop */
    for (size_t r = 0; r < rounds; ++r) {
        size_t half_n = cur_n >> 1;
        secp256k1_pubkey Lr, Rr;

        /* 4a. Compute Lr, Rr commitments (Cross-term commitments) */
        if (!secp256k1_bulletproof_ipa_compute_LR(
                ctx, &Lr, &Rr,
                a_vec, a_vec + half_n * 32,
                b_vec, b_vec + half_n * 32,
                G_vec, G_vec + half_n,
                H_vec, H_vec + half_n,
                g, x_scalar, half_n
        )) return 0;

        /* 4b. Store Lr and Rr outputs */
        L_out[r] = Lr;
        R_out[r] = Rr;

        /* 4c. Generate next round challenge x_r */
        // x_r = H(Transcript || Lr || Rr)

        // Serialize Lr and Rr for hashing
        len = 33; secp256k1_ec_pubkey_serialize(ctx, Lr_bytes, &len, &Lr, SECP256K1_EC_COMPRESSED);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, Rr_bytes, &len, &Rr, SECP256K1_EC_COMPRESSED);

        // NOTE: A proper transcript update routine would hash all previous L/R pairs.
        // Here we simulate the challenge derivation using a temporary hash.
        // We will reuse the initial commit_inp as a transcript ID placeholder.

        // PSEUDOCODE for challenge update:
        // hash_update_with_L_R(transcript, Lr_bytes, Rr_bytes);
        // x_scalar = H(transcript)

        // For structural integrity, we simulate x_scalar and compute inverse.
        if (!generate_random_scalar(ctx, x_scalar)) return 0; /* Simulate fresh challenge */

        // 4d. Compute x_inv (Placeholder for modular inverse function)
        memcpy(x_inv, x_scalar, 32); /* Placeholder for x_inv = 1/x */

        /* 4e. Compress vectors and generators in-place */
        if (!secp256k1_bulletproof_ipa_compress_step(
                ctx, a_vec, b_vec, G_vec, H_vec, half_n, x_scalar, x_inv
        )) return 0;

        cur_n = half_n;
    }

    /* 5. Final Result */
    // The reduced vectors (a_vec, b_vec, G_vec, H_vec) are now length 1.
    // The final components are the first element of the scalar arrays.
    memcpy(a_final, a_vec, 32);
    memcpy(b_final, b_vec, 32);

    return 1;
}





// NOTE: All other internal helpers (compute_vectors, commit_AS, etc.)
// must be updated to accept and pass the Pk_base point.

/*
================================================================================
|                       PUBLIC API IMPLEMENTATIONS                             |
================================================================================
*/

/**
 * Phase 1, Step 3: Computes the four required scalar vectors.
 */
int secp256k1_bulletproof_compute_vectors(
        const secp256k1_context* ctx,
        uint64_t value,
        unsigned char al[N_BITS][32],
        unsigned char ar[N_BITS][32],
        unsigned char sl[N_BITS][32],
        unsigned char sr[N_BITS][32])
{

    size_t i;
    unsigned char current_bit;
    int all_ok = 1;
    const unsigned char N_MINUS_ONE_SCALAR[32] = {
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE,
            0xBA, 0xAE, 0xDC, 0xE6, 0xAF, 0x48, 0xA0, 0x3B,
            0xBF, 0xEF, 0x90, 0xF0, 0xD2, 0xEB, 0xF9, 0x99
    };
    unsigned char one_scalar[32] = {0};
    one_scalar[31] = 0x01;
    unsigned char zero_scalar[32] = {0};

    // Assumes generate_random_scalar is available.

    /* 1. Encode value 'v' into a_l and a_r */
    for (i = 0; i < N_BITS; ++i) {
        current_bit = (value >> i) & 1; // Extract the i-th bit

        if (current_bit == 1) {
            // If bit is 1: a_l[i] = 1, a_r[i] = 0
            memcpy(al[i], one_scalar, 32);
            memcpy(ar[i], zero_scalar, 32);
        } else {
            // If bit is 0: a_l[i] = 0, a_r[i] = -1 (mod q)
            memcpy(al[i], zero_scalar, 32);
            memcpy(ar[i], N_MINUS_ONE_SCALAR, 32);
        }
    }

    /* 2. Generate random auxiliary scalars s_l and s_r */
    for (i = 0; i < N_BITS; ++i) {
        if (!generate_random_scalar(ctx, sl[i])) all_ok = 0;
        if (!generate_random_scalar(ctx, sr[i])) all_ok = 0;
    }

    return all_ok;
}

/**
 * Phase 1, Step 4: Computes the initial Commitment Points A and S.
 * A = (al_sum + rho) * G + ar_sum * Pk_base
 * S = (sl_sum + rho_s) * G + sr_sum * Pk_base
 *
 *  ctx   The context.
 *  A     Output: Commitment point A.
 *  S_cmt Output: Commitment point S.
 *  al..sr Input vectors (64x32 bytes).
 *  rho, rho_s Input: Random blinding scalars.
 *  pk_base Input: Dynamic generator Pk (used as H).
 * return 1 on success, 0 on failure.
 */
int secp256k1_bulletproof_commit_AS(
        const secp256k1_context* ctx,
        secp256k1_pubkey* A, secp256k1_pubkey* S_cmt,
        unsigned char al[64][32], unsigned char ar[64][32],
        unsigned char sl[64][32], unsigned char sr[64][32],
        const unsigned char* rho, const unsigned char* rho_s,
        const secp256k1_pubkey* pk_base)
{
    /* C90 Declarations */
    const size_t N = 64;
    int all_ok = 1;

    // Scalars for G base
    unsigned char al_sum[32], sl_sum[32]; // Sum of left vectors
    unsigned char G_term_scalar[32]; // (al_sum + rho) or (sl_sum + rho_s)

    // Scalars for Pk base
    unsigned char ar_sum[32], sr_sum[32]; // Sum of right vectors
    unsigned char H_term_scalar[32]; // ar_sum or sr_sum

    // Point components
    secp256k1_pubkey G_term_point, H_term_point;
    const secp256k1_pubkey* points_to_add[2];

    /* 1. Sum Vectors */
    if (!secp256k1_bulletproof_sum_scalar_vector(ctx, al_sum, al, N)) all_ok = 0;
    if (all_ok && !secp256k1_bulletproof_sum_scalar_vector(ctx, ar_sum, ar, N)) all_ok = 0;
    if (all_ok && !secp256k1_bulletproof_sum_scalar_vector(ctx, sl_sum, sl, N)) all_ok = 0;
    if (all_ok && !secp256k1_bulletproof_sum_scalar_vector(ctx, sr_sum, sr, N)) all_ok = 0;

    if (!all_ok) return 0;

    /* --- Compute Commitment A --- */

    // 2a. Compute G_scalar = al_sum + rho
    memcpy(G_term_scalar, al_sum, 32);
    if (!secp256k1_ec_seckey_tweak_add(ctx, G_term_scalar, rho)) all_ok = 0; // G_scalar = al_sum + rho

    // 2b. Compute H_scalar = ar_sum (no addition needed)
    memcpy(H_term_scalar, ar_sum, 32);

    // 2c. Compute A_G_term = G_scalar * G
    if (all_ok && !secp256k1_ec_pubkey_create(ctx, &G_term_point, G_term_scalar)) all_ok = 0;

    // 2d. Compute A_H_term = H_scalar * Pk_base
    H_term_point = *pk_base;
    if (all_ok && !secp256k1_ec_pubkey_tweak_mul(ctx, &H_term_point, H_term_scalar)) all_ok = 0;

    // 2e. A = A_G_term + A_H_term
    points_to_add[0] = &G_term_point;
    points_to_add[1] = &H_term_point;
    if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, A, &H_term_point)) all_ok = 0;


    /* --- Compute Commitment S --- */

    // 3a. Compute G_scalar = sl_sum + rho_s
    memcpy(G_term_scalar, sl_sum, 32);
    if (all_ok && !secp256k1_ec_seckey_tweak_add(ctx, G_term_scalar, rho_s)) all_ok = 0; // G_scalar = sl_sum + rho_s

    // 3b. Compute H_scalar = sr_sum (no addition needed)
    memcpy(H_term_scalar, sr_sum, 32);

    // 3c. Compute S_G_term = G_scalar * G
    if (all_ok && !secp256k1_ec_pubkey_create(ctx, &G_term_point, G_term_scalar)) all_ok = 0;

    // 3d. Compute S_H_term = H_scalar * Pk_base
    H_term_point = *pk_base;
    if (all_ok && !secp256k1_ec_pubkey_tweak_mul(ctx, &H_term_point, H_term_scalar)) all_ok = 0;

    // 3e. S = S_G_term + S_H_term
    points_to_add[0] = &G_term_point;
    points_to_add[1] = &H_term_point;
    if (all_ok && !secp256k1_bulletproof_add_point_to_accumulator(ctx, S_cmt, &H_term_point)) all_ok = 0;

    return all_ok;
}
/**
 * Computes the Pedersen Commitment: C = value*G + blinding_factor*Pk_base.
 */
int secp256k1_bulletproof_create_commitment(
        const secp256k1_context* ctx,
        secp256k1_pubkey* commitment_C,
        uint64_t value,
        const unsigned char* blinding_factor,
        const secp256k1_pubkey* pk_base
) {

    unsigned char v_scalar[32] = {0};
    secp256k1_pubkey G_term, Pk_term;
    const secp256k1_pubkey* points_to_add[2];

    if (value == 0) return 0; /* Commitment must be to a non-zero value */

    /* 1. Convert value to scalar (v*G) */
    if (!compute_amount_point(ctx, &G_term, value)) return 0; // V_term = v*G

    /* 2. Compute r*Pk_base (R_term) */
    Pk_term = *pk_base; // Start with the recipient's public key
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &Pk_term, blinding_factor) != 1) return 0; // R_term = r*Pk_base

    /* 3. Compute C = v*G + r*Pk_base */
    points_to_add[0] = &G_term;
    points_to_add[1] = &Pk_term;
    if (secp256k1_ec_pubkey_combine(ctx, commitment_C, points_to_add, 2) != 1) return 0;

    return 1;
}

/**
 * Generates the Bulletproof.
 */
int secp256k1_bulletproof_prove(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        size_t* proof_len,
        uint64_t value,
        const unsigned char* blinding_factor,
        const secp256k1_pubkey* pk_base, /* NEW: Dynamic H point */
        unsigned int proof_type
) {
    /* C90 Declarations */
    secp256k1_pubkey V_commitment, A, S, T1, T2;
    unsigned char al[N_BITS][32], ar[N_BITS][32];
    unsigned char sl[N_BITS][32], sr[N_BITS][32];

    /* Executable Code */
    if (value == 0) return 0; /* Range proofs are typically for non-zero values */

    /* 1. CREATE COMMITMENT: V = v*G + r*Pk_base (Initial point for the proof) */
    if (!secp256k1_bulletproof_create_commitment(ctx, &V_commitment, value, blinding_factor, pk_base)) {
        return 0;
    }

    /* 2. Phase 1: Vector Encoding and Commitment A, S */
    // NOTE: The internal functions called here (compute_vectors, commit_AS)
    // MUST be updated to use Pk_base instead of a hardcoded H_point.
    if (!secp256k1_bulletproof_compute_vectors(ctx, value, al, ar, sl, sr)) return 0;

    /* 3. Phase 2: Compute T(x) and Challenges y, z, x */

    /* 4. Phase 3: Inner Product Argument (IPA) Recursion */
    // NOTE: The IPA loop must use the recipient's public key (Pk_base) to update
    // the H vector components in each round of reduction.

    /* 5. AGGREGATE PROOF */
    // ... serialization ...

    return 1;
}


/**
 * Verifies a Bulletproof against a given commitment C.
 */
int secp256k1_bulletproof_verify(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        size_t proof_len,
        const secp256k1_pubkey* commitment_C,
        const secp256k1_pubkey* pk_base /* NEW: Dynamic H point */
) {
    /* C90 Declarations */
    // ...

    /* Executable Code */
    // NOTE: The verification logic is the inverse of the prover's aggregation.
    // It MUST use Pk_base when reconstructing the final verification point P_prime.

    return 0; // Placeholder
}