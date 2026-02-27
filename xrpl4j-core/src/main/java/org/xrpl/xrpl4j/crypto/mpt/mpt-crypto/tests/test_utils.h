#ifndef MPT_TEST_UTILS_H
#define MPT_TEST_UTILS_H

#include <openssl/rand.h>
#include <openssl/sha.h>

#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>

/* --- Macro: Persistent Assertion --- */
/* Ensures checks run in both Debug and Release modes */
#define EXPECT(condition)                                                          \
    do                                                                             \
    {                                                                              \
        if (!(condition))                                                          \
        {                                                                          \
            fprintf(stderr, "TEST FAILED: %s at line %d\n", #condition, __LINE__); \
            abort();                                                               \
        }                                                                          \
    } while (0)

/* Helper: Generate 32 raw random bytes (for seeds, IDs, etc.) */
static inline void
random_bytes(unsigned char* out)
{
    EXPECT(RAND_bytes(out, 32) == 1);
}

/* Helper: Generate a valid random scalar using OpenSSL RNG. */
static inline void
random_scalar(secp256k1_context const* ctx, unsigned char* out)
{
    do
    {
        EXPECT(RAND_bytes(out, 32) == 1);
    } while (!secp256k1_ec_seckey_verify(ctx, out));
}
#endif  // MPT_TEST_UTILS_H
