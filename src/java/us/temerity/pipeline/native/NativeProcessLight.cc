// $Id: NativeProcessLight.cc,v 1.1 2004/10/28 15:55:24 jim Exp $

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

#include "NativeProcessLight.hh"


/* Write the given string data to the STDIN of the native process. */ 
extern "C" 
JNIEXPORT jint 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_writeToStdIn
(
 JNIEnv *env, 
 jobject obj, 
 jstring jinput
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.writeToStdIn(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return -1;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdInFileDesc");
    return -1;
  }

  /* repackage the arguments */ 
  const char* input = env->GetStringUTFChars(jinput, 0);
  if((input == NULL) || (strlen(input) == 0)) {
    env->ThrowNew(IOException,"empty input string");
    return -1;
  }

  /* get the STDIN file descriptor */ 
  jint stdin = env->GetIntField(obj, pStdInFileDesc);
  
  /* write the string */ 
  size_t bytes = write(stdin, input, strlen(input));
  if(bytes == -1) {
    sprintf(msg, "write to child STDIN failed: %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return -1;
  }
  return bytes;
}


/* Close the STDIN pipe. */ 
extern "C"  
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdIn
(
 JNIEnv *env, 
 jobject obj
)
{  
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.closeStdIn(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdInFileDesc");
    return;
  }

  /* get the STDIN file descriptor */ 
  jint stdin = env->GetIntField(obj, pStdInFileDesc);

  /* close the STDIN pipe */ 
  if(close(stdin) == -1) {
    sprintf(msg, "unable to close the parent STDIN pipe: %s", 
	    strerror(errno));
    env->ThrowNew(IOException, msg);
  }
}


/* Read up to the given number of characters from the STDOUT of the native process. 
    Returns (null) on EOF.  The size of the String read may be smaller than "size" */ 
extern "C"  
JNIEXPORT jstring 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_readFromStdOut
(
 JNIEnv *env, 
 jobject obj, 
 jint size
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.readFromStdOut(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return NULL;
  }

  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdOutFileDesc");
    return NULL;
  }
  
  /* get the STDOUT file descriptor */ 
  jint stdout = env->GetIntField(obj, pStdOutFileDesc);

  /* read the STDOUT */ 
  char* buf = new char[size+1];
  ssize_t bytes = read(stdout, buf, size);
  if(bytes == -1) {
    sprintf(msg, "failed to read from parent STDOUT pipe: %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return NULL;
  }
  else if(bytes == 0) {
    return NULL;
  }
  
  buf[bytes] = '\0';
  return (env->NewStringUTF(buf));
}


/* Close the STDOUT pipe. */ 
extern "C"  
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdOut
(
 JNIEnv *env, 
 jobject obj
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.closeStdOut(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return;
  }
  
  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdOutFileDesc");
    return;
  }

  /* get the STDOUT file descriptor */ 
  jint stdout = env->GetIntField(obj, pStdOutFileDesc);

  /* close the STDOUT pipe */ 
  if(close(stdout) == -1) {
    sprintf(msg, "unable to close the parent STDOUT pipe: %s", 
	    strerror(errno));
    env->ThrowNew(IOException, msg);
  }
}


/* Read up to the given number of characters from the STDERR of the native process. 
     Returns (null) on EOF.  The size of the String read may be smaller than "size" */ 
extern "C"  
JNIEXPORT jstring 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_readFromStdErr
(
 JNIEnv *env, 
 jobject obj, 
 jint size
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.readFromStdErr(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return NULL;
  }

  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdErrFileDesc");
    return NULL;
  }
  
  /* get the STDERR file descriptor */ 
  jint stderr = env->GetIntField(obj, pStdErrFileDesc);

  /* read the STDERR */ 
  char* buf = new char[size+1];
  ssize_t bytes = read(stderr, buf, size);
  if(bytes == -1) {
    sprintf(msg, "failed to read from parent STDERR pipe: %s", strerror(errno));
    env->ThrowNew(IOException, msg);
    return NULL;
  }
  else if(bytes == 0) {
    return NULL;
  }
  
  buf[bytes] = '\0';
  return (env->NewStringUTF(buf));
}


