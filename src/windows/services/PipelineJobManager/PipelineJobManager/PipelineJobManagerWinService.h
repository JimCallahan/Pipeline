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
	  //
	  //TODO: Add the constructor code here
	  //
	}
    protected:
      /// <summary>
      /// Clean up any resources being used.
      /// </summary>
      ~PipelineJobManagerWinService()
	{
	  if (components)
	    {
	      delete components;
	    }
	}

      /// <summary>
      /// Set things in motion so your service can do its work.
      /// </summary>
      virtual void OnStart(array<String^>^ args) override
	{
	  if((pThread == nullptr) ||
	     ((pThread->ThreadState & 
	       (System::Threading::ThreadState::Unstarted | 
		System::Threading::ThreadState::Stopped)) 
	      != ((System::Threading::ThreadState) 0))) {

	    System::Diagnostics::EventLog^ log = gcnew System::Diagnostics::EventLog;
	    log->Source = "PipelineJobManagerWinService";
	    log->WriteEntry("OnStart: Started.");

	    pThread = gcnew Thread
	      (gcnew ThreadStart(this, &PipelineJobManagerWinService::StartJVM));
	    log->WriteEntry("OnStart: StartJVM Thread Created.");

	    pThread->Start();
	    log->WriteEntry("OnStart: StartJVM Thread Started.");

	    log->WriteEntry("OnStart: Finished.");
	  }
	}

      /// <summary>
      /// Stop this service.
      /// </summary>
      virtual void OnStop() override
	{
	  System::Diagnostics::EventLog^ log = gcnew System::Diagnostics::EventLog;
	  log->Source = "PipelineJobManagerWinService";
	  log->WriteEntry("OnStop: Started.");
	  
	  Thread^ thread = gcnew Thread
	    (gcnew ThreadStart(this, &PipelineJobManagerWinService::StopJVM));
	  log->WriteEntry("OnStop: StopJVM Thread Created.");

	  thread->Start();
	  log->WriteEntry("OnStop: StopJVM Thread Started.");

	  thread->Join();
	  log->WriteEntry("OnStop: StopJVM Joined."); 

	  log->WriteEntry("OnStop: Finished.");
	}

    private:
      /// <summary>
      /// Error check JNI functions.
      /// </summary>
      bool testJNI(jint code, System::Diagnostics::EventLog^ log, String^ msg)
	{
	  switch(code) {
	  case 0:
	    return false;
	
	  case JNI_ERR:
	    log->WriteEntry(String::Format("Unknown error: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	
	  case JNI_EDETACHED:
	    log->WriteEntry(String::Format("Thread detached from the VM: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	
	  case JNI_EVERSION:
	    log->WriteEntry(String::Format("JNI version error: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	
	  case JNI_ENOMEM:
	    log->WriteEntry(String::Format("Not enough memory: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	
	  case JNI_EEXIST:
	    log->WriteEntry(String::Format("VM already created: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	
	  case JNI_EINVAL:
	    log->WriteEntry(String::Format("Invalid arguments: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	 
	  default:
	    log->WriteEntry(String::Format("Bad error code: {0}", msg), 
			    EventLogEntryType::Error);
	    break; 
	  }

	  return true;
	}

      /// <summary>
      /// Start the Java Virtual Machine, call the onStart() method and wait around
      /// for the JVM to exit normally.
      /// </summary>
      void StartJVM() 
	{
	  System::Diagnostics::EventLog^ log = gcnew System::Diagnostics::EventLog;
	  log->Source = "PipelineJobManagerWinService";
	  log->WriteEntry("StartJVM: Started.");
      
 	  /* set the JVM initialization arguments */
 	  JavaVMInitArgs vm_args;
	  {
 	    JavaVMOption options[5];
 	    options[0].optionString = "-Xms8M"; 
 	    options[1].optionString = "-Xmx128M"; 
 	    options[2].optionString = "-Xdebug"; 
 	    options[3].optionString = "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=45006"; 
	    //options[4].optionString = "-Djava.class.path=C:\\TEMP"; 
	    //  	    options[4].optionString = "-Djava.class.path=C:\\TEMP\\api.jar";  
	    options[4].optionString = "-Djava.class.path=\\\\Dimetrodon\\base\\apps\\i686-pc-linux-gnu-dbg\\pipeline-dimetrodon-2.1.8-070122\\lib\\api.jar"; 
      
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
	  log->WriteEntry("StartJVM: Created JVM.");
      
 	  /* get the method handle for JobMgrService.onStart */
	  //jclass cls = env->FindClass("PipelineJobManager");
	  jclass cls = env->FindClass("us/temerity/pipeline/bootstrap/JobMgrService");
	  if(cls == NULL) {
	    log->WriteEntry("Unable to locate the JobMgrService class.", 
			    EventLogEntryType::Error);
	    return;
	  }

 	  jmethodID onStart = env->GetStaticMethodID(cls, "onStart", "()V");
 	  if(onStart == NULL) {
	    log->WriteEntry("Unable to get the JobMgrService.onStart method handle.", 
			    EventLogEntryType::Error);
 	    return;
 	  }
	  log->WriteEntry("StartJVM: Got the JobMgrService.onStart() method handle.");
      
 	  /* run JobMgrService.onStart */
 	  env->CallStaticVoidMethod(cls, onStart);
	  log->WriteEntry("StartJVM: JobMgrService.onStart() Finished.");
      
 	  /* clean up */
 	  jvm->DestroyJavaVM();
	  log->WriteEntry("StartJVM: JVM Destroyed.");

	  log->WriteEntry("StartJVM: Finished.");
	}

      /// <summary>
      /// Attach to the existing Java Virtual Machine, call the onStop() method and 
      /// wait around for the StartJVM thread to complete. 
      /// </summary>
      void StopJVM() 
	{
	  if((pThread != nullptr) && (pThread->IsAlive)) {
	    System::Diagnostics::EventLog^ log = gcnew System::Diagnostics::EventLog;
	    log->Source = "PipelineJobManagerWinService";
	    log->WriteEntry("StopJVM: Started.");

 	    /* lookup the previously created JVM */ 
 	    JavaVM *jvm;   
 	    {
 	      JavaVM *vmBuf[1];
 	      jsize bufLen;
 	      if(testJNI(JNI_GetCreatedJavaVMs(vmBuf, 1, &bufLen),
 			 log, "Unable to find the JVM running the service!"))
 		return; 
	      
 	      if((bufLen != 1) || (vmBuf[0] == NULL)) {
		log->WriteEntry("Missing JVM", 
				EventLogEntryType::Error);
 		return;
 	      }
 	      jvm = vmBuf[0];
 	    } 
	    
 	    /* attach to the existing JVM, return a JNI interface pointer in env */ 
 	    JNIEnv *env; 
 	    if(testJNI(jvm->AttachCurrentThread((void**) &env, NULL), 
 		       log, "Unable to attach to the JVM running the service!"))
 	      return; 
	    log->WriteEntry("StopJVM: Attached to the JVM.");
	    
 	    /* get the method handle for JobMgrService.onStop */
	    //jclass cls = env->FindClass("PipelineJobManager");
	    jclass cls = env->FindClass("us/temerity/pipeline/bootstrap/JobMgrService"); 
	    if(cls == NULL) {
	      log->WriteEntry("Unable to locate the JobMgrService class.", 
			      EventLogEntryType::Error);
	      return;
	    }

 	    jmethodID onStop = env->GetStaticMethodID(cls, "onStop", "()V");
 	    if(onStop == NULL) {
	      log->WriteEntry("Unable to get the JobMgrService.onStop method handle.", 
			      EventLogEntryType::Error);
 	      return;
 	    }
	    log->WriteEntry("StopJVM: Got the JobMgrService.onStop() method handle.");
	    
 	    /* run JobMgrService.onStop */
 	    env->CallStaticVoidMethod(cls, onStop);
	    log->WriteEntry("StopJVM: JobMgrService.onStop() Finished.");
	    
 	    /* attach to the JVM, return a JNI interface pointer in env */
 	    if(testJNI(jvm->DetachCurrentThread(), 
 		       log, "Unable to detach from the JVM running the service!"))
 	      return; 
	    log->WriteEntry("StopJVM: Detached from the JVM.");

	    /* wait for the main Java thread to finish */ 
	    pThread->Join();
	    log->WriteEntry("StopJVM: StartJVM Joined."); 

	    log->WriteEntry("StopJVM: Finished.");
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
	  this->components = gcnew System::ComponentModel::Container();
	  this->CanStop = true;
	  this->CanPauseAndContinue = true;
	  this->AutoLog = true;
	  this->ServiceName = L"PipelineJobManagerWinService";
	}
#pragma endregion
    };
}
