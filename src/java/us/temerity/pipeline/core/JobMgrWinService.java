// $Id: JobMgrWinService.java,v 1.2 2007/02/13 02:49:49 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.WinService; 

import java.io.*; 
import java.util.*;
import java.util.concurrent.atomic.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   W I N   S E R V I C E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The Job Manager windows service. <P> 
 */
public
class JobMgrWinService
  extends WinService
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a windows service. 
   */ 
  public
  JobMgrWinService()
  {}

  
  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S   S E R V I C E                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the processing that occurs when the service receives a Start command. <P> 
   * 
   * Subclasses should implement this method as the top-level entry function and main loop 
   * for the service which does not return as long as the service is still running. 
   */ 
  public void 
  onStart()
  {
    String args[] = {
      "--standard-log-file",
      "--log=all:finest", 
      "--fail-fast"
    };

    JobMgrApp app = new JobMgrApp();
    app.run(args);
  }
  
  /**
   * Performs the processing that occurs when the service receives a Stop command. <P> 
   * 
   * Subclasses should implement this method to asynchronously signal the shutdown of the 
   * service.  This method need not wait on the shutdown to complete.  Typically, this 
   * method simply sets a flag to signal the shutdown which the {@link onStart} method
   * periodically checks.
   */ 
  public void 
  onStop()
  { 
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Windows Service Shutdown..."); 
    LogMgr.getInstance().flush();

    try {
      String hostname = null;
      {
	TreeSet hostnames = new TreeSet();
	try {
	  Enumeration nets = NetworkInterface.getNetworkInterfaces();  
	  while(nets.hasMoreElements()) {
	    NetworkInterface net = (NetworkInterface) nets.nextElement();
	    Enumeration addrs = net.getInetAddresses();
	    while(addrs.hasMoreElements()) {
	      InetAddress addr = (InetAddress) addrs.nextElement();
	      if((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) 
		hostnames.add(addr.getCanonicalHostName());
	    }
	  }
	  
	  if(hostnames.isEmpty()) 
	    throw new IOException();
	}
	catch(Exception ex) {
	  throw new PipelineException("Could not determine the name of this machine!");
	}
	
	hostname = (String) hostnames.first();
      }

      JobMgrControlClient client = new JobMgrControlClient(hostname);
      client.shutdown();
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Warning,
	 ex.getMessage());
    }
  }


//   /**
//    * Performs the processing that occurs when the service receives a Start command. <P> 
//    * 
//    * Subclasses should implement this method as the top-level entry function and main loop 
//    * for the service which does not return as long as the service is still running. 
//    */ 
//   public void 
//   onStart()
//   {
//     try {
//       File file = new File("C:/TEMP/JobMgrWinService-OnStart.log");
//       if(file.exists()) 
// 	file.delete();

//       FileWriter out = new FileWriter(file);
      
//       {
// 	Date now = new Date();
// 	out.write("STARTED: " + now + "\n");
// 	out.flush();

// 	System.out.print("STARTED: " + now + "\n");
// 	System.err.print("STARTED: " + now + "\n");
//       }
      
//       while(!pShutdown.get()) {
// 	Date now = new Date();
// 	out.write("  Running: " + now + "\n");
// 	out.flush();

// 	System.out.print("  Running: " + now + "\n");

// 	Thread.sleep(5000);
//       }
      
//       {
// 	Date now = new Date();
// 	out.write("STOPPED: " + now + "\n");
// 	out.flush();

// 	System.out.print("STOPPED: " + now + "\n");
// 	System.err.print("STOPPED: " + now + "\n");
//       }

//       out.close();
//     }
//     catch(Exception ex) {
//       ex.printStackTrace();
//       System.exit(1);
//     }
//   }
  
//   /**
//    * Performs the processing that occurs when the service receives a Stop command. <P> 
//    * 
//    * Subclasses should implement this method to asynchronously signal the shutdown of the 
//    * service.  This method need not wait on the shutdown to complete.  Typically, this 
//    * method simply sets a flag to signal the shutdown which the {@link onStart} method
//    * periodically checks.
//    */ 
//   public void 
//   onStop()
//   {
//     try {
//       File file = new File("C:/TEMP/JobMgrWinService-OnStop.log");
//       if(file.exists()) 
// 	file.delete();

//       FileWriter out = new FileWriter(file);
      
//       {
// 	Date now = new Date();
// 	out.write("Shutting Down: " + now + "\n");
// 	out.flush();
//       }
      
//       pShutdown.set(true);

//       {
// 	Date now = new Date();
// 	out.write("SHUTDOWN: " + now + "\n");
// 	out.flush();
// 	out.close();
//       }
//     }
//     catch(Exception ex) {
//       ex.printStackTrace();
//       System.exit(1);
//     }
//   }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private AtomicBoolean  pShutdown; 
}


