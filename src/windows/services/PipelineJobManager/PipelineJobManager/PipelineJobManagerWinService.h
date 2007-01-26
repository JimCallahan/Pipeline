#pragma once

using namespace System;
using namespace System::Collections;
using namespace System::IO;
using namespace System::ServiceProcess;
using namespace System::ComponentModel;
using namespace System::Threading;
using namespace System::Diagnostics;


namespace PipelineJobManager {
  /// <summary>
  /// Summary for PipelineJobManagerWinService
  /// </summary>
  ///
  /// WARNING: If you change the name of this class, you will need to change the
  ///          'Resource File Name' property for the managed resource compiler tool
  ///          associated with all .resx files this class depends on.  Otherwise,
  ///          the designers will not be able to interact properly with localized
  ///          resources associated with this form.
  public ref class PipelineJobManagerWinService : public System::ServiceProcess::ServiceBase
    {
    public:
      PipelineJobManagerWinService()
	{
	  InitializeComponent();
	}

    protected:
      ~PipelineJobManagerWinService()
	{
	  if(components) 
	    delete components;
	}
    
      /// <summary>
      /// Set things in motion so your service can do its work.
      /// </summary>
      virtual void OnStart(array<String^>^ args) override
	{
	  if((pThread == nullptr) ||
	     ((pThread->ThreadState & 
	       (System::Threading::ThreadState::Unstarted | 
		System::Threading::ThreadState::Stopped)) != ((System::Threading::ThreadState) 0))) {
       
	    /* log file */ 
	    FILE* log = fopen("C:/TEMP/PipelineJobManager-OnStart.log", "a+");
	    if(log == NULL) 
	      return;
	    fprintf(log, "PipelineJobManager.OnStart()\n"); 
	    fflush(log);

	    pThread = gcnew Thread(gcnew ThreadStart(this, &PipelineJobManagerWinService::StartJVM));
	    fprintf(log, "StartJVM Thread Created\n"); 
	    fflush(log);

	    pThread->Start();
	    fprintf(log, "StartJVM Thread Started\n"); 
	    fflush(log);
	    fclose(log);
	  }
	}
    
      /// <summary>
      /// Stop this service.
      /// </summary>
      virtual void OnStop() override
	{
	  /* log file */ 
	  FILE* log = fopen("C:/TEMP/PipelineJobManager-OnStop.log", "a+");
	  if(log == NULL) 
	    return;
	  fprintf(log, "PipelineJobManager.OnStop()\n"); 
	  fflush(log);
	  
	  Thread^ thread = gcnew Thread(gcnew ThreadStart(this, &PipelineJobManagerWinService::StopJVM));
	  fprintf(log, "StopJVM Thread Created\n"); 
	  fflush(log);

	  thread->Start();
	  fprintf(log, "StopJVM Thread Started\n"); 
	  fflush(log);

	  thread->Join();
	  fprintf(log, "StopJVM Joined\n"); 
	  fflush(log);
	  fclose(log);
	}
    
    private:
      /// <summary>
      /// Error check JNI functions.
      /// </summary>
      bool testJNI(jint code, FILE* log, const char* msg)
	{
	  switch(code) {
	  case 0:
	    return false;
	
	  case JNI_ERR:
	    fprintf(log, "ERROR - Unknown error: %s\n", msg); 
	    break; 
	
	  case JNI_EDETACHED:
	    fprintf(log, "ERROR - Thread detached from the VM: %s\n", msg); 
	    break; 
	
	  case JNI_EVERSION:
	    fprintf(log, "ERROR - JNI version error: %s\n", msg); 
	    break; 
	
	  case JNI_ENOMEM:
	    fprintf(log, "ERROR - Not enough memory: %s\n", msg); 
	    break; 
	
	  case JNI_EEXIST:
	    fprintf(log, "ERROR - VM already created: %s\n", msg); 
	    break; 
	
	  case JNI_EINVAL:
	    fprintf(log, "ERROR - Invalid arguments: %s\n", msg); 
	    break; 
	
	  default:
	    fprintf(log, "ERROR - Bad error code: %s\n", msg); 
	    break; 
	  }

	  fflush(log);
	  return true;
	}

      /// <summary>
      /// Start the Java Virtual Machine, call the Java entry method and wait around
      /// for the JVM to exit normally.
      /// </summary>
      void StartJVM() 
	{
 	  /* log file */ 
 	  FILE* log = fopen("C:/TEMP/PipelineJobManager-StartJVM.log", "a+");
 	  if(log == NULL) 
 	    return;
 	  fprintf(log, "Service Starting...\n"); 
 	  fflush(log);
      
 	  /* set the JVM initialization arguments */
 	  JavaVMInitArgs vm_args;
 	  {
 	    JavaVMOption options[5];
 	    options[0].optionString = "-Xms8M"; 
 	    options[1].optionString = "-Xmx128M"; 
 	    options[2].optionString = "-Xdebug"; 
 	    options[3].optionString = "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=45006"; 
 	    options[4].optionString = "-Djava.class.path=C:\\TEMP"; 
      
 	    vm_args.version = JNI_VERSION_1_2;
 	    vm_args.options = options;
 	    vm_args.nOptions = 5;
 	    vm_args.ignoreUnrecognized = 1;
 	  }
      
 	  /* load and initialize a JVM, return a JNI interface pointer in env */
 	  JavaVM *jvm;     
 	  JNIEnv *env; 
 	  if(testJNI(JNI_CreateJavaVM(&jvm, (void**) &env, &vm_args), 
 		     log, "Unable to create a JVM to run the service!"))
 	    return; 
 	  fprintf(log, "Created a JVM for the service.\n");
 	  fflush(log);
      
 	  /* get the method handle for TestService.onStart */
 	  jclass cls = env->FindClass("TestService");
 	  jmethodID onStart = env->GetStaticMethodID(cls, "onStart", "()V");
 	  if(onStart == NULL) {
 	    fprintf(log, "ERROR - Unable to get the TestService.onStart method handle.\n");
 	    fflush(log);
 	    return;
 	  }
 	  fprintf(log, "Got the TestService.onStart method handle.\n");
 	  fflush(log);
      
 	  /* run TestService.onStart */
 	  env->CallStaticVoidMethod(cls, onStart);
      
 	  /* clean up */
 	  jvm->DestroyJavaVM();

 	  fprintf(log, "Service Stopped.\n"); 
 	  fflush(log);
 	  fclose(log);
	}

      /// <summary>
      /// 
      /// </summary>
      void StopJVM() 
	{
	  if((pThread != nullptr) && (pThread->IsAlive)) {
	    /* log file */ 
	    FILE* log = fopen("C:/TEMP/PipelineJobManager-StopJVM.log", "a+");
	    if(log == NULL) 
	      return;
	    fprintf(log, "Signaling Service to Stop...\n"); 
	    fflush(log);

 	    /* lookup the previously created JVM */ 
 	    JavaVM *jvm;   
 	    {
 	      JavaVM *vmBuf[1];
 	      jsize bufLen;
 	      if(testJNI(JNI_GetCreatedJavaVMs(vmBuf, 1, &bufLen),
 			 log, "Unable to find the JVM running the service!"))
 		return; 
	      
 	      if((bufLen != 1) || (vmBuf[0] == NULL)) {
 		fprintf(log, "ERROR - Missing JVM\n");
 		fflush(log);
 		return;
 	      }
 	      jvm = vmBuf[0];
 	    } 
	    
 	    /* attach to the existing JVM, return a JNI interface pointer in env */ 
 	    JNIEnv *env; 
 	    if(testJNI(jvm->AttachCurrentThread((void**) &env, NULL), 
 		       log, "Unable to attach to the JVM running the service!"))
 	      return; 
 	    fprintf(log, "Attached to the JVM running the service.\n");
 	    fflush(log);
	    
 	    /* get the method handle for TestService.onStop */
 	    jclass cls = env->FindClass("TestService");
 	    jmethodID onStop = env->GetStaticMethodID(cls, "onStop", "()V");
 	    if(onStop == NULL) {
 	      fprintf(log, "ERROR - Unable to get the TestService.onStop method handle.\n");
 	      fflush(log);
 	      return;
 	    }
 	    fprintf(log, "Got the TestService.onStop method handle.\n");
 	    fflush(log);
	    
 	    /* run TestService.onStop */
 	    env->CallStaticVoidMethod(cls, onStop);
	    
 	    /* attach to the JVM, return a JNI interface pointer in env */
 	    if(testJNI(jvm->DetachCurrentThread(), 
 		       log, "Unable to detach from the JVM running the service!"))
 	      return; 
 	    fprintf(log, "Detached from the JVM running the service.\n");
 	    fflush(log);
	    fclose(log);

	    /* wait for the main Java thread to finish */ 
	    pThread->Join();
	  }
	}

      void StartJVM2() 
      {
	  /* log file */ 
	  FILE* log = fopen("C:/TEMP/PipelineJobManager-StartJVM2.log", "a+");
	  if(log == NULL) 
	    return;
	  fprintf(log, "Service Starting...\n"); 
	  fflush(log);
  
	  try {
	    while(true) { 
	      Thread::Sleep(10000);
	      fprintf(log, "Service Running...\n"); 
	      fflush(log); 
	    }
	  }
	  catch(ThreadAbortException^) {
	    fprintf(log, "Service Interrupted.\n"); 
	    fflush(log); 
	  }

	  fprintf(log, "Service Stopped.\n"); 
	  fflush(log);
	  fclose(log);
      }

      void StopJVM2()
	{
	  if((pThread != nullptr) && (pThread->IsAlive)) {
	    /* log file */ 
	    FILE* log = fopen("C:/TEMP/PipelineJobManager-StopJVM2.log", "a+");
	    if(log == NULL) 
	      return;
	    fprintf(log, "Signaling Service to Stop...\n"); 
	    fflush(log);

	    pThread->Abort();
	    pThread->Join(500);

	    fprintf(log, "Stopped.\n");
	    fflush(log);
	    fclose(log);
	  }
	}

    private:
      /// <summary>
      /// Thread running the Java Virtual Machine and the JVM itself.
      /// </summary>
      Thread^ pThread;
    
      /// <summary>
      /// Required designer variable.
      /// </summary>
      System::ComponentModel::Container ^components;
    
#pragma region Windows Form Designer generated code
      /// <summary>
      /// Required method for Designer support - do not modify
      /// the contents of this method with the code editor.
      /// </summary>
      void InitializeComponent(void)
	{
	  // 
	  // PipelineJobManagerWinService
	  // 
	  this->ServiceName = L"PipelineJobManagerWinService";

	}
#pragma endregion
    };
}
