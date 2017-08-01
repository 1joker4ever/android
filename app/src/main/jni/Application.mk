DISABLE_WEBRTC = true
USE_LIBWEBSOCKETS = false

ifeq ($(DISABLE_WEBRTC),true)
APP_PLATFORM = android-9
APP_ABI := armeabi x86
else
USE_LIBWEBSOCKETS = true
APP_PLATFORM = android-14
APP_ABI := armeabi-v7a x86
endif

NDK_TOOLCHAIN_VERSION=clang
APP_STL := c++_static
APP_OPTIM := release
APP_PIE := false

APP_CPPFLAGS += -std=c++11 -Wno-extern-c-compat -mno-unaligned-access

