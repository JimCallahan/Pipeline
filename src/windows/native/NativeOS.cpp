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
    PDH_STATUS pdhStatus;
    
    /* open a query object */ 
    HQUERY hQuery;
    pdhStatus = PdhOpenQuery(0, 0, &hQuery);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                    "NativeOS.getFreeMemoryNative(), PdhOpenQuery() failed!"); 
      return -3L;
    }
    
    /* add the counter */ 
    HCOUNTER hCounter;
    pdhStatus = PdhAddCounter(hQuery, TEXT("\\Memory\\Available Bytes"), 0, &hCounter);
    
    /* allocate the counter value structures */
    PDH_FMT_COUNTERVALUE* counterBuf = 
      (PDH_FMT_COUNTERVALUE *) GlobalAlloc (GPTR, sizeof(PDH_FMT_COUNTERVALUE));
    if(counterBuf == NULL) {
      env->ThrowNew(IOException, 
                    "NativeOS.getFreeMemoryNative(), GlobalAlloc() failed!"); 
      return -4L;
    } 
    
    /* read the performance data records */
    pdhStatus = PdhCollectQueryData(hQuery);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                    "NativeOS.getFreeMemoryNative(), PdhCollectQueryData() failed!"); 
      return -5L;
    }
    
    /* format the performance data record */ 
    pdhStatus = PdhGetFormattedCounterValue(hCounter, PDH_FMT_LARGE, 
                                            (LPDWORD)NULL, counterBuf);
    if(pdhStatus != ERROR_SUCCESS) {
      env->ThrowNew(IOException, 
                    "NativeOS.getFreeMemoryNative(), PdhGetFormattedCounterValue() failed!"); 
      return -6L;
    }
    
    /* get the available memory */ 
    freeMem = (jlong) counterBuf->largeValue;
    
    /* close the query */ 
    pdhStatus = PdhCloseQuery(hQuery);
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
    HRESULT hres;

    /* initialize COM */ 
    hres =  CoInitializeEx(0, COINIT_MULTITHREADED); 
    if(FAILED(hres)) {
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), CoInitializeEx() failed!"); 
      return -3L;     
    }           
 
    /* set general COM security levels */ 
    hres = CoInitializeSecurity(NULL, -1, NULL, NULL, RPC_C_AUTHN_LEVEL_DEFAULT, 
                                RPC_C_IMP_LEVEL_IMPERSONATE, NULL, EOAC_NONE, NULL);          
    if(FAILED(hres)) {
      CoUninitialize();   
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), CoInitializeSecurity() failed!"); 
      return -4L;                  
    }
 
    /* obtain the initial locator to the WMI */ 
    IWbemLocator *pLoc = NULL;
    hres = CoCreateInstance(CLSID_WbemLocator, 0, CLSCTX_INPROC_SERVER, IID_IWbemLocator, 
                            (LPVOID *) &pLoc);
    if(FAILED(hres)) {
      CoUninitialize();
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), CoCreateInstance() failed!"); 
      return -5L;              
    }
 
    /* connect the the WMI */ 
    IWbemServices *pSvc = NULL;
    hres = pLoc->ConnectServer(_bstr_t(L"ROOT\\CIMV2"), NULL, NULL, 0, NULL, 0, 0, &pSvc);
    if(FAILED(hres)) {
      pLoc->Release();     
      CoUninitialize();
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), ConnectServer() failed!"); 
      return -6L;         
    }
 
    /* set security levels on the proxy */ 
    hres = CoSetProxyBlanket(pSvc, RPC_C_AUTHN_WINNT, RPC_C_AUTHZ_NONE, NULL, 
                             RPC_C_AUTHN_LEVEL_CALL, RPC_C_IMP_LEVEL_IMPERSONATE, 
                             NULL, EOAC_NONE);  
    if(FAILED(hres)) {
      pSvc->Release();
      pLoc->Release();     
      CoUninitialize();
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), CoSetProxyBlanket() failed!"); 
      return -7L;             
    }

    /* query the WMI */ 
    IEnumWbemClassObject* pEnumerator = NULL;
    hres = pSvc->CreateInstanceEnum(L"Win32_PhysicalMemory", 
                                    WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY, 
                                    NULL, &pEnumerator);
    if(FAILED(hres)) {
      pSvc->Release();
      pLoc->Release();
      CoUninitialize();
      env->ThrowNew(IOException, 
                    "NativeOS.getTotalMemoryNative(), ExecQuery() failed!"); 
      return -8L;              
    }
    
    /* extract the results of the query, 
         the query may return multiple physical memory DIMs which need to be summed to
         get the total physical memory size */ 
    IWbemClassObject *pclsObj;
    {
      ULONG uReturn = 0;  
      ULONG value = 0;
      while (pEnumerator) {
        HRESULT hr = pEnumerator->Next(WBEM_INFINITE, 1, &pclsObj, &uReturn);
        if(uReturn == 0)
          break;

        VARIANT vtProp, i64Prop;
        VariantInit(&vtProp);
        VariantInit(&i64Prop);
        
        hr = pclsObj->Get(L"Capacity", 0, &vtProp, 0, 0);
        VarUI4FromStr(vtProp.bstrVal, 0, 0, &value);
        VariantChangeType(&i64Prop, &vtProp, 0, VT_I8);
        totalMem += i64Prop.llVal;

        VariantClear(&vtProp);
        VariantClear(&i64Prop);
      }
    }
    
    /* clean up */ 
    pSvc->Release();
    pLoc->Release();
    pEnumerator->Release();
    pclsObj->Release();
    CoUninitialize();
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
 
