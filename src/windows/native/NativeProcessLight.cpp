// $Id: NativeProcessLight.cpp,v 1.7 2007/03/18 02:17:17 jim Exp $

#include "stdafx.h"

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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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

  /* get the STDIN handle */ 
  HANDLE stdin_handle = (HANDLE) env->GetIntField(obj, pStdInFileDesc);

  /* write the string to STDIN */ 
  DWORD bytes; 
  if(!WriteFile(stdin_handle, input, (DWORD) strlen(input), &bytes, NULL)) {
    env->ReleaseStringUTFChars(jinput, input);
    env->ThrowNew(IOException, "write to child STDIN failed!");
    return -1;
  }
  
  env->ReleaseStringUTFChars(jinput, input);
  return ((jint) bytes);  
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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

  /* get the STDIN file handle */ 
  HANDLE stdin_handle = (HANDLE) env->GetIntField(obj, pStdInFileDesc);

  /* close the STDIN pipe */ 
  if(!CloseHandle(stdin_handle)) 
    env->ThrowNew(IOException, "unable to close the parent STDIN pipe!");
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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
  
  /* get the STDOUT file handle */ 
  HANDLE stdout_handle = (HANDLE) env->GetIntField(obj, pStdOutFileDesc);

  printf("Read STDOUT = %I32u\n", stdout_handle);

  /* read the STDOUT */ 
  char* buf = new char[size+1];
  DWORD bytes;
  if(!ReadFile(stdout_handle, buf, (DWORD) size, &bytes, NULL) || (bytes == 0)) {
    delete[] buf;
    return NULL;
  }
  
  buf[bytes] = '\0';
  printf("Read %I32u bytes from STDOUT: %s\n", bytes, buf);
  jstring rtn = env->NewStringUTF(buf);
  delete[] buf;
  return rtn;
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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

  /* get the STDOUT file handle */ 
  HANDLE stdout_handle = (HANDLE) env->GetIntField(obj, pStdOutFileDesc);

  printf("Close STDOUT = %I32u\n", stdout_handle);

  /* close the STDOUT pipe */ 
  if(!CloseHandle(stdout_handle)) 
    env->ThrowNew(IOException, "unable to close the parent STDOUT pipe!");
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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
  
  /* get the STDERR file handle */ 
  HANDLE stderr_handle = (HANDLE) env->GetIntField(obj, pStdErrFileDesc);

  printf("Read STDERR = %I32u\n", stderr_handle);

  /* read the STDERR */ 
  char* buf = new char[size+1];
  DWORD bytes;
  if(!ReadFile(stderr_handle, buf, (DWORD) size, &bytes, NULL) || (bytes == 0)){
    delete[] buf;
    return NULL;
  }

  buf[bytes] = '\0';
  printf("Read %I32u bytes from STDERR: %s\n", bytes, buf);
  jstring rtn = env->NewStringUTF(buf);
  delete[] buf;
  return rtn;
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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

  /* get the STDERR file handle */ 
  HANDLE stderr_handle = (HANDLE) env->GetIntField(obj, pStdErrFileDesc);

  printf("Close STDERR = %I32u\n", stderr_handle);

  /* close the STDERR pipe */ 
  if(!CloseHandle(stderr_handle)) 
    env->ThrowNew(IOException, "unable to close the parent STDERR pipe!");
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
  jclass IOException = env->FindClass("java/io/IOException");
  char msg[1024];
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeProcessLight.signalNative(), unable to lookup \"java/lang/IOException\"");
    return;
  }

  /* give this process the ability to kill any process owned by anyone! */ 
  {
    /* get the token for the current process */ 
    HANDLE token;
    if(!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES|TOKEN_QUERY, &token)) {
      env->ThrowNew(IOException, "unable to get token for current process!");
      return;
    }

    /* lookup the debug programs privilege */ 
    LUID luid;
    if(!LookupPrivilegeValue(NULL, "SeDebugPrivilege", &luid)) {
      env->ThrowNew(IOException, "debug privilege lookup failed!");
      return;
    }
    
    /* enable the privilege */ 
    TOKEN_PRIVILEGES tp;
    tp.PrivilegeCount = 1;
    tp.Privileges[0].Luid = luid;
    tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

    if(!AdjustTokenPrivileges(token, FALSE, &tp, sizeof(TOKEN_PRIVILEGES), 
                              (PTOKEN_PRIVILEGES) NULL, (PDWORD) NULL)) {
      env->ThrowNew(IOException, "adjust token privileges failed!");
      return;
    } 

    CloseHandle(token);
  }

  /* get the process handle */ 
  HANDLE proc = OpenProcess(PROCESS_TERMINATE, 0, (DWORD) pid);
  if(proc == NULL) {
    sprintf(msg, "unable to open handle for process (%d)", pid);
    env->ThrowNew(IOException, msg);
  }

  /* kill the process */ 
  switch(signal) {
  case 9:  /* SIGKILL */
    if(!TerminateProcess(proc, -1)) {
      sprintf(msg, "failed send signal (%d) to process (%d)", signal, pid);
      env->ThrowNew(IOException, msg);
      return; 
    }
    break;

  default:
    sprintf(msg, "signal (%d) not supported for Windows processes", pid);
    env->ThrowNew(IOException, msg);
    return;
  }

  /* cleanup process handles */ 
  CloseHandle(proc);
}


