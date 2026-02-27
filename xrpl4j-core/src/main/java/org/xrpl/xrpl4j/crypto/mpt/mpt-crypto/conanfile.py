from conan import ConanFile, tools
from conan.tools.cmake import CMake, CMakeDeps, CMakeToolchain, cmake_layout
from conan.tools.files import get

required_conan_version = ">=2.0.0"


class MptCryptoConan(ConanFile):
    name = "mpt-crypto"
    description = "MPT-Crypto: Cryptographic Primitives for Confidential Assets"
    url = "https://github.com/XRPLF/mpt-crypto"
    package_type = "library"
    settings = "os", "arch", "compiler", "build_type"
    options = {
        "shared": [True, False],
        "fPIC": [True, False],
        "tests": [True, False],
    }
    default_options = {
        "shared": False,
        "fPIC": True,
        "tests": False,
    }

    requires = [
        "openssl/3.5.5",
        "secp256k1/0.7.1",
    ]

    def config_options(self):
        if self.settings.os == "Windows":
            del self.options.fPIC

    def layout(self):
        cmake_layout(self, src_folder="src")
        self.folders.generators = "build/generators"

    def generate(self):
        tc = CMakeToolchain(self)
        tc.variables["ENABLE_TESTS"] = self.options.tests
        tc.generate()

        deps = CMakeDeps(self)
        deps.generate()

    def build(self):
        cmake = CMake(self)
        cmake.configure()
        cmake.build()

    def package(self):
        cmake = CMake(self)
        cmake.install()

    def package_info(self):
        self.cpp_info.libs = ["mpt-crypto"]
        self.cpp_info.set_property("cmake_file_name", "mpt-crypto")
        self.cpp_info.set_property("cmake_target_name", "mpt-crypto::mpt-crypto")
