// $Id: NativeOS.cc,v 1.4 2007/09/24 08:10:16 jim Exp $

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

#ifdef HAVE_PWD_H
#  include <pwd.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif


#include "NativeOS.hh"

/* Get the abstract file system path location of the given user's home directory. */  
extern "C" 
JNIEXPORT jstring
JNICALL Java_us_temerity_pipeline_NativeOS_getUserHomePathNative
(
 JNIEnv *env, 
 jclass cls, 
 jstring juser  /* IN: the name of the user */ 
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeOS.realpathNative(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }

  /* repackage the arguments */ 
  const char* user = env->GetStringUTFChars(juser, 0);
  if((user == NULL) || (strlen(user) == 0)) {
    env->ThrowNew(IOException,"empty user argument");
    return NULL;
  }

  /* find the user's password database entry */ 
  char* homedir = NULL;
  while(1) {
    struct passwd* pwent = getpwent();
    if(pwent == NULL) 
      break;

    if(strcmp(pwent->pw_name, user) == 0) {
      homedir = pwent->pw_dir;
      break;
    }
  }

  if(homedir == NULL) {
    sprintf(msg, "cannot determine the home directory for (%s)\n", user);
    env->ReleaseStringUTFChars(juser, user); 
    env->ThrowNew(IOException, msg);  
    return NULL; 
  }
  else {
    env->ReleaseStringUTFChars(juser, user); 
    return env->NewStringUTF(homedir);
  }
}
