// $Id: NativeFileStat.cc,v 1.2 2009/07/11 10:54:21 jim Exp $

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


#include "NativeFileStat.hh"

/*  */ 
extern "C" 
JNIEXPORT void
JNICALL Java_us_temerity_pipeline_NativeFileStat_statNative
(
 JNIEnv *env, 
 jobject obj, 
 jstring jpath  /* IN: the file to test */ 
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeFileStat.statNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* get handles for the NativeFileStat object's fields/methods */ 
  jclass NativeFileStatClass = env->GetObjectClass(obj);  
  if(NativeFileStatClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeFileStat");
    return;
  }
  
  jfieldID pINodeNumber = env->GetFieldID(NativeFileStatClass, "pINodeNumber", "J");
  if(pINodeNumber == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pINodeNumber");
    return;
  }
    
  jfieldID pMode = env->GetFieldID(NativeFileStatClass, "pMode", "I");
  if(pMode == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pMode");
    return;
  }
    
  jfieldID pFileSize = env->GetFieldID(NativeFileStatClass, "pFileSize", "J");
  if(pFileSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pFileSize");
    return;
  }
    
  jfieldID pLastAccess = env->GetFieldID(NativeFileStatClass, "pLastAccess", "J");
  if(pLastAccess == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pLastAccess");
    return;
  }
    
  jfieldID pLastMod = env->GetFieldID(NativeFileStatClass, "pLastMod", "J");
  if(pLastMod == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pLastMod");
    return;
  }
    
  jfieldID pLastChange = env->GetFieldID(NativeFileStatClass, "pLastChange", "J");
  if(pLastChange == 0) {
    env->ThrowNew(IOException, "unable to access: NativeFileStat.pLastChange");
    return;
  }

  /* repackage the arguments */ 
  const char* path = env->GetStringUTFChars(jpath, 0);
  if((path == NULL) || (strlen(path) == 0)) {
    env->ThrowNew(IOException,"empty path argument");
    return;
  }

  /* get the file status */ 
  struct stat buf;
  switch(stat(path, &buf)) {
  case 0:
    {
      env->SetLongField(obj, pINodeNumber, ((jlong) buf.st_ino));
      env->SetIntField(obj,  pMode,        ((jint)  buf.st_mode));
      env->SetLongField(obj, pFileSize,    ((jlong) buf.st_size));

      jlong linkAccess = ((jlong) buf.st_atime) * 1000L;
      jlong linkMod    = ((jlong) buf.st_mtime) * 1000L;
      jlong linkChange = ((jlong) buf.st_ctime) * 1000L;
      {
        struct stat lbuf;
        switch(lstat(path, &lbuf)) {
        case 0:
          /* if its a symlink, use its timestamps instead of the target file */ 
          if((lbuf.st_mode & S_IFMT) == S_IFLNK) {
            linkAccess = ((jlong) lbuf.st_atime) * 1000L;
            linkMod    = ((jlong) lbuf.st_mtime) * 1000L;
            linkChange = ((jlong) lbuf.st_ctime) * 1000L;
          }
        }
      }
      env->SetLongField(obj, pLastAccess, linkAccess); 
      env->SetLongField(obj, pLastMod,    linkMod);  
      env->SetLongField(obj, pLastChange, linkChange); 
    }
  }

  env->ReleaseStringUTFChars(jpath, path);
}
