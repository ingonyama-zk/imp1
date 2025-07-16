cmake_minimum_required(VERSION 3.22.1)
project(imp1)

# Only force debug build for better profiling if not Release
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_BUILD_TYPE Debug)
    set(CMAKE_CXX_FLAGS_DEBUG "-g -O0 -fno-omit-frame-pointer")
endif()

set(RUST_LIBS_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
link_directories(${RUST_LIBS_DIR})

add_library(imp1 SHARED imp1.cpp)

# Ensure debug symbols are preserved
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set_target_properties(imp1 PROPERTIES
        COMPILE_FLAGS "-g -O0 -fno-omit-frame-pointer"
        LINK_FLAGS "-g"
    )
endif()

target_link_libraries(
        imp1
        icicle_snark
        log # Android logging library
        c++_shared
)
