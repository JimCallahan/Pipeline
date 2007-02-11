// stdafx.cpp : source file that includes just the standard includes
// libNative.pch will be the pre-compiled header
// stdafx.obj will contain the pre-compiled type information

#include "stdafx.h"

// TODO: reference any additional headers you need in STDAFX.H
// and not in this file

void 
throwWindowsIOException
(
 JNIEnv *env, 
 jclass IOException, 
 LPTSTR lpszFunction
) 
{ 
  LPVOID lpMsgBuf;
  LPVOID lpDisplayBuf;

  DWORD dw = GetLastError(); 

  FormatMessage
    (FORMAT_MESSAGE_ALLOCATE_BUFFER | 
     FORMAT_MESSAGE_FROM_SYSTEM |
     FORMAT_MESSAGE_IGNORE_INSERTS,
     NULL,
     dw,
     MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
     (LPTSTR) &lpMsgBuf,
     0, NULL);

  lpDisplayBuf = (LPVOID) LocalAlloc(LMEM_ZEROINIT, 
                                     (lstrlen((LPCTSTR)lpMsgBuf) + 
                                      lstrlen((LPCTSTR)lpszFunction) + 40) * sizeof(TCHAR)); 
  wsprintf((LPTSTR)lpDisplayBuf, 
           TEXT("%s failed with error %d:\n  %s"), 
           lpszFunction, dw, lpMsgBuf); 

  env->ThrowNew(IOException, (LPTSTR)lpDisplayBuf);

  LocalFree(lpMsgBuf);
  LocalFree(lpDisplayBuf);
}
