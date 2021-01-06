/**
 * Hook API include for Webassembly XRPLD Hooks
 *
 * Note to the reader:
 * This include defines two types of things: external functions and macros
 * Functions are used sparingly because a non-inlining compiler may produce
 * undesirable output.
 */

#ifndef HOOKAPI_INCLUDED
#define HOOKAPI_INCLUDED 1
#include <stdint.h>

int64_t hook(int64_t reserved) __attribute__((used));
int64_t cbak(int64_t reserved) __attribute__((used));

/**
 * Guard function. Each time a loop appears in your code a call to this must be the first branch instruction after the
 * beginning of the loop.
 * @param id The identifier of the guard (typically the line number).
 * @param maxiter The maximum number of times this loop will iterate across the life of the hook.
 * @return Can be ignored. If the guard is violated the hook will terminate.
 */
extern int32_t _g                  (uint32_t id, uint32_t maxiter);

/**
 * Accept the originating transaction and commit all hook state changes and submit all emitted transactions.
 * @param read_ptr An optional string to use as a return comment. May be 0.
 * @param read_len The length of the string. May be 0.
 * @return Will never return, terminates the hook.
 */
extern int64_t accept              (uint32_t read_ptr,  uint32_t read_len,   int32_t error_code);

/**
 * Rollback the originating transaction, discard all hook state changes and emitted transactions.
 * @param read_ptr An optional string to use as a return comment. May be 0.
 * @param read_len The length of the string. May be 0.
 * @return Will never return, terminates the hook.
 */
extern int64_t rollback            (uint32_t read_ptr,  uint32_t read_len,   int32_t error_code);

/**
 * Read a 20 byte account-id from the memory pointed to by read_ptr of length read_len and encode it to a base58-check
 * encoded r-address.
 * @param read_ptr The memory address of the account-id
 * @param read_len The byte length of the account-id (should always be 20)
 * @param write_ptr The memory address of a suitable buffer to write the encoded r-address into.
 * @param write_len The size of the write buffer.
 * @return On success the length of the r-address will be returned indicating the bytes written to the write buffer.
 *         On failure a negative integer is returned indicating what went wrong.
 */
extern int64_t util_raddr          (uint32_t write_ptr, uint32_t write_len,
                                    uint32_t read_ptr,  uint32_t read_len);

/**
 * Read an r-address from the memory pointed to by read_ptr of length read_len and decode it to a 20 byte account id
 * and write to write_ptr
 * @param read_ptr The memory address of the r-address
 * @param read_len The byte length of the r-address
 * @param write_ptr The memory address of a suitable buffer to write the decoded account id into.
 * @param write_len The size of the write buffer.
 * @return On success 20 will be returned indicating the bytes written. On failure a negative integer is returned
 *         indicating what went wrong.
 */
extern int64_t util_accid          (uint32_t write_ptr, uint32_t write_len,
                                    uint32_t read_ptr,  uint32_t read_len);
/**
 * Verify a cryptographic signature either ED25519 of SECP256k1. Public key should be prefixed with 0xED for 25519.
 * @param dread_ptr The memory location of the data or payload to verify
 * @param dread_len The length of the data or payload to verify
 * @param sread_ptr The memory location of the signature
 * @param sread_len The length of the signature
 * @param kread_ptr The memory location of the public key
 * @param kread_len The length of the public key
 * @return True if and only if the signature was verified.
 */
extern int64_t util_verify         (uint32_t dread_ptr, uint32_t dread_len,
                                    uint32_t sread_ptr, uint32_t sread_len,
                                    uint32_t kread_ptr, uint32_t kread_len);
/**
 * Compute the first half of a SHA512 checksum.
 * @param write_ptr The buffer to write the checksum into. Must be at least 32 bytes.
 * @param write_len The length of the buffer.
 * @param read_ptr  The buffer to read data for digest from.
 * @param read_len  The amount of data to read from the buffer.
 * @return The number of bytes written to write_ptr or a negative integer on error.
 */
extern int64_t util_sha512h        (uint32_t write_ptr, uint32_t write_len,
                                    uint32_t read_ptr,  uint32_t read_len);
/**
 * Index into a xrpld serialized object and return the location and length of a subfield. Except for Array subtypes
 * the offset and length refer to the **payload** of the subfield not the entire subfield. Use SUB_OFFSET and 
 * SUB_LENGTH macros to extract return value.
 * @param read_ptr The memory location of the stobject
 * @param read_len The length of the stobject
 * @param field_id The Field Code of the subfield
 * @return high-word (most significant 4 bytes excluding the most significant bit (MSB)) is the field offset relative
 *         to read_ptr and the low-word (least significant 4 bytes) is its length. MSB is sign bit, if set (negative)
 *         return value indicates error (typically error means could not find.)
 */
extern int64_t util_subfield       (uint32_t read_ptr,  uint32_t read_len, uint32_t field_id );

