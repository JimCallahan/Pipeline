// $Id: NativeOS.cc,v 1.1 2005/11/03 22:02:14 jim Exp $

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

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_SYSCTL_H
#  include <sys/sysctl.h>
#endif


#include "NativeOS.hh"
 
/* Get the total amount of system memory (in bytes). */
extern "C" 
JNIEXPORT jlong
JNICALL Java_us_temerity_pipeline_NativeOS_getTotalMemoryNative
(
 JNIEnv *env, 
 jclass cls
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeOS.getTotalMemoryNative(), unable to lookup \"java/lang/IOException\"");
    return -2L;
  }

  long total = -1L;

#ifdef OS_IS_MAC_OS
  {
    int mib[2], mem;
    size_t len;
    
    mib[0] = CTL_HW;
    mib[1] = HW_PHYSMEM; 
    len = sizeof(mem);
    if(sysctl(mib, 2, &mem, &len, NULL, 0) == -1) {
      sprintf(msg, "cannot determine total memory size: %s\n", strerror(errno));
      env->ThrowNew(IOException, msg);
    }
    
    total = ((long) mem); 
  }
#endif

 return ((jlong) total);
}
 

/* Get the number of processors (CPUs). */
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_NativeOS_getNumProcessorsNative
(
 JNIEnv *env, 
 jclass cls
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeOS.getNumProcessorsNative(), unable to lookup \"java/lang/IOException\"");
    return -2;
  }

  int procs = -1;
  
#ifdef OS_IS_MAC_OS
  {
    int mib[2];
    size_t len;
    
    mib[0] = CTL_HW;
    mib[1] = HW_NCPU;
    len = sizeof(procs);
    if(sysctl(mib, 2, &procs, &len, NULL, 0) == -1) {
      sprintf(msg, "cannot determine the number of processors: %s\n", strerror(errno));
      env->ThrowNew(IOException, msg);
    }
  }
#endif

  return ((jint) procs);
}
 

/* Get the system load factor (1-minute average). */
extern "C" 
JNIEXPORT jfloat
JNICALL Java_us_temerity_pipeline_NativeOS_getLoadAverageNative
(
 JNIEnv *env, 
 jclass cls
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeOS.getLoadAverageNative(), unable to lookup \"java/lang/IOException\"");
    return -2.0f;
  }

  float load = -1.0f;

#ifdef OS_IS_MAC_OS
  {
    double loadavg[1];
    
    if(getloadavg(loadavg, 1) == -1) 
      env->ThrowNew(IOException, "cannot determine the load average!");
    
    load = (float) loadavg[0];
  }
#endif

  return ((jfloat) load);
}
 
