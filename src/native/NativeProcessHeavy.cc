// $Id: NativeProcessHeavy.cc,v 1.5 2007/02/12 19:19:05 jim Exp $

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

#include "NativeProcessHeavy.hh"


/* Write the given string data to the STDIN of the native process. */ 
extern "C" 
JNIEXPORT jint 
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_writeToStdIn
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
    perror("NativeProcessHeavy.writeToStdIn(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }
  
  /* get handles for the NativeProcessHeavy object's fields */ 
  jclass NativeProcessHeavyClass = env->GetObjectClass(obj);  
  if(NativeProcessHeavyClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessHeavy");
    return -1;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessHeavyClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pStdInFileDesc");
    return -1;
  }

  /* repackage the arguments */ 
  const char* input = env->GetStringUTFChars(jinput, 0);
  if((input == NULL) || (strlen(input) == 0)) {
    env->ThrowNew(IOException,"empty input string");
    return -1;
  }

  /* get the STDIN file descriptor */ 
  jint stdin_fd = env->GetIntField(obj, pStdInFileDesc);
  
  /* write the string */ 
  size_t bytes = write(stdin_fd, input, strlen(input));
  if(bytes == -1) {
    sprintf(msg, "write to child STDIN failed: %s", strerror(errno));
    env->ReleaseStringUTFChars(jinput, input);
    env->ThrowNew(IOException, msg);
    return -1;
  }

  env->ReleaseStringUTFChars(jinput, input);
  return bytes;
}


/* Close the STDIN pipe. */ 
extern "C"  
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_closeStdIn
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
    perror("NativeProcessHeavy.closeStdIn(), unable to lookup \"java/lang/IOException\"");
    return;
  }
  
  /* get handles for the NativeProcessHeavy object's fields */ 
  jclass NativeProcessHeavyClass = env->GetObjectClass(obj);  
  if(NativeProcessHeavyClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessHeavy");
    return;
  }
  
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessHeavyClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pStdInFileDesc");
    return;
  }

  /* get the STDIN file descriptor */ 
  jint stdin_fd = env->GetIntField(obj, pStdInFileDesc);

  /* close the STDIN pipe */ 
  if(close(stdin_fd) == -1) {
    sprintf(msg, "unable to close the parent STDIN pipe: %s", 
	    strerror(errno));
    env->ThrowNew(IOException, msg);
  }
}


