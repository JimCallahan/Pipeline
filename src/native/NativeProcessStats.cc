// $Id: NativeProcessStats.cc,v 1.2 2005/11/03 22:02:14 jim Exp $

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

#ifdef HAVE_CLIMITS
#  include <climits>
#else
#  ifdef HAVE_LIMITS_H
#    include <limits.h>
#  endif
#endif

#ifdef HAVE_CTYPE_H
#  include <ctype.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_LIBGEN_H
#  include <libgen.h>
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_FCNTL_H
#  include <fcntl.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
#endif

#ifdef HAVE_SYS_RESOURCE_H
#  include <sys/resource.h>
#endif

#include "NativeProcessStats.hh"

/* Resource usage statistics for the given process. */ 
JNIEXPORT jboolean 
JNICALL Java_us_temerity_pipeline_NativeProcessStats_collectStatsNative
(
 JNIEnv *env, 
 jobject obj, 
 jint pid      /* IN: process ID */ 
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("collectStatsNative(), unable to lookup \"java/lang/IOException\"");
    return false;
  }

  /* get handles for the NativeProcessStats object's fields/methods */ 
  jclass NativeProcessStatsClass = env->GetObjectClass(obj);  
  if(NativeProcessStatsClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessStats");
    return false;
  }
    
  jfieldID pParentPID = env->GetFieldID(NativeProcessStatsClass, "pParentPID", "I");
  if(pParentPID == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pParentPID");
    return false;
  }

  jfieldID pUTime = env->GetFieldID(NativeProcessStatsClass, "pUTime", "J");
  if(pUTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pUTime");
    return false;
  }

  jfieldID pSTime = env->GetFieldID(NativeProcessStatsClass, "pSTime", "J");
  if(pSTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pSTime");
    return false;
  }

  jfieldID pPageFaults = env->GetFieldID(NativeProcessStatsClass, "pPageFaults", "J");
  if(pPageFaults == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pPageFaults");
    return false;
  }

  jfieldID pVirtualSize = env->GetFieldID(NativeProcessStatsClass, "pVirtualSize", "J");
  if(pVirtualSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pVirtualSize");
    return false;
  }

  jfieldID pResidentSize = env->GetFieldID(NativeProcessStatsClass, "pResidentSize", "J");
  if(pResidentSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pResidentSize");
    return false;
  }

  jfieldID pSwappedSize = env->GetFieldID(NativeProcessStatsClass, "pSwappedSize", "J");
  if(pSwappedSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessStats.pSwappedSize");
    return false;
  }

#ifdef OS_IS_UNIX
  /* determine the size of memory pages */ 
  long psize = (long) getpagesize(); 
  if(psize <= 0) {
    env->ThrowNew(IOException, "cannot determine the page size!");
    return false;
  }

  /* scan the statistics psuedo-file for the process */ 
  {
    char path[1024];
    sprintf(path, "/proc/%d/stat", pid);
    FILE* file = fopen(path, "r");
    if(file == NULL) 
      return false;

    const char* pattern = 
      "%*s %*s %*s %d %*s %*s %*s %*s %*s %*s %*s %lu %*s %lu %lu %*s %*s %*s %*s %*s %*s %*s %lu %ld %*s %*s %*s %*s %*s %*s %*s %*s %*s %*s %*s %*s %lu";

    int ppid;
    unsigned long majflt, utime, stime, vmem, cnswap;
    long rss;
    int matches = fscanf(file, pattern, &ppid, &majflt, &utime, &stime, &vmem, &rss, &cnswap);
    fclose(file);
    if(matches != 7) {
      sprintf(msg, "internal error: %s", strerror(errno));
      env->ThrowNew(IOException, msg);
      return false;
    }

//     printf("RAW STATS: PID[%d]\n", pid);
//     printf("    PPID = %d\n", ppid); 
//     printf("  MAJFLT = %lu\n", majflt); 
//     printf("   UTIME = %lu\n", utime); 
//     printf("   STIME = %lu\n", stime); 
//     printf("    VMEM = %lu\n", vmem); 
//     printf("     RSS = %lu\n", rss); 
//     printf("  CNSWAP = %lu\n\n", cnswap); 

    env->SetIntField(obj, pParentPID, ppid);
    env->SetLongField(obj, pUTime, utime);
    env->SetLongField(obj, pSTime, stime);
    env->SetLongField(obj, pPageFaults, majflt);
    env->SetLongField(obj, pVirtualSize, vmem);
    env->SetLongField(obj, pResidentSize, rss * psize);
    env->SetLongField(obj, pSwappedSize, cnswap * psize);
  }  
#endif  

  return true;
}
