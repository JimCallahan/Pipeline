// $Id: NativeFileSys.cc,v 1.3 2006/09/29 09:57:03 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_ASSERT_H
#  include <assert.h>
#endif

#ifdef HAVE_STRING_H
#  include <string.h>
#endif

#ifdef HAVE_CSTDIO
#  include <cstdio>
#else
#  ifdef HAVE_STDIO_H
#    include <stdio.h>
#  endif
#endif

#ifdef HAVE_CSTDLIB
#  include <cstdlib>
#else
#  ifdef HAVE_STDLIB_H
#    include <stdlib.h>
#  endif
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_PARAM_H
#  include <sys/param.h>
#endif

#ifdef HAVE_SYS_STATFS_H
#  include <sys/statfs.h>
#endif

#ifdef HAVE_SYS_MOUNT_H
#  include <sys/mount.h>
#endif


#include "NativeFileSys.hh"

 
/* Change file access permissions. */
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeFileSys_chmodNative
(
 JNIEnv *env, 
 jclass cls, 
 jint mode,    /* IN: the access mode bitmask */ 
 jstring jfile  /* IN: the fully resolved path to the file to change */ 
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.chmodNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* repackage the arguments */ 
  const char* file = env->GetStringUTFChars(jfile, 0);
  if((file == NULL) || (strlen(file) == 0)) {
    env->ThrowNew(IOException,"empty file argument");
    return;
  }

  /* change the access permissions */ 
  if(chmod(file, mode) == -1) {
    sprintf(msg, "failed to change the permissions of file (%d): %s\n", 
	    file, strerror(errno));
    env->ThrowNew(IOException, msg);    
  }
}
 
/* Set the file creation mask. */
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeFileSys_umaskNative
(
 JNIEnv *env, 
 jclass cls, 
 jint mask     /* IN: the file creation bitmask */ 
)
{
  /* change the file creation mask (always succeeds) */ 
  umask(mask);
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
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.symlinkNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* repackage the arguments */ 
  const char* file = env->GetStringUTFChars(jfile, 0);
  if((file == NULL) || (strlen(file) == 0)) {
    env->ThrowNew(IOException,"empty file argument");
    return;
  }

  const char* link = env->GetStringUTFChars(jlink, 0);
  if((file == NULL) || (strlen(file) == 0)) {
    env->ThrowNew(IOException,"empty link argument");
    return;
  }
  
  /* create the symlink */ 
  if(symlink(file, link) == -1) {
    sprintf(msg, "unable to create symlink (%s) pointing to (%s): %s\n", 
	    link, file, strerror(errno));
    env->ThrowNew(IOException, msg);  
  }
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
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.realpathNative(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return NULL;
  }

  /* resolve the path */ 
  char resolved[MAXPATHLEN];
  if(realpath(path, resolved) == NULL) {
    sprintf(msg, "cannot resolve (%s): %s\n", path, strerror(errno));
    env->ThrowNew(IOException, msg);  
  }

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
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.lastChangedNative(), unable to lookup \"java/lang/IOException\"");
    return 0L;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return 0L;
  }

  /* get the file status */ 
  struct stat buf;
  switch(stat(path, &buf)) {
  case 0:
    return ((jlong) ((buf.st_mtime > buf.st_ctime) ? buf.st_mtime : buf.st_ctime));
    
  case EACCES:
  case ENOENT: 
  case ENOTDIR:
    return 0L;

  default:
    sprintf(msg, "cannot stat (%s): %s\n", path, strerror(errno));
    env->ThrowNew(IOException, msg);  
    return 0L;
  }
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
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.freeDiskSpaceNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return -1;
  }

  /* get the file system statistics */ 
  struct statfs fs;
  if(statfs(path, &fs) != 0) {
    sprintf(msg, "cannot determine free disk space for (%s): %s\n", path, strerror(errno));
    env->ThrowNew(IOException, msg);  
  }

  return ((jlong) fs.f_bavail * 4096L);
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
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileSys.freeDiskSpaceNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return -1;
  }

  /* get the file system statistics */ 
  struct statfs fs;
  if(statfs(path, &fs) != 0) {
    sprintf(msg, "cannot determine total disk space for (%s): %s\n", path, strerror(errno));
    env->ThrowNew(IOException, msg);  
  }

  return ((jlong) fs.f_blocks * 4096L);
}