/* Send a signal to a native process. */ 
extern "C" 
JNIEXPORT void 
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_signalNative
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
    perror("NativeProcessHeavy.signalNative(), unable to lookup \"java/lang/IOException\"");
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
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_execNativeHeavy
(
 JNIEnv *env, 
 jobject obj, 
 jstring juser,            /* IN: the user to impersonate (or NULL) */ 
 jstring jdomain,          /* IN: the domain of the user to impersonate (or NULL) */
 jcharArray jpassword,     /* IN: the user's password (or NULL) */     
 jobjectArray jcmdarray,   /* IN: command[0] and arguments[1+] */ 		  
 jobjectArray jenvp,	   /* IN: environmental variable name=value pairs */  
 jstring jdir,		   /* IN: the working directory */     
 jstring joutfile,	   /* IN: the file to which all STDOUT output is redirected */
 jstring jerrfile	   /* IN: the file to which all STDERR output is redirected */
)
{
  /* exception initialization */ 
  char msg[1024];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = ECANCELED;
    perror("NativeProcessHeavy.execNative(), unable to lookup \"java/lang/IOException\"");
    return -1;
  }


  /* get handles for the NativeProcessHeavy object's fields/methods */ 
  jclass NativeProcessHeavyClass = env->GetObjectClass(obj);  
  if(NativeProcessHeavyClass == 0) {
    env->ThrowNew(IOException, "unable to lookup class: NativeProcessHeavy");
    return -1;
  }
    
  jfieldID pStdInFileDesc = env->GetFieldID(NativeProcessHeavyClass, "pStdInFileDesc", "I");
  if(pStdInFileDesc == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pStdInFileDesc");
    return -1;
  }
    
  jfieldID pUTime = env->GetFieldID(NativeProcessHeavyClass, "pUTime", "J");
  if(pUTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pUTime");
    return -1;
  }

  jfieldID pSTime = env->GetFieldID(NativeProcessHeavyClass, "pSTime", "J");
  if(pSTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pSTime");
    return -1;
  }
  
  jfieldID pPageFaults = env->GetFieldID(NativeProcessHeavyClass, "pPageFaults", "J");
  if(pPageFaults == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pPageFaults");
    return -1;
  }
  
  jfieldID pVirtualSize = env->GetFieldID(NativeProcessHeavyClass, "pVirtualSize", "J");
  if(pVirtualSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pVirtualSize");
    return -1;
  }
  
  jfieldID pResidentSize = env->GetFieldID(NativeProcessHeavyClass, "pResidentSize", "J");
  if(pResidentSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pResidentSize");
    return -1;
  }
  
  jfieldID pSwappedSize = env->GetFieldID(NativeProcessHeavyClass, "pSwappedSize", "J");
  if(pSwappedSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.pSwappedSize");
    return -1;
  }
  
  jmethodID setPid = env->GetMethodID(NativeProcessHeavyClass, "setPid", "(I)V");
  if(setPid == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.setPid()");
    return -1;
  }
  
  jmethodID setIsRunning = env->GetMethodID(NativeProcessHeavyClass, "setIsRunning", "(Z)V");
  if(setIsRunning == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessHeavy.setIsRunning()");
    return -1;
  }
  

  /* repackage the arguments */ 
  const char *dir = NULL; 
  const char *outFile = NULL;
  const char *errFile = NULL;
  int cmdsize = 0;
  char** cmdarray = NULL;
  int envsize = 0;
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
	env->ReleaseStringUTFChars(jdir, dir);
	env->ThrowNew(IOException, msg);
	return -1;
      }
      else if(!S_ISDIR(buf.st_mode)) {
	sprintf(msg, "illegal working directory \"%s\"", dir);
	env->ReleaseStringUTFChars(jdir, dir);
	env->ThrowNew(IOException, msg);
	return -1;
      }
    }

    {
      outFile = env->GetStringUTFChars(joutfile, 0);      
      if((outFile == NULL) || (strlen(outFile) == 0)) {
	env->ReleaseStringUTFChars(jdir, dir);
	env->ThrowNew(IOException,"empty STDOUT output file");
	return -1;
      }    
    }

    {
      errFile = env->GetStringUTFChars(jerrfile, 0);      
      if((errFile == NULL) || (strlen(errFile) == 0)) {
	env->ReleaseStringUTFChars(jdir, dir);
	env->ReleaseStringUTFChars(joutfile, outFile);
	env->ThrowNew(IOException,"empty STDERR output file");
	return -1;
      }    
    }

    {
      jsize len = env->GetArrayLength(jcmdarray);
      if(len == 0) {
	env->ReleaseStringUTFChars(jdir, dir);
	env->ReleaseStringUTFChars(joutfile, outFile);
	env->ReleaseStringUTFChars(jerrfile, errFile);
	env->ThrowNew(IOException, "empty command arguments array");
	return -1;
      }

      cmdsize = len;
      cmdarray = new char*[len+1];
      jsize i;
      for(i=0; i<len; i++) {
	jstring s = (jstring) env->GetObjectArrayElement(jcmdarray, i);
	const char* arg = env->GetStringUTFChars(s, 0);
	cmdarray[i] = strdup(arg);
	env->ReleaseStringUTFChars(s, arg);
      }
      cmdarray[i] = NULL;
    }

    {
      jsize len = env->GetArrayLength(jenvp);

      envsize = len;
      envp = new char*[len+1];       
      jsize i;
      for(i=0; i<len; i++) {
	jstring s = (jstring) env->GetObjectArrayElement(jenvp, i);
	const char* keyval = env->GetStringUTFChars(s, NULL);
	envp[i] = strdup(keyval);
	env->ReleaseStringUTFChars(s, keyval);
      }
      envp[i] = NULL;
    }
  }
  
  /* create a pipe to communicate with the child process STDIN */ 
  int pipeIn[2];
  if(pipe(pipeIn) == -1) {
    {
      env->ReleaseStringUTFChars(jdir, dir);
      env->ReleaseStringUTFChars(joutfile, outFile);
      env->ReleaseStringUTFChars(jerrfile, errFile);
      
      jsize i;
      for(i=0; i<envsize; i++) 
	free(envp[i]);
      delete[] envp;
      
      for(i=0; i<cmdsize; i++) 
	free(cmdarray[i]);
      delete[] cmdarray;
    }

    sprintf(msg, "unable to create the STDIN pipe: %s", dir, strerror(errno));
    env->ThrowNew(IOException, msg);
    return -1;
  }

  /* fork a process */ 
  {
    pid_t pid = fork();
    switch(pid) {
    case -1:  
      /* failure */ 
      {
	env->ReleaseStringUTFChars(jdir, dir);
	env->ReleaseStringUTFChars(joutfile, outFile);
	env->ReleaseStringUTFChars(jerrfile, errFile);
	
	jsize i;
	for(i=0; i<envsize; i++) 
	  free(envp[i]);
	delete[] envp;
	
	for(i=0; i<cmdsize; i++) 
	  free(cmdarray[i]);
	delete[] cmdarray;
      }

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
	    sprintf(msg, "%s, unable to close child STDIN before connecting it to the pipe!",
		    "NativeProcessHeavy.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* hook the READ side of the pipe up to STDIN */ 
	  if(dup(pipeIn[0]) == -1) {
	    sprintf(msg, "%s, unable to connect child STDIN to the pipe",
		    "NativeProcessHeavy.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);	    
	  }

	  /* close the original pipe, leaving only the STDIN connected */ 
	  if((close(pipeIn[0]) == -1) || (close(pipeIn[1]) == -1)) {
	    sprintf(msg, "%s, unable to close down the original child STDIN pipe",
		    "NativeProcessHeavy.execNative()");
	    perror(msg);
	    exit(EXIT_FAILURE);    	    
	  }
	}

	/* redirect the STDOUT/STDERR to given output files */ 
	{
	  /* close the default STDOUT */ 
	  if(close(1) == -1) {
	    sprintf(msg, "%s, unable to close STDOUT %s (%s)!",
		    "NativeProcessHeavy.execNative()", 
		    "before redirecting it to the output file", outFile);
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* redirect the STDOUT to the newly opened file */ 
	  if(open(outFile, O_CREAT|O_WRONLY|O_TRUNC, 00644) != 1) {
	    sprintf(msg, "unable to redirect the STDOUT to the output file (%s)!", 
		    outFile, strerror(errno));
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* close the default STDERR */ 
	  if(close(2) == -1) {
	    sprintf(msg, "%s, unable to close STDERR %s (%s)!",
		    "NativeProcessHeavy.execNative()", 
		    "before redirecting it to the output file", errFile);
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	  
	  /* redirect the STDERR to the newly opened file */ 
	  if(open(errFile, O_CREAT|O_WRONLY|O_TRUNC, 00644) != 2) {
	    sprintf(msg, "unable to redirect the STDERR to the output file (%s)!", 
		    errFile, strerror(errno));
	    perror(msg);
	    exit(EXIT_FAILURE);    
	  }
	}
  
	/* change to the working directory */ 
	if(chdir(dir) == -1) {
	  sprintf(msg, "%s, unable change directory to \"%s\"", 
		  "NativeProcessHeavy.execNative()", dir);
	  perror(msg);
	  exit(EXIT_FAILURE);    
	}

	/* put this process into its own process group */ 
	if(setsid() == -1) {
	  sprintf(msg, "%s, unable to create a new process group for the child process", 
		  "NativeProcessHeavy.execNative()");
	  perror(msg);
	  exit(EXIT_FAILURE);    	    
	}

	/* overlay the process */ 
	execve(cmdarray[0], cmdarray, envp);

	/* execve() NEVER returns if successful */ 
	sprintf(msg, "%s, unable to execute \"%s\"", 
		"NativeProcessHeavy.execNative()", cmdarray[0]);
	perror(msg);
	exit(EXIT_FAILURE);    
      }
    }

    /* parent process */
    {   
      /* deallocated dynamic memory used to launch the process */ 
      {
	env->ReleaseStringUTFChars(jdir, dir);
	env->ReleaseStringUTFChars(joutfile, outFile);
	env->ReleaseStringUTFChars(jerrfile, errFile);

	jsize i;
	for(i=0; i<envsize; i++) 
	  free(envp[i]);
	delete[] envp;

	for(i=0; i<cmdsize; i++) 
	  free(cmdarray[i]);
	delete[] cmdarray;
      }      

      /* close the READ side of the STDIN pipe */ 
      if(close(pipeIn[0]) == -1) {
	sprintf(msg, "unable to close the READ side of the parent STDIN pipe: %s", 
		strerror(errno));
	env->ThrowNew(IOException, msg);
	kill(pid, 9);
	return -1;
      }

      /* set the process ID */ 
      env->CallVoidMethod(obj, setPid, pid);

      /* set IO fields for the parent ends of the pipes */ 
      env->SetIntField(obj, pStdInFileDesc, pipeIn[1]);

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
	ru.ru_majflt = -1;
	ru.ru_maxrss = -1;
	ru.ru_nswap  = -1;
	
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
	  env->SetLongField(obj, pUTime, 
			    ru.ru_utime.tv_sec*100 + ru.ru_utime.tv_usec/10000);
	  env->SetLongField(obj, pSTime, 
			    ru.ru_stime.tv_sec*100 + ru.ru_stime.tv_usec/10000);

	  long psize = (long) getpagesize(); 
	  if(psize <= 0) 
	    psize = 0L;

	  env->SetLongField(obj, pPageFaults, ru.ru_majflt);
	  env->SetLongField(obj, pVirtualSize, 0L); // FOR NOW...
	  env->SetLongField(obj, pResidentSize, ru.ru_maxrss * 1000L);
	  env->SetLongField(obj, pSwappedSize, ru.ru_nswap * psize);

// 	  printf("Page Faults: %ld\n", ru.ru_majflt);
// 	  printf("Resident Size: %ld (bytes)\n", ru.ru_maxrss * 1000L);
// 	  printf("Swapped Size: %ld (pages) %ld (bytes)\n", ru.ru_nswap, ru.ru_nswap * psize);

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


