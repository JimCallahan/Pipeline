// $Id: NativeProcess.cc,v 1.5 2004/02/25 01:27:09 jim Exp $

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

#ifdef HAVE_STDLIB_H
#  include <stdlib.h>
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

#include "NativeProcess.hh"

 

/* Write the given string data to the STDIN of the native process. */ 
extern "C" 
JNIEXPORT jint 
JNICALL Java_us_temerity_pipeline_NativeProcess_writeToStdIn
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
    perror("NativeProcess.writeToStdIn(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return -1;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdInFileDesc");
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
JNICALL Java_us_temerity_pipeline_NativeProcess_closeStdIn
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
    perror("NativeProcess.closeStdIn(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdInFileDesc");
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
JNICALL Java_us_temerity_pipeline_NativeProcess_readFromStdOut
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
    perror("NativeProcess.readFromStdOut(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return NULL;
  }

  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdOutFileDesc");
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
JNICALL Java_us_temerity_pipeline_NativeProcess_closeStdOut
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
    perror("NativeProcess.closeStdOut(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return;
  }
  
  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdOutFileDesc");
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
JNICALL Java_us_temerity_pipeline_NativeProcess_readFromStdErr
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
    perror("NativeProcess.readFromStdErr(), unable to lookup \"java/lang/IOException\"");
    return NULL;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return NULL;
  }

  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdErrFileDesc");
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
JNICALL Java_us_temerity_pipeline_NativeProcess_closeStdErr
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
    perror("NativeProcess.closeStdErr(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcess object's fields */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return;
  }
  
  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdErrFileDesc");
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


