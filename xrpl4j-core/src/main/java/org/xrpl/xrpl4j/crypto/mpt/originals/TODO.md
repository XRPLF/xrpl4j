# TODOs

1. Remove final byte[] privateKey and use a PrivateKey from xrpl4j.
1. Create implementation that wraps the C libraries
   2. See RocksDB for prior art (.so, .dll, .dylib) and load based on runtime.
   3. Need JNI wrapper for each of the C functions.
   4. Need a proper Java interface to properly abstract both.
   5. Remove the C code and consider a git submodule to the original Bulletproofs repo for the C.
   6. Need to build the C libraries as part of this project's CI
5. Ensure full unit test coverage from the C impl exists in Java.
2. Remove this folder once the above is completed
