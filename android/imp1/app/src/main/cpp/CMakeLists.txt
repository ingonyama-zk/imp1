cmake_minimum_required(VERSION 3.22.1)
project(imp1)

set(RUST_LIBS_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
link_directories(${RUST_LIBS_DIR})

add_library(imp1 SHARED imp1.cpp)

target_link_libraries(
        imp1
        icicle_snark
        log # Android logging library
        c++_shared
)