/* Native replacement for Runtime.exec().
     Returns the OS process ID of the started process on success. 
     Throws a PError exception on failure (or returns -1). */ 
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_NativeProcess_execNative
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
    perror("NativeProcess.execNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }


  /* get handles for the NativeProcess object's fields/methods */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return -1;
  }
    
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdInFileDesc");
    return -1;
  }
    
  jfieldID pStdOutFileDesc = env->GetFieldID(NativeProcessClass, "pStdOutFileDesc", "I");
  if(pStdOutFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdOutFileDesc");
    return -1;
  }
  
  jfieldID pStdErrFileDesc = env->GetFieldID(NativeProcessClass, "pStdErrFileDesc", "I");
  if(pStdErrFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pStdErrFileDesc");
    return -1;
  }

  jfieldID pUserSecs = env->GetFieldID(NativeProcessClass, "pUserSecs", "J");
  if(pUserSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pUserSecs");
    return -1;
  }

  jfieldID pUserMSecs = env->GetFieldID(NativeProcessClass, "pUserMSecs", "J");
  if(pUserMSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pUserMSecs");
    return -1;
  }

  jfieldID pSystemSecs = env->GetFieldID(NativeProcessClass, "pSystemSecs", "J");
  if(pSystemSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pSystemSecs");
    return -1;
  }

  jfieldID pSystemMSecs = env->GetFieldID(NativeProcessClass, "pSystemMSecs", "J");
  if(pSystemMSecs == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pSystemMSecs");
    return -1;
  }

  jfieldID pPageFaults = env->GetFieldID(NativeProcessClass, "pPageFaults", "J");
  if(pPageFaults == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pPageFaults");
    return -1;
  }
  
  jmethodID setPid = env->GetMethodID(NativeProcessClass, "setPid", "(I)V");
  if(setPid == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.setPid()");
    return -1;
  }
  
  jmethodID setIsRunning = env->GetMethodID(NativeProcessClass, "setIsRunning", "(Z)V");
  if(setIsRunning == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.setIsRunning()");
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
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the READ side of the pipe up to STDIN */ 
	  if(dup(pipeIn[0]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDIN to the pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDIN connected */ 
	  if((close(pipeIn[0]) == -1) || (close(pipeIn[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDIN pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* connect the STDOUT pipe */ 
	{
	  /* close the default STDOUT */ 
	  if(close(1) == -1) {
	    sprintf(msg, "%s, unable to close child STDOUT (%s)",
		    "NativeProcess.execNative()", "before connecting it to the pipe");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the WRITE side of the pipe up to STDOUT */ 
	  if(dup(pipeOut[1]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDOUT to the pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDOUT connected */ 
	  if((close(pipeOut[0]) == -1) || (close(pipeOut[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDOUT pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* connect the STDERR pipe */ 
	{
	  /* close the default STDERR */ 
	  if(close(2) == -1) {
	    sprintf(msg, "%s, unable to close child STDERR (%s)"
		    "NativeProcess.execNative()", "before connecting it to the pipe");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the WRITE side of the pipe up to STDERR */ 
	  if(dup(pipeErr[1]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDERR to the pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDERR connected */ 
	  if((close(pipeErr[0]) == -1) || (close(pipeErr[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDERR pipe",
		    "NativeProcess.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* change to the working directory */ 
	if(chdir(dir) == -1) {
	  sprintf(msg, "%s, unable change directory to \"%s\"", 
		  "NativeProcess.execNative()", dir);
	  perror(msg);
	  exit(EXIT_FAILURE);    
	}

	/* put this process into its own process group */ 
	if(setsid() == -1) {
	  sprintf(msg, "%s, unable to create a new process group for the child process", 
		  "NativeProcess.execNative()");
	  perror(msg);
	  exit(EXIT_FAILURE);    	    
	}

	/* overlay the process */ 
	execve(cmdarray[0], cmdarray, envp);

	/* execve() NEVER returns if successful */ 
	sprintf(msg, "%s, unable to execute \"%s\"", 
		"NativeProcess.execNative()", cmdarray[0]);
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



/* Resource usage statistics for running native process. 
       Returns whether the collection was successful. */ 
JNIEXPORT jboolean 
JNICALL Java_us_temerity_pipeline_NativeProcess_collectStatsNative
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
    perror("NativeProcess.waitNative(), unable to lookup \"java/lang/IOException\"");
    return false;
  }

  /* get handles for the NativeProcess object's fields/methods */ 
  jclass NativeProcessClass = env->GetObjectClass(obj);  
  if(NativeProcessClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcess");
    return false;
  }
    
  jfieldID pAvgVMem = env->GetFieldID(NativeProcessClass, "pAvgVMem", "J");
  if(pAvgVMem == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pAvgVMem");
    return false;
  }

  jfieldID pMaxVMem = env->GetFieldID(NativeProcessClass, "pMaxVMem", "J");
  if(pMaxVMem == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pMaxVMem");
    return false;
  }

  jfieldID pAvgResMem = env->GetFieldID(NativeProcessClass, "pAvgResMem", "J");
  if(pAvgResMem == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pAvgResMem");
    return false;
  }

  jfieldID pMaxResMem = env->GetFieldID(NativeProcessClass, "pMaxResMem", "J");
  if(pMaxResMem == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pMaxResMem");
    return false;
  }

  jfieldID pMemSamples = env->GetFieldID(NativeProcessClass, "pMemSamples", "J");
  if(pMemSamples == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.pMemSamples");
    return false;
  }

  jmethodID getPid = env->GetMethodID(NativeProcessClass, "getPid", "()I");
  if(getPid == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcess.getPid()");
    return false;
  }


  /* get the process ID */ 
  jint pid         = env->CallIntMethod(obj, getPid);
  jlong avgVMem    = env->GetLongField(obj, pAvgVMem);
  jlong maxVMem    = env->GetLongField(obj, pMaxVMem);
  jlong avgResMem  = env->GetLongField(obj, pAvgResMem);
  jlong maxResMem  = env->GetLongField(obj, pMaxResMem);
  jlong memSamples = env->GetLongField(obj, pMemSamples);

  /* determine the size of memory pages */ 
  long psize = (long) getpagesize(); 
  if(psize <= 0) {
    env->ThrowNew(IOException, "cannot determine the page size!");
    return false;
  }

  /* open the statistics psuedo-file for the process */ 
  {
    char path[1024];
    sprintf(path, "/proc/%d/statm", pid);
    FILE* file = fopen(path, "r");
    if(file == NULL) 
      return false;

    long vmem, rss;
    int matches = 
      fscanf(file, "%ld %ld %*s %*s %*s %*s %*s", &vmem, &rss);
    fclose(file);
    if(matches != 2) {
      sprintf(msg, "internal error: %s", strerror(errno));
      env->ThrowNew(IOException, msg);
      return false;
    }
    vmem = vmem / 1024;
    rss  = rss * (psize / 1024);

    avgVMem += vmem;
    if(maxVMem < vmem)
      maxVMem = vmem;

    avgResMem += rss;
    if(maxResMem < rss)
      maxResMem = rss;

    memSamples++;
  }

  /* set statistics fields */ 
  env->SetLongField(obj, pAvgVMem, avgVMem);
  env->SetLongField(obj, pMaxVMem, maxVMem);
  env->SetLongField(obj, pAvgResMem, avgResMem);
  env->SetLongField(obj, pMaxResMem, maxResMem);
  env->SetLongField(obj, pMemSamples, memSamples);

  return true;
}



/* Send a signal to a native process. */ 
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcess_signalNative
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
    perror("NativeProcess.signalNative(), unable to lookup \"java/lang/IOException\"");
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


/* Change file access permissions. */
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcess_chmodNative
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
    perror("NativeProcess.signalNative(), unable to lookup \"java/lang/IOException\"");
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
 
