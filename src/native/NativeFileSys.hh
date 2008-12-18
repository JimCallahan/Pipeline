/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class us_temerity_pipeline_NativeFileSys */

#ifndef _Included_us_temerity_pipeline_NativeFileSys
#define _Included_us_temerity_pipeline_NativeFileSys
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    chmodNative
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeFileSys_chmodNative
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    umaskNative
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeFileSys_umaskNative
  (JNIEnv *, jclass, jint);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    symlinkNative
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_us_temerity_pipeline_NativeFileSys_symlinkNative
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    realpathNative
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_us_temerity_pipeline_NativeFileSys_realpathNative
  (JNIEnv *, jclass, jstring);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    lastStamps
 * Signature: (Ljava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_us_temerity_pipeline_NativeFileSys_lastStamps
  (JNIEnv *, jclass, jstring, jlong);

/*
 * Class:     us_temerity_pipeline_NativeFileSys
 * Method:    md5sumNative
 * Signature: (Ljava/lang/String;J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_us_temerity_pipeline_NativeFileSys_md5sumNative
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

#ifdef __cplusplus
}
#endif
#endif
