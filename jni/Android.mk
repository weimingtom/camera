LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := surface-jni
LOCAL_SRC_FILES := surface-jni.c

include $(BUILD_SHARED_LIBRARY)
