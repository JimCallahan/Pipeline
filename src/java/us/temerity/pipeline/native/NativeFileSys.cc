// $Id: NativeFileSys.cc,v 1.3 2004/04/11 19:30:20 jim Exp $

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