/* Native replacement for Runtime.exec().
   Returns the OS process ID of the started process on success. 
   Throws a IOException on failure (or returns -1). */ 
extern "C" 
JNIEXPORT jint
JNICALL Java_us_temerity_pipeline_NativeProcessLight_execNativeLight
(
 JNIEnv *env, 
 jobject obj, 
 jobjectArray jcmdarray,   /* IN: command[0] and arguments[1+] */                 
 jobjectArray jenvp,       /* IN: environmental variable name=value pairs */  
 jstring jdir              /* IN: the working directory */  
)
{
  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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
 
  jfieldID pUTime = env->GetFieldID(NativeProcessLightClass, "pUTime", "J");
  if(pUTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pUTime");
    return -1;
  }

  jfieldID pSTime = env->GetFieldID(NativeProcessLightClass, "pSTime", "J");
  if(pSTime == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pSTime");
    return -1;
  }
  
  jfieldID pPageFaults = env->GetFieldID(NativeProcessLightClass, "pPageFaults", "J");
  if(pPageFaults == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pPageFaults");
    return -1;
  }
  
  jfieldID pVirtualSize = env->GetFieldID(NativeProcessLightClass, "pVirtualSize", "J");
  if(pVirtualSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pVirtualSize");
    return -1;
  }
  
  jfieldID pResidentSize = env->GetFieldID(NativeProcessLightClass, "pResidentSize", "J");
  if(pResidentSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pResidentSize");
    return -1;
  }
  
  jfieldID pSwappedSize = env->GetFieldID(NativeProcessLightClass, "pSwappedSize", "J");
  if(pSwappedSize == 0) {
    env->ThrowNew(IOException, "unable to access: NativeProcessLight.pSwappedSize");
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
  const char* dir = NULL; 
  char* cmdline = NULL; 
  TCHAR envbuf[32768]; 
  {
    {
      dir = env->GetStringUTFChars(jdir, 0);      
      if((dir == NULL) || (strlen(dir) == 0)) {
        env->ThrowNew(IOException,"empty working directory");
        return -1;
      } 

      printf("Dir = %s\n", dir);    // DEBUG

      struct stat buf;
      if(stat(dir, &buf) == -1) {
        sprintf(msg, "stat failed for \"%s\": %s", dir, strerror(errno));
        env->ReleaseStringUTFChars(jdir, dir);

        env->ThrowNew(IOException, msg);
        return -1;
      }
      else if(!(buf.st_mode & _S_IFDIR)) {
        sprintf(msg, "illegal working directory \"%s\"", dir);
        env->ReleaseStringUTFChars(jdir, dir);

        env->ThrowNew(IOException, msg);
        return -1;
      }
    }

    printf("Dir is OK\n");      // DEBUG

    {
      jsize len = env->GetArrayLength(jcmdarray);
      if(len == 0) {
        env->ReleaseStringUTFChars(jdir, dir);

        env->ThrowNew(IOException, "empty command arguments array");
        return -1;
      }

      jsize i;
      size_t csize = 0; 
      for(i=0; i<len; i++) {    
        jstring s = (jstring) env->GetObjectArrayElement(jcmdarray, i);
        const char* arg = env->GetStringUTFChars(s, NULL);
        csize += strlen(arg) + ((i == 0) ? 2 : 1);
        env->ReleaseStringUTFChars(s, arg);     
      }

      printf("Cmdline Size = %d\n", csize);     // DEBUG

      if(csize >= 32767) {
        env->ReleaseStringUTFChars(jdir, dir);

        env->ThrowNew(IOException, "command line exceeds 32K limit on Windows!");
        return -1; 
      }

      cmdline = new char[csize+1];
      cmdline[0] = '\0';
      for(i=0; i<len; i++) {
        jstring s = (jstring) env->GetObjectArrayElement(jcmdarray, i);
        const char* arg = env->GetStringUTFChars(s, NULL);      

        strcat(cmdline, (i == 0) ? "\"" : " ");
        strcat(cmdline, arg);
        if(i == 0)
          strcat(cmdline, "\"");

        env->ReleaseStringUTFChars(s, arg);
      }

      printf("Cmdline = %s\n", cmdline);     // DEBUG
    }

    {
      jsize len = env->GetArrayLength(jenvp);

      printf("Environment:\n");      // DEBUG

      LPTSTR envp = envbuf;
      int total = 0;
      jsize i;
      for(i=0; i<len; i++) {
        jstring s = (jstring) env->GetObjectArrayElement(jenvp, i);
        const char* keyval = env->GetStringUTFChars(s, NULL);

        printf("  %s\n", keyval);

        if(lstrcpy(envp, TEXT(keyval)) == NULL) {
          env->ReleaseStringUTFChars(jdir, dir);
          delete[] cmdline;

          env->ThrowNew(IOException, "failed to copy environment");
          return -1;
        }

        env->ReleaseStringUTFChars(s, keyval);

        int size = (lstrlen(envp) + 1) * sizeof(TCHAR);
        total += size;

        if(total > 32768) {
          env->ReleaseStringUTFChars(jdir, dir);
          delete[] cmdline;

          env->ThrowNew(IOException, "environment exceeds 32K limit on Windows!");
          return -1; 
        }

        envp += size;
      }
      *envp = '\0';
    }
  }
  
  /* create pipes to communicate with the child process */ 
  HANDLE child_stdin, child_stdout, child_stderr;
  HANDLE parent_stdin, parent_stdout, parent_stderr; 
  {
    /* security attributes which allow handles to be inherited by the child process */ 
    SECURITY_ATTRIBUTES saAttr; 
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES); 
    saAttr.bInheritHandle = TRUE; 
    saAttr.lpSecurityDescriptor = NULL; 
 
    /* create a pipe to the child's STDIN */
    if(!CreatePipe(&child_stdin, &parent_stdin, &saAttr, 0)) {
      env->ReleaseStringUTFChars(jdir, dir);
      delete[] cmdline;        

      env->ThrowNew(IOException, "unable to create the STDIN pipe!"); 
      return -1;
    }

    /* create a pipe to the child's STDOUT */
    if(!CreatePipe(&parent_stdout, &child_stdout, &saAttr, 0)) {
      env->ReleaseStringUTFChars(jdir, dir);
      delete[] cmdline;
      CloseHandle(child_stdin);

      env->ThrowNew(IOException, "unable to create the STDOUT pipe!"); 
      return -1;
    }

    /* create a pipe to the child's STDERR */
    if(!CreatePipe(&parent_stderr, &child_stderr, &saAttr, 0)) {
      env->ReleaseStringUTFChars(jdir, dir);
      delete[] cmdline;
      CloseHandle(child_stdin);
      CloseHandle(child_stdout);

      env->ThrowNew(IOException, "unable to create the STDERR pipe!"); 
      return -1;
    }  
    
    /* make sure parent side of STDIN STDOUT and STDERR pipes are not inherited */ 
    SetHandleInformation(parent_stdin, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation(parent_stdout, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation(parent_stderr, HANDLE_FLAG_INHERIT, 0);
  }
  
  /* create the child process */ 
  DWORD exitCode; 
  {
    PROCESS_INFORMATION procInfo; 
    STARTUPINFO startInfo;

    ZeroMemory(&procInfo, sizeof(PROCESS_INFORMATION));
 
    ZeroMemory(&startInfo, sizeof(STARTUPINFO));
    startInfo.cb = sizeof(STARTUPINFO); 
    startInfo.hStdError  = child_stderr;
    startInfo.hStdOutput = child_stdout; 
    startInfo.hStdInput  = child_stdin;
    startInfo.dwFlags |= STARTF_USESTDHANDLES;
 
    if(CreateProcess(NULL, TEXT(cmdline), NULL, NULL, TRUE, 0, 
                     (LPVOID) envbuf, TEXT(dir), &startInfo, &procInfo) == 0)  {
      env->ReleaseStringUTFChars(jdir, dir);
      delete[] cmdline;       
      CloseHandle(child_stdin);
      CloseHandle(child_stdout);
      CloseHandle(child_stderr);
      
      throwWindowsIOException(env, IOException, "CreateProcessAsUser"); 
      return -1;
    }
    env->ReleaseStringUTFChars(jdir, dir);
    delete[] cmdline;
                    
    printf("Created Process\n");     // DEBUG

    /* set the process ID */ 
    jint pid = (jint) procInfo.dwProcessId;
    env->CallVoidMethod(obj, setPid, pid);

    /* set IO fields for the parent ends of the pipes */ 
    env->SetIntField(obj, pStdInFileDesc, (jint) parent_stdin);
    env->SetIntField(obj, pStdOutFileDesc, (jint) parent_stdout);
    env->SetIntField(obj, pStdErrFileDesc, (jint) parent_stderr);

    /* let Java know that the process is running */ 
    env->CallVoidMethod(obj, setIsRunning, true);
      
    printf("Wating on PID (%d)...\n", pid);     // DEBUG

    /* wait on the process to exit */ 
    if(WaitForSingleObject(procInfo.hProcess, INFINITE) == WAIT_FAILED) {
      CloseHandle(child_stdin);
      CloseHandle(child_stdout);
      CloseHandle(child_stderr);

      env->ThrowNew(IOException, "failed to wait on subprocess!"); 
      return -1;
    }

    printf("Done Wating!\n");     // DEBUG

    /* let Java know that the process has exited */ 
    env->CallVoidMethod(obj, setIsRunning, false);
        
    /* get the exit code */ 
    if(!GetExitCodeProcess(procInfo.hProcess, &exitCode)) {
      CloseHandle(child_stdin);
      CloseHandle(child_stdout);
      CloseHandle(child_stderr);

      env->ThrowNew(IOException, "failed to get subprocess exit code!"); 
      return -1;
    }

    printf("Exit Code = %d\n", exitCode);       // DEBUG

    /* close the child side of the STDIN pipe */ 
    if(!CloseHandle(child_stdin)) {
      CloseHandle(child_stdout);
      CloseHandle(child_stderr);

      env->ThrowNew(IOException, "unable to close the child side of the STDIN pipe!"); 
      return -1;
    }

    /* close the child side of the STDOUT pipe */ 
    if(!CloseHandle(child_stdout)) {
      env->ThrowNew(IOException, "unable to close the child side of the STDOUT pipe!"); 
      return -1;
    }
    
    /* close the child side of the STDERR pipe */ 
    if(!CloseHandle(child_stderr)) {
      env->ThrowNew(IOException, "unable to close the child side of the STDERR pipe!"); 
      return -1;
    }

    /* get usage statistics */
    {
      FILETIME createTime, exitTime, sysTime, userTime; 
      if(!GetProcessTimes(procInfo.hProcess, &createTime, &exitTime, &sysTime, &userTime)) {
        env->ThrowNew(IOException, "failed to get subprocess timing statistics!"); 
        return -1;
      }

      /* 64-bit times returned by GetProcessTime are in 100ns (100/10^9), 
         while the times reported to Java are in jiffies (1/100) of a second */   
      jlong denom = 100000L;

      { 
        ULARGE_INTEGER li;
        memcpy(&li, &userTime, sizeof(FILETIME));
        env->SetLongField(obj, pUTime, li.QuadPart / denom);  

        printf("User Time = %I64u (100-ns)  %I64u (jiffies)\n", 
               li.QuadPart, li.QuadPart / denom); 
      }                                   

      { 
        ULARGE_INTEGER li;
        memcpy(&li, &sysTime, sizeof(FILETIME));
        env->SetLongField(obj, pSTime, li.QuadPart / denom);

        printf("System Time = %I64u (100-ns)  %I64u (jiffies)\n", 
               li.QuadPart, li.QuadPart / denom); 
      }

      /* get process memory usage statistics */ 
      {
        PROCESS_MEMORY_COUNTERS pmc;
        if(!GetProcessMemoryInfo(procInfo.hProcess, &pmc, sizeof(pmc))) {
          env->ThrowNew(IOException, "failed to get subprocess memory usage statistics!"); 
          return -1;
        }

        // DEBUG
        printf("PageFaults = %I64d\n", (jlong) pmc.PageFaultCount);
        printf("PeakWorking Set Size = %I64d\n", (jlong) pmc.PeakWorkingSetSize);
        printf("PeakPagefileUsage = %I64d\n", (jlong) pmc.PeakPagefileUsage);
        printf("QuotaPeakPagedPoolUsage = %I64d\n", (jlong) pmc.QuotaPeakPagedPoolUsage);
        // DEBUG

        env->SetLongField(obj, pPageFaults,   (jlong) pmc.PageFaultCount);
        env->SetLongField(obj, pResidentSize, (jlong) pmc.PeakWorkingSetSize); 
        env->SetLongField(obj, pVirtualSize,  (jlong) pmc.PeakPagefileUsage); 
        env->SetLongField(obj, pSwappedSize,  (jlong) pmc.QuotaPeakPagedPoolUsage);
      } 
    }

    /* cleanup process/thread handles */ 
    CloseHandle(procInfo.hProcess);
    CloseHandle(procInfo.hThread);
  }

  return ((jint) exitCode);
}
