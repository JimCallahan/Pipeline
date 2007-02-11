// $Id: NativeFileSys.cpp,v 1.4 2007/02/11 10:30:33 jim Exp $

#include "stdafx.h"

 
/* Change file access permissions. */
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeFileSys_chmodNative
(
 JNIEnv *env, 
 jclass cls, 
 jint mode,      /* IN: the access mode bitmask */ 
 jstring jfile   /* IN: the fully resolved path to the file to change */ 
)
{
  // IS THERE A .COM WAY TO DO THIS WHICH HAS MORE FUNCTIONALITY?

  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL; 
    perror("NativeFileSys.chmodNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* repackage the arguments */ 
  const char* file = env->GetStringUTFChars(jfile, 0);
  if((file == NULL) || (strlen(file) == 0)) {
    env->ThrowNew(IOException,"empty file argument");
    return;
  }
  
  /* convert from Unix to Windows mode */ 
  int wmode = 0;
  if(mode & 0111) 
    wmode &= _S_IREAD; 
  if(mode & 0222) 
    wmode &= _S_IWRITE;

  /* change the access permissions */ 
  if(_chmod(file, wmode) == -1) {
    sprintf(msg, "failed to change the permissions of file (%s): %s\n", 
	        file, strerror(errno));
    env->ReleaseStringUTFChars(jfile, file); 
    env->ThrowNew(IOException, msg);  
    return;    
  }

  env->ReleaseStringUTFChars(jfile, file); 
}
 
/* Create a symbolic link which points to the given file. */
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeFileSys_symlinkNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring jfile, /* IN: the relative or absolute path to the file pointed to by the symlink */
 jstring jlink  /* IN: the fully resolved path of the symlink to create */
)
{
  /* exception initialization */ 
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeFileSys.symlinkNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  env->ThrowNew(IOException, "Windows NativeFileSys.symlinkNative() not implementable!");  
}

/* Determine the canonicalized absolute pathname of the given path. */  
extern "C" 
JNIEXPORT jstring
JNICALL Java_us_temerity_pipeline_NativeFileSys_realpathNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring jpath  /* IN: the file system path to resolve */ 
)
{
  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeFileSys.realpathNative(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }
  
  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    env->ReleaseStringUTFChars(jpath, path); 
    return NULL;
  }

  /* resolve the path */ 
  char resolved[FILENAME_MAX];
  if(_fullpath(resolved, path, sizeof(resolved))) {
    sprintf(msg, "cannot resolve (%s): %s\n", 
	      path, strerror(errno)); 
    env->ReleaseStringUTFChars(jpath, path); 
    env->ThrowNew(IOException, msg);  
    return NULL;
  }

  env->ReleaseStringUTFChars(jpath, path); 
  return env->NewStringUTF(resolved);
}

/* Returns the newest of change and modification time for the given file. */ 
extern "C" 
JNIEXPORT jlong 
JNICALL Java_us_temerity_pipeline_NativeFileSys_lastChangedNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring jpath  /* IN: the file/directory to test */ 
)
{
  /* exception initialization */ 
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeFileSys.lastChangedNative(), unable to lookup \"java/lang/IOException\"");
    return 0L;
  }

  env->ThrowNew(IOException, "Windows NativeFileSys.lastChangedNative() not implementable!"); 
  return 0L;
}

/* Determine amount of free disk space available on the file system which contains the 
   given path. */  
extern "C" 
JNIEXPORT jlong
JNICALL Java_us_temerity_pipeline_NativeFileSys_freeDiskSpaceNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring jpath  /* IN: the file/directory used to determine the file system */ 
)
{
  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeFileSys.freeDiskSpaceNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return -1;
  }

  /* lookup the free disk space */ 
  {
    DWORD sectPerClust, bytesPerSect, freeClust, totalClust;

    if(!GetDiskFreeSpaceA(path, &sectPerClust, &bytesPerSect, &freeClust, &totalClust)) {
      sprintf(msg, "cannot determine free disk space for (%s)", path); 
      env->ReleaseStringUTFChars(jpath, path);
      env->ThrowNew(IOException, msg); 
      return -1; 
    }

    env->ReleaseStringUTFChars(jpath, path);
    return ((jlong) freeClust)  * ((jlong) sectPerClust) * ((jlong) bytesPerSect);
  }
}

/* Determine total amount of disk space available on the file system which contains the 
   given path. */  
extern "C" 
JNIEXPORT jlong
JNICALL Java_us_temerity_pipeline_NativeFileSys_totalDiskSpaceNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring jpath  /* IN: the file/directory used to determine the file system */ 
)
{
  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeFileSys.freeDiskSpaceNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return -1;
  }

  /* lookup the free disk space */ 
  {
    DWORD sectPerClust, bytesPerSect, freeClust, totalClust;

    if(!GetDiskFreeSpaceA(path, &sectPerClust, &bytesPerSect, &freeClust, &totalClust)) {
      sprintf(msg, "cannot determine total disk space for (%s)", path); 
      env->ReleaseStringUTFChars(jpath, path);
      env->ThrowNew(IOException, msg);  
      return -1;
    }

    env->ReleaseStringUTFChars(jpath, path);
    return ((jlong) totalClust)  * ((jlong) sectPerClust) * ((jlong) bytesPerSect);
  }  
}
