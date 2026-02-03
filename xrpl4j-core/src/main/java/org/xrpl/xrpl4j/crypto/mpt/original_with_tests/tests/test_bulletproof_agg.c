#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <time.h>
#include <openssl/rand.h>
#include <secp256k1.h>

#include "secp256k1_mpt.h"

/* ---- Aggregation parameters ---- */
#define M 2
#define BP_VALUE_BITS 64
#define BP_TOTAL_BITS(m) ((size_t)(BP_VALUE_BITS * (m)))

/* ---- Benchmark parameters ---- */
#define VERIFY_RUNS 5

/* ---- Prototypes ---- */
int secp256k1_bulletproof_prove_agg(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        size_t* proof_len,
        const uint64_t* values,
        const unsigned char* blindings_flat,
        size_t m,
        const secp256k1_pubkey* pk_base,
        const unsigned char* context_id
);

int secp256k1_bulletproof_verify_agg(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* G_vec,
        const secp256k1_pubkey* H_vec,
        const unsigned char* proof,
        size_t proof_len,
        const secp256k1_pubkey* commitment_C_vec,
        size_t m,
        const secp256k1_pubkey* pk_base,
        const unsigned char* context_id
);

int secp256k1_bulletproof_create_commitment(
        const secp256k1_context* ctx,
        secp256k1_pubkey* commitment_C,
        uint64_t value,
        const unsigned char* blinding_factor,
        const secp256k1_pubkey* pk_base
);

extern int secp256k1_mpt_get_generator_vector(
        const secp256k1_context* ctx,
        secp256k1_pubkey* vec,
        size_t n,
        const unsigned char* label,
        size_t label_len
);

/* ---- Helpers ---- */

static inline double elapsed_ms(struct timespec a, struct timespec b) {
    return (b.tv_sec - a.tv_sec) * 1000.0 +
           (b.tv_nsec - a.tv_nsec) / 1e6;
}

static void random_scalar(
        const secp256k1_context* ctx,
        unsigned char out[32]
) {
    do {
        RAND_bytes(out, 32);
    } while (!secp256k1_ec_seckey_verify(ctx, out));
}

/* ---- Main ---- */
int main(void) {
    printf("[TEST] Aggregated Bulletproof test (m = %d)\n", M);

    /* ---- Context ---- */
    secp256k1_context* ctx =
            secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);

    /* ---- Values ---- */
    uint64_t values[M] = { 5000, 123456 };
    unsigned char blindings[M][32];
    secp256k1_pubkey commitments[M];

    /**
     * CONTEXT BINDING:
     * In the production system, this ID is derived deterministically:
     * TransactionContextID := H(TxType || Account || MPTokenIssuanceID || ...)
     * * See [Spec Section 3.3.1] for the derivation rules.
     * * For this library unit test, random bytes suffice to verify that the
     * proof binds correctly to *whatever* context ID is provided.
     */
    unsigned char context_id[32];
    RAND_bytes(context_id, 32);


    secp256k1_pubkey pk_base;
   /* Use the standard H generator from the library */
    assert(secp256k1_mpt_get_h_generator(ctx, &pk_base));


    /* ---- Commitments ---- */
    for (size_t i = 0; i < M; i++) {
        random_scalar(ctx, blindings[i]);
        assert(secp256k1_bulletproof_create_commitment(
                ctx,
                &commitments[i],
                values[i],
                blindings[i],
                &pk_base));
    }

    /* ---- Generator vectors ---- */
    const size_t n = BP_TOTAL_BITS(M);
    secp256k1_pubkey* G_vec = malloc(n * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* H_vec = malloc(n * sizeof(secp256k1_pubkey));
    assert(G_vec && H_vec);

    assert(secp256k1_mpt_get_generator_vector(
            ctx, G_vec, n, (const unsigned char*)"G", 1));
    assert(secp256k1_mpt_get_generator_vector(
            ctx, H_vec, n, (const unsigned char*)"H", 1));

    /* ---- Prove (timed) ---- */
    unsigned char proof[4096];
    size_t proof_len = sizeof(proof);

    printf("[TEST] Proving aggregated range proof...\n");

    struct timespec t_p_start, t_p_end;
    clock_gettime(CLOCK_MONOTONIC, &t_p_start);

    assert(secp256k1_bulletproof_prove_agg(
            ctx,
            proof,
            &proof_len,
            values,
            (const unsigned char*)blindings,
            M,
            &pk_base,
            context_id));

    clock_gettime(CLOCK_MONOTONIC, &t_p_end);

    printf("[TEST] Proof size = %zu bytes\n", proof_len);
    printf("[BENCH] Proving time: %.3f ms\n",
           elapsed_ms(t_p_start, t_p_end));

    /* ---- Verify (single run, timed) ---- */
    printf("[TEST] Verifying aggregated proof...\n");

    struct timespec t_v_start, t_v_end;
    clock_gettime(CLOCK_MONOTONIC, &t_v_start);

    int ok = secp256k1_bulletproof_verify_agg(
            ctx,
            G_vec,
            H_vec,
            proof,
            proof_len,
            commitments,
            M,
            &pk_base,
            context_id);

    clock_gettime(CLOCK_MONOTONIC, &t_v_end);

    if (!ok) {
        printf("FAILED\n");
        return 1;
    }

    printf("PASSED\n");
    printf("[BENCH] Verification time (single): %.3f ms\n",
           elapsed_ms(t_v_start, t_v_end));

    /* ---- Verify benchmark (average) ---- */
    double total_ms = 0.0;

    for (int i = 0; i < VERIFY_RUNS; i++) {
        struct timespec ts, te;
        clock_gettime(CLOCK_MONOTONIC, &ts);

        ok = secp256k1_bulletproof_verify_agg(
                ctx,
                G_vec,
                H_vec,
                proof,
                proof_len,
                commitments,
                M,
                &pk_base,
                context_id);

        clock_gettime(CLOCK_MONOTONIC, &te);

        assert(ok);
        total_ms += elapsed_ms(ts, te);
    }

    printf("[BENCH] Verification avg over %d runs: %.3f ms\n",
           VERIFY_RUNS, total_ms / VERIFY_RUNS);

    /* ---- Negative test ---- */
    printf("[TEST] Tamper test... ");

    secp256k1_pubkey bad_commitments[M];
    memcpy(bad_commitments, commitments, sizeof(commitments));

    unsigned char bad_blinding[32];
    random_scalar(ctx, bad_blinding);

    assert(secp256k1_bulletproof_create_commitment(
            ctx,
            &bad_commitments[1],
            values[1] + 1,
            bad_blinding,
            &pk_base));

    ok = secp256k1_bulletproof_verify_agg(
            ctx,
            G_vec,
            H_vec,
            proof,
            proof_len,
            bad_commitments,
            M,
            &pk_base,
            context_id);

    if (ok) {
        printf("FAILED (accepted invalid proof)\n");
        return 1;
    }

    printf("PASSED (rejected invalid proof)\n");

    /* ---- Cleanup ---- */
    free(G_vec);
    free(H_vec);
    secp256k1_context_destroy(ctx);

    printf("[TEST] Aggregated Bulletproof test completed successfully\n");
    return 0;
}