/**
 * Index into a xrpld serialized array and return the location and length of an index. Unlike util_subfield this api
 * always returns the offset and length of the whole object at that index (not its payload.) Use SUB_OFFSET and
 * SUB_LENGTH macros to extract return value.
 * @param read_ptr The memory location of the stobject
 * @param read_len The length of the stobject
 * @param array_id The index requested
 * @return high-word (most significant 4 bytes excluding the most significant bit (MSB)) is the field offset relative
 *         to read_ptr and the low-word (least significant 4 bytes) is its length. MSB is sign bit, if set (negative)
 *         return value indicates error (typically error means could not find.)
 */
extern int64_t util_subarray       (uint32_t read_ptr,  uint32_t read_len, uint32_t array_id );

/**
 * Compute burden for an emitted transaction.
 * @return the burden a theoretically emitted transaction would have.
 */ 
extern int64_t etxn_burden         (void );

/**
 * Write a full emit_details stobject into the buffer specified.
 * @param write_ptr A sufficiently large buffer to write into.
 * @param write_len The length of that buffer.
 * @return The number of bytes written or a negative integer indicating an error.
 */
extern int64_t etxn_details        (uint32_t write_ptr,  uint32_t write_len);

/**
 * Compute the minimum fee required to be paid by a hypothetically emitted transaction based on its size in bytes.
 * @param The size of the emitted transaction in bytes
 * @return The minimum fee in drops this transaction should pay to succeed
 */
extern int64_t etxn_fee_base       (uint32_t tx_byte_count);

/**
 * Inform xrpld that you will be emitting at most @count@ transactions during the course of this hook execution.
 * @param count The number of transactions you intend to emit from this  hook.
 * @return If a negaitve integer an error has occured
 */
extern int64_t etxn_reserve        (uint32_t count);

/**
 * Compute the generation of an emitted transaction. If this hook was invoked by a transaction emitted by a previous
 * hook then the generation counter will be 1+ the previous generation counter otherwise it will be 1.
 * @return The generation of a hypothetically emitted transaction.
 */
extern int64_t etxn_generation     (void);

/**
 * Emit a transaction from this hook.
 * @param read_ptr Memory location of a buffer containing the fully formed binary transaction to emit.
 * @param read_len The length of the transaction.
 * @return A negative integer if the emission failed.
 */
extern int64_t emit                (uint32_t read_ptr,   uint32_t read_len);

/**
 * Retrieve the account the hook is running on.
 * @param write_ptr A buffer of at least 20 bytes to write into.
 * @param write_len The length of that buffer
 * @return The number of bytes written into the buffer of a negative integer if an error occured.
 */
extern int64_t hook_account        (uint32_t write_ptr,  uint32_t write_len);

/**
 * Retrieve the hash of the currently executing hook.
 * @param write_ptr A buffer of at least 32 bytes to write into.
 * @param write_len The length of that buffer
 * @return The number of bytes written into the buffer of a negative integer if an error occured.
 */
extern int64_t hook_hash           (uint32_t write_ptr,  uint32_t write_len);

/**
 * Retrive the currently recommended minimum fee for a transaction to succeed.
 */
extern int64_t fee_base            (void);

/**
 * Retrieve the current ledger sequence number
 */
extern int64_t ledger_seq          (void);

/**
 * Retrieve a nonce for use in an emitted transaction (or another task). Can be called repeatedly for multiple nonces.
 * @param write_ptr A buffer of at least 32 bytes to write into.
 * @param write_len The length of that buffer
 * @return The number of bytes written into the buffer of a negative integer if an error occured.
 */ 
extern int64_t nonce               (uint32_t write_ptr,  uint32_t write_len);


/**
 * Slot functions have not been implemented yet and the api for them is subject to change
 */
extern int64_t slot_clear          (uint32_t slot);
extern int64_t slot_set            (uint32_t read_ptr,   uint32_t read_len,
                                    uint32_t slot_type,  int32_t  slot);
extern int64_t slot_field_txt      (uint32_t write_ptr,  uint32_t write_len,
                                    uint32_t field_id,   uint32_t slot);
extern int64_t slot_field          (uint32_t write_ptr,  uint32_t write_len,
                                    uint32_t field_id,   uint32_t slot);
extern int64_t slot_id             (uint32_t slot);
extern int64_t slot_type           (uint32_t slot);
extern int64_t trace_slot          (uint32_t slot);


/**
 * In the hook's state key-value map, set the value for the key pointed at by kread_ptr.
 * @param read_ptr A buffer containing the data to store
 * @param read_len The length of the data
 * @param kread_ptr A buffer containing the key
 * @param kread_len The length of the key
 * @return The number of bytes stored or a negative integer if an error occured
 */
extern int64_t state_set           (uint32_t read_ptr,   uint32_t read_len,
                                    uint32_t kread_ptr,  uint32_t kread_len);

/**
 * Retrieve a value from the hook's key-value map.
 * @param write_ptr A buffer to write the state value into
 * @param write_len The length of that buffer
 * @param kread_ptr A buffer to read the state key from
 * @param kread_len The length of that key
 * @return The number of bytes written or a negative integer if an error occured.
 */
