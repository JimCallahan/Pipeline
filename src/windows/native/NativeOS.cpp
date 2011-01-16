// $Id: NativeOS.cpp,v 1.3 2007/02/11 17:44:11 jim Exp $

#include "stdafx.h"
 
/* Get the amount of free system memory (in bytes). */ 
extern "C" 
JNIEXPORT jlong
JNICALL Java_us_temerity_pipeline_NativeOS_getFreeMemoryNative
(
 JNIEnv *env, 
 jclass cls
)
{
  /* exception initialization */ 
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeOS.getFreeMemoryNative(), unable to lookup \"java/lang/IOException\"");
    return -2L;
  }

  jlong freeMem = 0L;
  {
    MEMORYSTATUSEX statex; 
    statex.dwLength = sizeof(statex);
    
    if(GlobalMemoryStatusEx(&statex) == 0) {
      env->ThrowNew(IOException, 
                    "NativeOS.getFreeMemoryNative(), call to GlobalMemoryStatusEx failed!");
      return -3L; 
    }
    
    /* get the available memory */ 
    freeMem = (jlong) statex.ullAvailPhys;
  }

  return freeMem;
}


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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeOS.getTotalMemoryNative(), unable to lookup \"java/lang/IOException\"");
    return -2L;
  }

  /* query WMI to compute total system memory */ 
  jlong totalMem = 0;
  {
    MEMORYSTATUSEX statex; 
    statex.dwLength = sizeof(statex);
    
    if(GlobalMemoryStatusEx(&statex) == 0) {
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), call to GlobalMemoryStatusEx failed!");
      return -3L; 
    }
    
    /* get the total physical memory */ 
    totalMem = (jlong) statex.ullTotalPhys;
  }
  
  return totalMem;
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
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeOS.getNumProcessorsNative(), unable to lookup \"java/lang/IOException\"");
    return -2;
  }

  /* lookup number of processors from the system info table */ 
  SYSTEM_INFO siSysInfo;
  GetSystemInfo(&siSysInfo); 

  return ((jint) siSysInfo.dwNumberOfProcessors);
}
 

/* a running average of system load */ 
static jfloat NativeOS_avgLoad = -1.0f;

/* Get the instantaneous processor active percentage. */
extern "C" 
JNIEXPORT jfloat
JNICALL Java_us_temerity_pipeline_NativeOS_getLoadAverageNative
(
 JNIEnv *env, 
 jclass cls
)
{
  /* exception initialization */ 
  jclass IOException = env->FindClass("java/io/IOException");
  if(IOException == 0) {
    errno = EINVAL;
    perror("NativeOS.getLoadAverageNative(), unable to lookup \"java/lang/IOException\"");
    return -2.0f;
  }

  float load = -1.0f;
  {
    PDH_STATUS pdhStatus;

    /* open a query object */ 
    HQUERY hQuery;
    pdhStatus = PdhOpenQuery(0, 0, &hQuery);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                    "NativeOS.getLoadAverageNative(), PdhOpenQuery() failed!"); 
      return -3.0L;
    }
   
    /* add the counter */ 
    HCOUNTER hCounter;
    pdhStatus = PdhAddCounter(hQuery, TEXT("\\System\\Processor Queue Length"),
                              0, &hCounter);
    
    /* allocate the counter value structures */
    PDH_FMT_COUNTERVALUE* counterBuf = 
      (PDH_FMT_COUNTERVALUE *) GlobalAlloc (GPTR, sizeof(PDH_FMT_COUNTERVALUE));
    if(counterBuf == NULL) {
      env->ThrowNew(IOException, 
                    "NativeOS.getLoadAverageNative(), GlobalAlloc() failed!"); 
      return -4.0L;
    } 
    
    /* read the performance data records */
    pdhStatus = PdhCollectQueryData(hQuery);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                    "NativeOS.getLoadAverageNative(), PdhCollectQueryData() failed!"); 
      return -5.0L;
    }
    
    /* format the performance data record */ 
    pdhStatus = PdhGetFormattedCounterValue(hCounter, PDH_FMT_LONG,  
                                            (LPDWORD)NULL, counterBuf);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                  "NativeOS.getLoadAverageNative(), PdhGetFormattedCounterValue() failed!"); 
      return -6.0L;
    }
    
    /* compute a pseudo-load from the length of the processor queue, 
        a queue length of 10 is roughly equivalent to a UNIX load of 1.0 */ 
    load = ((jfloat) (counterBuf->longValue)) * 0.1f;

    /* close the query */ 
    pdhStatus = PdhCloseQuery(hQuery);
  }

  /* update the running 1-minute average (samples are at 15-sec intervals) */ 
  if(NativeOS_avgLoad < 0.0f) 
    NativeOS_avgLoad = load;
  else 
    NativeOS_avgLoad = 0.75f*NativeOS_avgLoad + 0.25f*load;

  return NativeOS_avgLoad; 
}
 
