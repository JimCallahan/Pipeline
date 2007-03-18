// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#define _CRT_SECURE_NO_DEPRECATE
#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>


#include <assert.h>
#include <string.h>
#include <cstdio>
#include <cstdlib>
#include <errno.h>
#include <io.h>
#include <sys/stat.h>
#include <sys/types.h>

#define _WIN32_DCOM
#include <comdef.h>
#include <comutil.h>
#include <Wbemidl.h>

#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <pdh.h>
#include <pdhmsg.h>
#include <psapi.h>


# pragma comment(lib, "wbemuuid.lib")

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

void throwWindowsIOException(JNIEnv *env, jclass IOException, LPTSTR lpszFunction);


/* Header for class us_temerity_pipeline_NativeFileSys */

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    realpathNative
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_us_temerity_pipeline_NativeFileSys_realpathNative
  (JNIEnv *, jclass, jstring);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    freeDiskSpaceNative
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_us_temerity_pipeline_NativeFileSys_freeDiskSpaceNative
  (JNIEnv *, jclass, jstring);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    totalDiskSpaceNative
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_us_temerity_pipeline_NativeFileSys_totalDiskSpaceNative
  (JNIEnv *, jclass, jstring);


/* Header for class us_temerity_pipeline_NativeOS */

/*
 * Class:     us_temerity_pipeline_NativeOS
 * Method:    nativeGetFreeMemoryNative
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_us_temerity_pipeline_NativeOS_getFreeMemoryNative
  (JNIEnv *, jclass);

/*
 * Class:     us_temerity_pipeline_NativeOS
 * Method:    nativeGetTotalMemoryNative
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_us_temerity_pipeline_NativeOS_getTotalMemoryNative
  (JNIEnv *, jclass);

/*
 * Class:     us_temerity_pipeline_NativeOS
 * Method:    getNumProcessorsNative
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_us_temerity_pipeline_NativeOS_getNumProcessorsNative
  (JNIEnv *, jclass);

/*
 * Class:     us_temerity_pipeline_NativeOS
 * Method:    getLoadAverageNative
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_us_temerity_pipeline_NativeOS_getLoadAverageNative
  (JNIEnv *, jclass);


/* Header for class us_temerity_pipeline_NativeProcessHeavy */

/*
 * Class:     us_temerity_pipeline_NativeProcessHeavy
 * Method:    writeToStdIn
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_writeToStdIn
  (JNIEnv *, jobject, jstring);

/*
 * Class:     us_temerity_pipeline_NativeProcessHeavy
 * Method:    closeStdIn
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_closeStdIn
  (JNIEnv *, jobject);

/*
 * Class:     us_temerity_pipeline_NativeProcessHeavy
 * Method:    signalNative
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_signalNative
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     us_temerity_pipeline_NativeProcessHeavy
 * Method:    execNativeHeavy
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_execNativeHeavy
  (JNIEnv *, jobject, jobjectArray, jobjectArray, jstring, jstring, jstring);


/* Header for class us_temerity_pipeline_NativeProcessLight */

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    writeToStdIn
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_us_temerity_pipeline_NativeProcessLight_writeToStdIn
  (JNIEnv *, jobject, jstring);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    closeStdIn
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdIn
  (JNIEnv *, jobject);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    readFromStdOut
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_us_temerity_pipeline_NativeProcessLight_readFromStdOut
  (JNIEnv *, jobject, jint);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    closeStdOut
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdOut
  (JNIEnv *, jobject);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    readFromStdErr
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_us_temerity_pipeline_NativeProcessLight_readFromStdErr
  (JNIEnv *, jobject, jint);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    closeStdErr
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdErr
  (JNIEnv *, jobject);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    signalNative
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeProcessLight_signalNative
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     us_temerity_pipeline_NativeProcessLight
 * Method:    execNativeLight
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_us_temerity_pipeline_NativeProcessLight_execNativeLight
  (JNIEnv *, jobject, jobjectArray, jobjectArray, jstring);



/* Header for class us_temerity_pipeline_NativeProcessStats */

/*
 * Class:     us_temerity_pipeline_NativeProcessStats
 * Method:    collectStatsNative
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_us_temerity_pipeline_NativeProcessStats_collectStatsNative
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