extern int64_t state               (uint32_t write_ptr,  uint32_t write_len,
                                    uint32_t kread_ptr,  uint32_t kread_len);

/**
 * Retrieve a value from another hook's key-value map.
 * @param write_ptr A buffer to write the state value into
 * @param write_len The length of that buffer
 * @param kread_ptr A buffer to read the state key from
 * @param kread_len The length of that key
 * @param aread_ptr A buffer containing an account-id of another account containing a hook whose state we are reading
 * @param aread_len The length of the account-id (should always be 20).
 * @return The number of bytes written or a negative integer if an error occured.
 */
extern int64_t state_foreign       (uint32_t write_ptr,  uint32_t write_len,
                                    uint32_t kread_ptr,  uint32_t kread_len,
                                    uint32_t aread_ptr,  uint32_t aread_len);

/**
 * Print some output to the trace log on xrpld. Any xrpld instance set to "trace" log level will see this.
 * @param read_ptr A buffer containing either data or text (in either utf8, or utf16le)
 * @param read_len The byte length of the data/text to send to the trace log
 * @param as_hex If 0 treat the read_ptr as pointing at a string of text, otherwise treat it as data and print hex
 * @return The number of bytes output or a negative integer if an error occured.
 */
extern int64_t trace               (uint32_t read_ptr,   uint32_t read_len,   uint32_t as_hex);

/**
 * Print some output to the trace log on xrpld along with a decimal number. Any xrpld instance set to "trace" log
 * level will see this.
 * @param read_ptr A pointer to the string to output
 * @param read_len The length of the string to output
 * @param number Any integer you wish to display after the text
 * @return A negative value on error
 */
extern int64_t trace_num           (uint32_t read_ptr,   uint32_t read_len,   int64_t number);

/**
 * Retrieve the burden of the originating transaction (if any)
 * @return The burden of the originating transaction
 */
extern int64_t otxn_burden         (void);

/**
 * Retrieve a field from the originating transaction as "full text" (The way it is displayed in JSON)
 * @param write_ptr A buffer to write the representation into
 * @param write_len The length of the buffer
 * @param field_id The field code of the field being requested
 * @return The number of bytes written to write_ptr or a negative integer if an error occured.
 */
extern int64_t otxn_field_txt      (uint32_t write_ptr,  uint32_t write_len,  uint32_t field_id);

/**
 * Retrieve a field from the originating transaction in its raw serialized form.
 * @param write_ptr A buffer to output the field into
 * @param write_len The length of the buffer.
 * @param field_if The field code of the field being requested
 * @return The number of bytes written to write_ptr or a negative integer if an error occured.
 */
extern int64_t otxn_field          (uint32_t write_ptr,  uint32_t write_len,  uint32_t field_id);

/**
 * Retrieve the generation of the originating transaction (if any).
 * @return the generation of the originating transaction.
 */
extern int64_t otxn_generation     (void);

/**
 * Retrieve the TXNID of the originating transaction.
 * @param write_ptr A buffer at least 32 bytes long
 * @param write_len The length of the buffer.
 * @return The number of bytes written into the buffer or a negative integer on failure.
 */
extern int64_t otxn_id             (uint32_t write_ptr,  uint32_t write_len);

/**
 * Retrieve the Transaction Type (e.g. ttPayment = 0) of the originating transaction.
 * @return The Transaction Type (tt-code)
 */ 
extern int64_t otxn_type           (void);


#define SUCCESS  0                  // return codes > 0 are reserved for hook apis to return "success"
#define OUT_OF_BOUNDS  -1           // could not read or write to a pointer to provided by hook
#define INTERNAL_ERROR  -2          // eg directory is corrupt
#define TOO_BIG  -3                 // something you tried to store was too big
#define TOO_SMALL  -4               // something you tried to store or provide was too small
#define DOESNT_EXIST  -5            // something you requested wasn't found
#define NO_FREE_SLOTS  -6           // when trying to load an object there is a maximum of 255 slots
#define INVALID_ARGUMENT  -7        // self explanatory
#define ALREADY_SET  -8             // returned when a one-time parameter was already set by the hook
#define PREREQUISITE_NOT_MET  -9    // returned if a required param wasn't set before calling
#define FEE_TOO_LARGE  -10          // returned if the attempted operation would result in an absurd fee
#define EMISSION_FAILURE  -11       // returned if an emitted tx was not accepted by rippled
#define TOO_MANY_NONCES  -12        // a hook has a maximum of 256 nonces
#define TOO_MANY_EMITTED_TXN  -13   // a hook has emitted more than its stated number of emitted txn
#define NOT_IMPLEMENTED  -14        // an api was called that is reserved for a future version
#define INVALID_ACCOUNT  -15        // an api expected an account id but got something else
#define GUARD_VIOLATION  -16        // a guarded loop or function iterated over its maximum
#define INVALID_FIELD  -17          // the field requested is returning sfInvalid
#define PARSE_ERROR  -18            // hook asked hookapi to parse something the contents of which was invalid


#include "sfcodes.h"
#include "hookmacro.h"

#endif
