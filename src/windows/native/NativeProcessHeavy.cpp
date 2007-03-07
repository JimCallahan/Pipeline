// $Id: NativeProcessHeavy.cpp,v 1.6 2007/03/07 08:25:59 jim Exp $

#include "stdafx.h"

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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_closeStdIn
(
 JNIEnv *env, 
 jobject obj
 )
{  
  /* exception initialization */ 
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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

  /* get the STDIN file handle */ 
  HANDLE stdin_handle = (HANDLE) env->GetIntField(obj, pStdInFileDesc);

  //printf("Close STDIN = %I32u\n", stdin_handle);

  /* close the STDIN pipe */ 
  if(!CloseHandle(stdin_handle)) 
    env->ThrowNew(IOException, "unable to close the parent STDIN pipe!");
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
  jclass IOException = env->FindClass("java/io/IOException");
  char msg[1024];
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeProcessHeavy.signalNative(), unable to lookup \"java/lang/IOException\"");
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
 jobjectArray jenvp,       /* IN: environmental variable name=value pairs */  
 jstring jdir,             /* IN: the working directory */     
 jstring joutfile,         /* IN: the file to which all STDOUT output is redirected */
 jstring jerrfile          /* IN: the file to which all STDERR output is redirected */
)
{
  /* exception initialization */ 
  char msg[2048];
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
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
  HANDLE tok;
  bool substUser = false;
  const char* dir = NULL; 
  const char* outFile = NULL;
  const char* errFile = NULL;
  char* cmdline = NULL; 
  TCHAR envbuf[32768]; 
  {
    /* substitute user? */ 
    if((juser != NULL) && (jdomain != NULL) && (jpassword != NULL)) {
      const char* user   = env->GetStringUTFChars(juser, NULL);      
      const char* domain = env->GetStringUTFChars(jdomain, NULL);     
 
      char* password = NULL; 
      jsize pwLen = env->GetArrayLength(jpassword);
      if(pwLen == 0) {
        if(user != NULL) 
          env->ReleaseStringUTFChars(juser, user);
        if(domain != NULL) 
          env->ReleaseStringUTFChars(jdomain, domain);

        env->ThrowNew(IOException, "empty password array"); 
        return -1;
      }

      jchar* pwd = env->GetCharArrayElements(jpassword, NULL);
      {
        password = new char[pwLen+1];
        jsize i;
        for(i=0; i<pwLen; i++)
          password[i] = pwd[i];
        password[i] = '\0';
      }
      env->ReleaseCharArrayElements(jpassword, pwd, JNI_ABORT);

      // DEBUG
//       printf("User = %s\n", user);     
//       printf("Domain = %s\n", domain); 
//       printf("Password = %s\n", password); 
      // DEBUG

      substUser = LogonUser(TEXT(user), TEXT(domain), TEXT(password), 
                            LOGON32_LOGON_BATCH, LOGON32_PROVIDER_DEFAULT, &tok) != 0; 
      SecureZeroMemory(password, pwLen+1); 
      
      if(user != NULL) 
        env->ReleaseStringUTFChars(juser, user);
      if(domain != NULL) 
        env->ReleaseStringUTFChars(jdomain, domain);
      if(password != NULL) 
        delete[] password; 

      if(!substUser) {
        throwWindowsIOException(env, IOException, "LogonUser"); 
        return -1;
      } 
    }

    {
      dir = env->GetStringUTFChars(jdir, 0);      
      if((dir == NULL) || (strlen(dir) == 0)) {
        if(substUser) 
          CloseHandle(tok);
        env->ThrowNew(IOException,"empty working directory");
        return -1;
      } 

      printf("Dir = %s\n", dir);    // DEBUG

      struct stat buf;
      if(stat(dir, &buf) == -1) {
        if(substUser) 
          CloseHandle(tok);
        sprintf(msg, "stat failed for \"%s\": %s", dir, strerror(errno));
        env->ReleaseStringUTFChars(jdir, dir);
        env->ThrowNew(IOException, msg);
        return -1;
      }
      else if(!(buf.st_mode & _S_IFDIR)) {
        if(substUser) 
          CloseHandle(tok);
        sprintf(msg, "illegal working directory \"%s\"", dir);
        env->ReleaseStringUTFChars(jdir, dir);
        env->ThrowNew(IOException, msg);
        return -1;
      }
    }

    printf("Dir is OK\n");      // DEBUG

    {
      outFile = env->GetStringUTFChars(joutfile, 0);      
      if((outFile == NULL) || (strlen(outFile) == 0)) {
        if(substUser) 
          CloseHandle(tok);
        env->ReleaseStringUTFChars(jdir, dir);

        env->ThrowNew(IOException,"empty STDOUT output file");
        return -1;
      }    
    }

    printf("Outfile = %s\n", outFile);     // DEBUG
 
    {
      errFile = env->GetStringUTFChars(jerrfile, 0);      
      if((errFile == NULL) || (strlen(errFile) == 0)) {
        if(substUser) 
          CloseHandle(tok);
        env->ReleaseStringUTFChars(jdir, dir);
        env->ReleaseStringUTFChars(joutfile, outFile);

        env->ThrowNew(IOException,"empty STDERR output file");
        return -1;
      }    
    }

    printf("Errfile = %s\n", errFile);     // DEBUG

    {
      jsize len = env->GetArrayLength(jcmdarray);
      if(len == 0) {
        if(substUser) 
          CloseHandle(tok);
        env->ReleaseStringUTFChars(jdir, dir);
        env->ReleaseStringUTFChars(joutfile, outFile);
        env->ReleaseStringUTFChars(jerrfile, errFile);

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
        if(substUser) 
          CloseHandle(tok);
        env->ReleaseStringUTFChars(jdir, dir);
        env->ReleaseStringUTFChars(joutfile, outFile);
        env->ReleaseStringUTFChars(jerrfile, errFile);

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
          if(substUser) 
            CloseHandle(tok);
          env->ReleaseStringUTFChars(jdir, dir);
          env->ReleaseStringUTFChars(joutfile, outFile);
          env->ReleaseStringUTFChars(jerrfile, errFile);
          delete[] cmdline;

          env->ThrowNew(IOException, "failed to copy environment");
          return -1;
        }

        env->ReleaseStringUTFChars(s, keyval);

        int size = (lstrlen(envp) + 1) * sizeof(TCHAR);
        total += size;

        if(total > 32768) {
          if(substUser) 
            CloseHandle(tok);
          env->ReleaseStringUTFChars(jdir, dir);
          env->ReleaseStringUTFChars(joutfile, outFile);
          env->ReleaseStringUTFChars(jerrfile, errFile);
          delete[] cmdline;

          env->ThrowNew(IOException, "environment exceeds 32K limit on Windows!");
          return -1; 
        }

        envp += size;
      }
      *envp = '\0';
    }
  }
  
  /* create STDIN pipe to communicate with the child process 
     and get handles to the STDOUT/STDERR files */ 
  HANDLE child_stdin, child_stdout, child_stderr;
  HANDLE parent_stdin; 
  {
    /* security attributes which allow handles to be inherited by the child process */ 
    SECURITY_ATTRIBUTES saAttr; 
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES); 
    saAttr.bInheritHandle = TRUE; 
    saAttr.lpSecurityDescriptor = NULL; 
 
    /* create a pipe to the child's STDIN */
    if(!CreatePipe(&child_stdin, &parent_stdin, &saAttr, 0)) {
      if(substUser) 
        CloseHandle(tok);
      env->ReleaseStringUTFChars(jdir, dir);
      env->ReleaseStringUTFChars(joutfile, outFile);
      env->ReleaseStringUTFChars(jerrfile, errFile);
      delete[] cmdline;
        
      env->ThrowNew(IOException, "unable to create the STDIN pipe!"); 
      return -1;
    }

    /* get handle to the STDOUT file */ 
    child_stdout = CreateFile(TEXT(outFile), GENERIC_WRITE, FILE_SHARE_READ, &saAttr, 
                              CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if(child_stdout == INVALID_HANDLE_VALUE) {
      if(substUser) 
        CloseHandle(tok);
      env->ReleaseStringUTFChars(jdir, dir);
      env->ReleaseStringUTFChars(joutfile, outFile);
      env->ReleaseStringUTFChars(jerrfile, errFile);
      delete[] cmdline;

      CloseHandle(child_stdin);

      env->ThrowNew(IOException, "unable to open the STDOUT file!"); 
      return -1;
    }
    env->ReleaseStringUTFChars(joutfile, outFile);

    /* get handle to the STDERR file */ 
    child_stderr = CreateFile(TEXT(errFile), GENERIC_WRITE, FILE_SHARE_READ, &saAttr, 
                              CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if(child_stderr == INVALID_HANDLE_VALUE) {
      if(substUser) 
        CloseHandle(tok);
      env->ReleaseStringUTFChars(jdir, dir);
      env->ReleaseStringUTFChars(jerrfile, errFile);
      delete[] cmdline;

      CloseHandle(child_stdin);
      CloseHandle(child_stdout);

      env->ThrowNew(IOException, "unable to open the STDERR file!"); 
      return -1;
    }  
    env->ReleaseStringUTFChars(jerrfile, errFile);
    
    /* make sure parent side of STDIN pipes is not inherited */ 
    SetHandleInformation(parent_stdin, HANDLE_FLAG_INHERIT, 0);
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
 
    if(substUser) {
      if(CreateProcessAsUser(tok, NULL, TEXT(cmdline), NULL, NULL, TRUE, 0, 
                             (LPVOID) envbuf, TEXT(dir), &startInfo, &procInfo) == 0)  {
        env->ReleaseStringUTFChars(jdir, dir);
        delete[] cmdline;

        CloseHandle(child_stdin);
        CloseHandle(child_stdout);
        CloseHandle(child_stderr);

        throwWindowsIOException(env, IOException, "CreateProcessAsUser"); 
        return -1;
      }

      CloseHandle(tok);
    }
    else {      
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
    }
    env->ReleaseStringUTFChars(jdir, dir);
    delete[] cmdline;
                    
    printf("Created Process\n");     // DEBUG

    /* set the process ID */ 
    jint pid = (jint) procInfo.dwProcessId;
    env->CallVoidMethod(obj, setPid, pid);

    /* set IO fields for the parent ends of the pipes */ 
    env->SetIntField(obj, pStdInFileDesc, (jint) parent_stdin);

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
      CloseHandle(child_stderr);

      env->ThrowNew(IOException, "unable to close the STDOUT file!"); 
      return -1;
    }
    
    /* close the child side of the STDERR pipe */ 
    if(!CloseHandle(child_stderr)) {
      env->ThrowNew(IOException, "unable to close the STDERR file!"); 
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

        printf("PageFaults = %I64d\n", (jlong) pmc.PageFaultCount);
        printf("PeakWorking Set Size = %I64d\n", (jlong) pmc.PeakWorkingSetSize);
        printf("PeakPagefileUsage = %I64d\n", (jlong) pmc.PeakPagefileUsage);
        printf("QuotaPeakPagedPoolUsage = %I64d\n", (jlong) pmc.QuotaPeakPagedPoolUsage);

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

extern "C" 
JNIEXPORT jlong
JNICALL Java_us_temerity_pipeline_NativeProcessHeavy_convertTime
(
 FILETIME* timep
)
{
  ULARGE_INTEGER li;
  memcpy(&li, timep, sizeof(FILETIME));
  return li.QuadPart;
}
