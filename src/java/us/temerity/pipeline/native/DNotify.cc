// $Id: DNotify.cc,v 1.3 2004/04/14 18:42:13 jim Exp $

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_ASSERT_H
#  include <assert.h>
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

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_STRING_H
#  include <string.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_STAT_H
#  include <sys/stat.h>
#endif

#ifdef HAVE_SYS_PARAM_H
#  include <sys/param.h>
#endif

#ifdef HAVE_FCNTL_H
#  include <fcntl.h>
#endif

#ifdef HAVE_SIGNAL_H
#  include <signal.h>
#endif

#ifdef HAVE_SYS_RESOURCE_H
#  include <sys/resource.h>
#endif

#include "DNotify.hh"


/* Initialize the signal handling. */ 
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_core_DNotify_initNative
(
 JNIEnv* env, 
 jobject obj
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("DNotify.initNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* block handling of SIGRTMIN+4 */
  sigset_t signalset;
  {
    sigemptyset(&signalset);
    sigaddset(&signalset, SIGRTMIN+4);

    if(sigprocmask(SIG_BLOCK, &signalset, NULL) == -1) {
      sprintf(msg, "Unable to block directory change signal (SIGRTMIN+4): %s", 
	      strerror(errno));
      env->ThrowNew(IOException, msg);
      return -1;
    }
  }

  /* try to raise the maximum number of file descriptors which can be monitored */ 
  jint maxfiles = 0;
  {
    struct rlimit rlim;
    if(getrlimit(RLIMIT_NOFILE, &rlim) == -1) {
      sprintf(msg, "Unable to determine the monitored directory limit: %s", 
	      strerror(errno));
      env->ThrowNew(IOException, msg);
      return -1;
    }
    
    maxfiles = rlim.rlim_cur;
  }

  return maxfiles;
}



/* Monitor changes to the given directory. 
     Returns the file descriptor of the monitored directory. */ 
extern "C" 
JNIEXPORT jint 
JNICALL Java_us_temerity_pipeline_core_DNotify_monitorNative
(
 JNIEnv* env, 
 jobject obj, 
 jstring jdir
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("DNotify.monitorNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* repackage the arguments */ 
  const char *dir = NULL; 
  {
    dir = env->GetStringUTFChars(jdir, 0);      
    if((dir == NULL) || (strlen(dir) == 0)) {
      env->ThrowNew(IOException,"Empty directory!");
      return -1;
    }	
  }

  /* get the file descriptor */ 
  int fd = open(dir, O_RDONLY);
  if(fd == -1) {
    sprintf(msg, "Unable to open directory: %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return -1;
  }

  /* setup monitoring */ 
  if(fcntl(fd, F_SETSIG, SIGRTMIN+4) == -1) {
    sprintf(msg, "Unable to set signal (SIGRTMIN+4): %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return -1;
  }

  long args = DN_MODIFY | DN_CREATE | DN_DELETE | DN_RENAME | DN_ATTRIB;
  if(fcntl(fd, F_NOTIFY, args | DN_MULTISHOT) == -1) {
    sprintf(msg, "Unable to set directory notification: %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return -1;
  }

  return fd;
}



/* Cancel monitoring of the directory with the given file descriptor. */ 
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_core_DNotify_unmonitorNative
(
 JNIEnv* env, 
 jobject obj, 
 jint fd
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("DNotify.unmonitorNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* cancel monitoring */ 
  if(fcntl(fd, F_NOTIFY, 0) == -1) {
    sprintf(msg, "Unable cancel directory notification for (%d): %s", 
	    fd, strerror(errno));
    env->ThrowNew(IOException, msg);
    return;
  }

  /* release the file descriptor */ 
  if(close(fd) == -1) {
    sprintf(msg, "Unable close directory (%d): %s", 
	    fd, strerror(errno));
    env->ThrowNew(IOException, msg);
    return;
  }
}



/* Wait for one of the monitored directories to be modified. */ 
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_core_DNotify_watchNative
(
 JNIEnv* env, 
 jobject obj, 
 jint jtimeout
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("DNotify.watchNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* listen for change signals */ 
  {
    sigset_t signalset;
    sigemptyset(&signalset);
    sigaddset(&signalset, SIGRTMIN+4);

    timespec timeout;
    timeout.tv_sec  = (long) (jtimeout / 1000);
    timeout.tv_nsec = ((long) (jtimeout % 1000)) * 1000000;

    siginfo_t sinfo;
    int signal = sigtimedwait(&signalset, &sinfo, &timeout);
    if(signal == -1) {
      switch(errno) {
      case EAGAIN:
	return -2;

      default:
	sprintf(msg, "Bad signal: %s", strerror(errno));
	env->ThrowNew(IOException, msg);
	return -1;   
      }
    }
    else if(signal == (SIGRTMIN+4)) {
      return (sinfo.si_fd);
    }
  }
  
  return -1;
}
