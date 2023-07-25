# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)



# Declares and names the project.



project("appmovil")



set(OpenCV_STATIC on)

set(OpenCV_DIR C:/openCv/OpenCV-android-sdk/sdk/native/jni/)

find_package(OpenCV REQUIRED)

include_directories(C:/openCv/OpenCV-android-sdk/sdk/native/jni/include/)




add_library( # Sets the name of the library.
        appmovil

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)


find_library(
        jnigraphics-lib jnigraphics
)

find_library( # Sets the name of the path variable.
        log-lib

        log)

target_link_libraries( # Specifies the target library.
        appmovil
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
