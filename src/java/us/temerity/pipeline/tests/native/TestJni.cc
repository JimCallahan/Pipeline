// $Id: TestJni.cc,v 1.1 2004/02/12 15:50:12 jim Exp $

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

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
#endif


#include <jni.h>
#include "TestJniApp.h"


extern "C" 
JNIEXPORT void 
JNICALL Java_TestJniApp_displayHelloWorld
(
 JNIEnv *env, 
 jobject obj
)
{
  printf("Hello world!\n");
    return;
}


extern "C" 
JNIEXPORT jstring 
JNICALL Java_TestJniApp_toUpper
(
 JNIEnv *env, 
 jobject obj, 
 jstring jstr
)
{
  const char* str = env->GetStringUTFChars(jstr, 0);

  char* upstr = strdup(str);
  char* p = upstr;
  while(*p) {
    (*p) = (char) toupper((int) *p);
    p++;
  }
  
  return env->NewStringUTF(upstr);
}


extern "C" 
JNIEXPORT jint 
JNICALL Java_TestJniApp_exec
(
 JNIEnv *env, 
 jobject obj, 
 jobjectArray jcmdarray,
 jobjectArray jenvp,
 jstring jdir
)
{
  /* unpack the arguments */ 
  const char *dir = env->GetStringUTFChars(jdir, 0);

  char** cmdarray = NULL;
  {
    jsize len = env->GetArrayLength(jcmdarray);
    cmdarray = new char*[len+1];

    jsize i;
    for(i=0; i<len; i++) {
      const char* arg = 
	env->GetStringUTFChars((jstring) env->GetObjectArrayElement(jcmdarray, i), 0);
      cmdarray[i] = strdup(arg);
    }
    cmdarray[i] = NULL;
  }

  char** envp = NULL;
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
  



  // DEBUG
  printf("Current Working Directory: %s\n", dir);

  printf("Cmd: %s\n", cmdarray[0]);

  {
    printf("Args:\n");
    char** arg = cmdarray;
    while((*arg) != NULL) {
      printf("  %s\n", *arg);
      arg++;
    }
  }

  {
    printf("Environment:\n");
    char** keyval = envp;
    while((*keyval) != NULL) {
      printf("  %s\n", *keyval);
      keyval++;
    }
  }
  // DEBUG
  

  /* fork a process */ 
  pid_t pid = fork();
  switch(pid) {
  /* failure */ 
  case -1:  
    {
      printf("Unable to fork thread for \"%s\": %s\n", cmdarray[0], strerror(errno));
      return -1;
    }

  /* child process */ 
  case 0: 
    if(execve(cmdarray[0], cmdarray, envp) == -1) {
      printf("Unable to execute \"%s\": %s\n", cmdarray[0], strerror(errno));
      exit(EXIT_FAILURE);    
    }

  /* parent process */ 
  default: 
    return pid;
  }
}

extern "C" 
JNIEXPORT jint 
JNICALL Java_TestJniApp_wait
(
 JNIEnv *env, 
 jobject obj, 
 jint pid
)
{
  int status = 0;
  if(waitpid(pid, &status, 0) == -1) {
    printf("Failed to wait for child process (%d): %s\n", pid, strerror(errno));
    return -1;
  }
  
  if(WIFEXITED(status)) 
    return WEXITSTATUS(status);
  else 
    return -1;
}