/* Close the STDERR pipe. */ 
extern "C"  
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_closeStdErr
(
 JNIEnv *env, 
 jobject obj
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.closeStdErr(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcessLight object's fields */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return;
  }
  
  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdErrFileDesc");
    return;
  }

  /* get the STDERR file descriptor */ 
  jint stderr = env->GetIntField(obj, pStdErrFileDesc);

  /* close the STDERR pipe */ 
  if(close(stderr) == -1) {
    sprintf(msg, "unable to close the parent STDERR pipe: %s", 
	    strerror(errno));
    env->ThrowNew(IOException, msg);
  }
}


/* Send a signal to a native process. */ 
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessLight_signalNative
(
 JNIEnv *env, 
 jobject obj, 
 jint signal,  /* IN: POSIX signal */ 
 jint pid      /* IN: POSIX signal */ 
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.signalNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* send the signal */ 
  if(kill(pid, signal) == -1) {
    sprintf(msg, "failed send signal (%d) to process (%d): %s\n", 
	    signal, pid, strerror(errno));
    env->ThrowNew(IOException, msg);
    return; 
  }
}
 

/* Native replacement for Runtime.exec().
     Returns the OS process ID of the started process on success. 
     Throws a PError exception on failure (or returns -1). */ 
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_NativeProcessLight_execNativeLight
(
 JNIEnv *env, 
 jobject obj, 
 jobjectArray jcmdarray,   /* IN: command[0] and arguments[1+] */ 		  
 jobjectArray jenvp,	   /* IN: environmental variable name=value pairs */  
 jstring jdir		   /* IN: the working directory */                    
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessLight.execNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }

  /* get handles for the NativeProcessLight object's fields/methods */ 
  jclass NativeProcessLightClass = env->GetObjectClass(obj);  
  if(NativeProcessLightClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessLight");
    return -1;
  }

  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdInFileDesc");
    return -1;
  }
    
  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdOutFileDesc");
    return -1;
  }

  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessLightClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pStdErrFileDesc");
    return -1;
  }

  jfieldID pUserSecs = env->GetFieldID(NativeProcessLightClass, "pUserSecs", "J");
  if(pUserSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pUserSecs");
    return -1;
  }

  jfieldID pUserMSecs = env->GetFieldID(NativeProcessLightClass, "pUserMSecs", "J");
  if(pUserMSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pUserMSecs");
    return -1;
  }

  jfieldID pSystemSecs = env->GetFieldID(NativeProcessLightClass, "pSystemSecs", "J");
  if(pSystemSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pSystemSecs");
    return -1;
  }

  jfieldID pSystemMSecs = env->GetFieldID(NativeProcessLightClass, "pSystemMSecs", "J");
  if(pSystemMSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pSystemMSecs");
    return -1;
  }

  jfieldID pPageFaults = env->GetFieldID(NativeProcessLightClass, "pPageFaults", "J");
  if(pPageFaults == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pPageFaults");
    return -1;
  }
  
  jmethodID setPid = env->GetMethodID(NativeProcessLightClass, "setPid", "(I)V");
  if(setPid == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.setPid()");
    return -1;
  }
  
  jmethodID setIsRunning = env->GetMethodID(NativeProcessLightClass, "setIsRunning", "(Z)V");
  if(setIsRunning == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.setIsRunning()");
    return -1;
  }
  
  /* repackage the arguments */ 
  const char *dir = NULL; 
  char** cmdarray = NULL;
  char** envp = NULL;
  {
    {
      dir = env->GetStringUTFChars(jdir, 0);      
      if((dir == NULL) || (strlen(dir) == 0)) {
	env->ThrowNew(IOException,"empty working directory");
	return -1;
      }	

      struct stat buf;
      if(stat(dir, &buf) == -1) {
	sprintf(msg, "stat failed for \"%s\": %s", dir, strerror(errno));
	env->ThrowNew(IOException, msg);
	return -1;
      }
      else if(!S_ISDIR(buf.st_mode)) {
	sprintf(msg, "illegal working directory \"%s\"", dir);
	env->ThrowNew(IOException, msg);
	return -1;
      }
    }

    {
      jsize len = env->GetArrayLength(jcmdarray);
      if(len == 0) {
	env->ThrowNew(IOException, "empty command arguments array");
	return -1;
      }

      cmdarray = new char*[len+1];
      jsize i;
      for(i=0; i<len; i++) {
	const char* arg = 
	  env->GetStringUTFChars((jstring) env->GetObjectArrayElement(jcmdarray, i), 0);
	cmdarray[i] = strdup(arg);
      }
      cmdarray[i] = NULL;
    }

    {
      jsize len = env->GetArrayLength(jenvp);

      envp = new char*[len+1];      
      jsize i;
      for(i=0; i<len; i++) {
	const char* keyval = 
	  env->GetStringUTFChars((jstring) env->GetObjectArrayElement(jenvp, i), 0);
	envp[i] = strdup(keyval);
      }
      envp[i] = NULL;
    }
  }
  
  /* create pipes to communicate with the child process */ 
  int pipeIn[2];
  int pipeOut[2];
  int pipeErr[2];
  {
    if(pipe(pipeIn) == -1) {
      sprintf(msg, "unable to create the STDIN pipe: %s", dir, strerror(errno));
      env->ThrowNew(IOException, msg);
      return -1;
    }

    if(pipe(pipeOut) == -1) {
      sprintf(msg, "unable to create the STDOUT pipe: %s", dir, strerror(errno));
      env->ThrowNew(IOException, msg);
      return -1;
    }

    if(pipe(pipeErr) == -1) {
      sprintf(msg, "unable to create the STDERR pipe: %s", dir, strerror(errno));
      env->ThrowNew(IOException, msg);
      return -1;
    }
  }

  /* fork a process */ 
  {
    pid_t pid = fork();
    switch(pid) {
    case -1:  
      /* failure */ 
      sprintf(msg, "unable to fork thread for \"%s\": %s\n", cmdarray[0]);
      env->ThrowNew(IOException, msg);
      return -1;

    case 0: 
      /* child process */ 
      {
	/* connect the STDIN pipe */ 
	{
	  /* close the default STDIN */ 
	  if(close(0) == -1) {
	    sprintf(msg, "%s, unable to close child STDIN (before connecting it to the pipe)",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the READ side of the pipe up to STDIN */ 
	  if(dup(pipeIn[0]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDIN to the pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDIN connected */ 
	  if((close(pipeIn[0]) == -1) || (close(pipeIn[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDIN pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* connect the STDOUT pipe */ 
	{
	  /* close the default STDOUT */ 
	  if(close(1) == -1) {
	    sprintf(msg, "%s, unable to close child STDOUT (%s)",
		    "NativeProcessLight.execNative()", "before connecting it to the pipe");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the WRITE side of the pipe up to STDOUT */ 
	  if(dup(pipeOut[1]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDOUT to the pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDOUT connected */ 
	  if((close(pipeOut[0]) == -1) || (close(pipeOut[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDOUT pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* connect the STDERR pipe */ 
	{
	  /* close the default STDERR */ 
	  if(close(2) == -1) {
	    sprintf(msg, "%s, unable to close child STDERR (%s)"
		    "NativeProcessLight.execNative()", "before connecting it to the pipe");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the WRITE side of the pipe up to STDERR */ 
	  if(dup(pipeErr[1]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDERR to the pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDERR connected */ 
	  if((close(pipeErr[0]) == -1) || (close(pipeErr[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDERR pipe",
		    "NativeProcessLight.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* change to the working directory */ 
	if(chdir(dir) == -1) {
	  sprintf(msg, "%s, unable change directory to \"%s\"", 
		  "NativeProcessLight.execNative()", dir);
	  perror(msg);
	  exit(EXIT_FAILURE);    
	}

	/* put this process into its own process group */ 
	if(setsid() == -1) {
	  sprintf(msg, "%s, unable to create a new process group for the child process", 
		  "NativeProcessLight.execNative()");
	  perror(msg);
	  exit(EXIT_FAILURE);    	    
	}

	/* overlay the process */ 
	execve(cmdarray[0], cmdarray, envp);

	/* execve() NEVER returns if successful */ 
	sprintf(msg, "%s, unable to execute \"%s\"", 
		"NativeProcessLight.execNative()", cmdarray[0]);
	perror(msg);
	exit(EXIT_FAILURE);    
      }
    }

    /* parent process */
    {      
      /* close the READ side of the STDIN pipe */ 
      if(close(pipeIn[0]) == -1) {
	sprintf(msg, "unable to close the READ side of the parent STDIN pipe: %s", 
		strerror(errno));
	env->ThrowNew(IOException, msg);
	kill(pid, 9);
	return -1;
      }

      /* close the WRITE side of the STDOUT pipe */ 
      if(close(pipeOut[1]) == -1) {
	sprintf(msg, "unable to close the WRITE side of the parent STDOUT pipe: %s", 
		strerror(errno));
	env->ThrowNew(IOException, msg);
	kill(pid, 9);
	return -1;
      }

      /* close the WRITE side of the STDERR pipe */ 
      if(close(pipeErr[1]) == -1) {
	sprintf(msg, "unable to close the WRITE side of the parent STDERR pipe: %s", 
		strerror(errno));
	env->ThrowNew(IOException, msg);
	kill(pid, 9);
	return -1;
      }

      /* set the process ID */ 
      env->CallVoidMethod(obj, setPid, pid);

      /* set IO fields for the parent ends of the pipes */ 
      env->SetIntField(obj, pStdInFileDesc, pipeIn[1]);
      env->SetIntField(obj, pStdOutFileDesc, pipeOut[0]);
      env->SetIntField(obj, pStdErrFileDesc, pipeErr[0]);

      /* let Java know that the process is running */ 
      env->CallVoidMethod(obj, setIsRunning, true);
      
      /* wait on the process */ 
      {
	int status = 0;
	struct rusage ru;
	ru.ru_utime.tv_sec  = -1;
	ru.ru_utime.tv_usec = -1;
	ru.ru_stime.tv_sec  = -1;
	ru.ru_stime.tv_usec = -1;
	ru.ru_majflt        = -1;
	
	pid_t epid = wait4(pid, &status, 0, &ru);

	/* let Java know that the process has exited */ 
	env->CallVoidMethod(obj, setIsRunning, false);
	
	if(epid == -1) {
	  sprintf(msg, "wait failed for child process (%d): %s\n", 
		  pid, strerror(errno));  
	  env->ThrowNew(IOException, msg);
	  return -1;
	}
	else if(WIFEXITED(status)) {
	  env->SetLongField(obj, pUserSecs, ru.ru_utime.tv_sec);
	  env->SetLongField(obj, pUserMSecs, ru.ru_utime.tv_usec);
	  
	  env->SetLongField(obj, pSystemSecs, ru.ru_stime.tv_sec);
	  env->SetLongField(obj, pSystemMSecs, ru.ru_stime.tv_usec);
	  
	  env->SetLongField(obj, pPageFaults, ru.ru_majflt);
	  
	  return WEXITSTATUS(status);
	}
	else if(WIFSIGNALED(status)) {
	  sprintf(msg, "child process (%d) terminated with signal (%d)!", 
		  pid, WTERMSIG(status));
	  env->ThrowNew(IOException, msg);
	  return -1;      
	}
	else if(WIFSTOPPED(status)) {
	  sprintf(msg, "child process (%d) stopped with signal (%d)!", 
		  pid, WSTOPSIG(status));
	  env->ThrowNew(IOException, msg);
	  return -1;      
	}
	else {
	  sprintf(msg, "exited process (%d) was NOT the one waited on (%d)!", epid, pid);
	  env->ThrowNew(IOException, msg);
	  return -1;
	}
      }
    }
  }
}


