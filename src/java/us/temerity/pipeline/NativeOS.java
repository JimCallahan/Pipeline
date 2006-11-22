// $Id: NativeOS.java,v 1.5 2006/11/22 09:08:00 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   O S                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for querying operating system parameters.
 */
public
class NativeOS
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the total amount of system memory (in bytes).
   * 
   * @throws IOException 
   *   If unable to determine the amount of total memory.
   */ 
  public static long
  getTotalMemory() 
    throws IOException
  {
    long memory = 0;
    switch(PackageInfo.sOsType) {
    case Unix:
      {
	FileReader reader = new FileReader("/proc/meminfo");
	while(true) {
	  /* read a line */ 
	  StringBuilder buf = new StringBuilder();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) 
	      break;
	    
	    char c = (char) next;
	    if(c == '\n') 
	      break;
	    
	    buf.append(c);
	  }
	  
	  String[] fields = buf.toString().split(" ");
	  if(fields[0].equals("MemTotal:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		memory = Long.parseLong(fields[wk]) * 1024L;
		break;
	      }
	    }
	    
	    break;
	  }
	}
	reader.close();
      }
      break;

    case MacOS:
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-n");
	args.add("hw.physmem");
	
	SubProcessLight proc = 
	  new SubProcessLight("TotalMemory", "/usr/sbin/sysctl", 
			      args, new TreeMap<String,String>(), 
			      PackageInfo.sTempPath.toFile());
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
	
	if(!proc.wasSuccessful()) 
	  throw new IOException
	    ("Unable to determine the amount of free memory:\n" + 
	     proc.getStdErr());
	
	String output[] = proc.getStdOut().split("\\n");
	if(output.length < 1)
	  throw new IOException
	    ("Missing output from sysctl(8)!");

	try {
	  memory = Long.valueOf(output[0]);
	}
	catch(NumberFormatException ex) {
	  throw new IOException
	    ("Incomprehensible output from sysctl(8):\n" + 
	     getFullMessage(ex));
	}
      }
      break;

    case Windows:
      throw new IOException("The Windows operating system is not supported!");
    }

    return memory;
  }

  /**
   * Get the amount of free system memory (in bytes). 
   * 
   * @throws IOException 
   *   If unable to determine the amount of free memory.
   */
  public static long
  getFreeMemory() 
    throws IOException
  {
    long memory = 0;
    switch(PackageInfo.sOsType) {
    case Unix:
      {
    	long unused = 0;
	long cached = 0;
	
	FileReader reader = new FileReader("/proc/meminfo");
	while(true) {
	  /* read a line */ 
	  StringBuilder buf = new StringBuilder();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) 
	      break;

	    char c = (char) next;
	    if(c == '\n') 
	      break;

	    buf.append(c);
	  }
	  
	  String[] fields = buf.toString().split(" ");
	  if(fields[0].equals("MemFree:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		unused = Long.parseLong(fields[wk]);
		break;
	      }
	    }
	  }
	  else if(fields[0].equals("Cached:")) {
	    int wk;
	    for(wk=1; wk<fields.length; wk++) {
	      if(fields[wk].length() > 0) {
		cached = Long.parseLong(fields[wk]);
		break;
	      }
	    }
	    
	    break;
	  }
	}
	reader.close();
	
	/* free memory: (unused + file cache) * page size */ 
	memory = ((unused + cached) * 1024L);
      } 
      break;
      
    case MacOS:
      {
	String output[] = null;
	{
	  SubProcessLight proc = 
	    new SubProcessLight("VMStat", "/usr/bin/vm_stat", 
				new ArrayList<String>(), new TreeMap<String,String>(), 
				PackageInfo.sTempPath.toFile());
	  proc.start();
    
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	       ex.getMessage());
	  }
	  
	  if(!proc.wasSuccessful()) 
	    throw new IOException
	      ("Unable to determin the amount of free memory:\n" + 
	       proc.getStdErr());
	  
	  output = proc.getStdOut().split("\\n");
	}

	if(output.length < 4)
	  throw new IOException
	    ("Missing output from vm_stat(1)!");

	try {
	  long pageSize = Long.parseLong(output[0].split("\\s")[7]);

	  long freePages = 0L;
	  {
	    String fields[] = output[1].split("\\s");
	    String str = fields[fields.length-1];
	    freePages = Long.parseLong(str.substring(0, str.length()-1));
	  }

	  long inactivePages = 0L;
	  {
	    String fields[] = output[3].split("\\s");
	    String str = fields[fields.length-1];
	    inactivePages = Long.parseLong(str.substring(0, str.length()-1));
	  }	    

	  memory = pageSize * (freePages + inactivePages);
	}
	catch(Exception ex) {
	  throw new IOException
	    ("Incomprehensible output from vm_stat(1):\n" + 
	     getFullMessage(ex));
	}
      }
      break;  

    case Windows:
      throw new IOException("The Windows operating system is not supported!");
    }

    return memory;
  }

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  private static String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuilder buf = new StringBuilder();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }

     
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of processors (CPUs).
   * 
   * @throws IOException 
   *   If unable to determine the number of processors. 
   */
  public static int
  getNumProcessors() 
    throws IOException
  {
    int procs = 0;
    switch(PackageInfo.sOsType) {
    case Unix:
      {
	FileReader reader = new FileReader("/proc/cpuinfo");
	boolean done = false;
	while(!done) {
	  /* read a line */ 
	  StringBuilder buf = new StringBuilder();
	  while(true) {
	    int next = reader.read();
	    if(next == -1) {
	      done = true;
	      break;
	    }

	    char c = (char) next;
	    if(c == '\n') 
	      break;

	    buf.append(c);
	  }

	  String line = buf.toString();
	  if(line.startsWith("processor")) 
	    procs++;
	}
	reader.close();	
      }
      break;

    case MacOS:
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-n");
	args.add("hw.ncpu");
	
	SubProcessLight proc = 
	  new SubProcessLight("TotalMemory", "/usr/sbin/sysctl", 
			      args, new TreeMap<String,String>(), 
			      PackageInfo.sTempPath.toFile());
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
	
	if(!proc.wasSuccessful()) 
	  throw new IOException
	    ("Unable to determine the number of processors:\n" + 
	     proc.getStdErr());
	
	String output[] = proc.getStdOut().split("\\n");
	if(output.length < 1)
	  throw new IOException
	    ("Missing output from sysctl(8)!");

	try {
	  procs = Integer.valueOf(output[0]);
	}
	catch(NumberFormatException ex) {
	  throw new IOException
	    ("Incomprehensible output from sysctl(8):\n" + 
	     getFullMessage(ex));
	}
      }
      break;

    case Windows:
      throw new IOException("The Windows operating system is not supported!");
    }
    
    return procs;
  }

  /**
   * Get the system load factor (1-minute average). <P> 
   * 
   * On Windows, the load factor is computed by averaging the last (12) instantaneous samples
   * of the length of the processor queue.  It is assumed that the JobMgr collector thread is
   * calling this method at 5-second intervals, so that the average the last (12) samples 
   * should constitute a 1-minute average. The average load factor is also rescaled to make
   * values reported from Windows systems more consistent with the values reported for 
   * Unix/MacOS systems with similar system activity.<P> 
   * 
   * @throws IOException 
   *   If unable to determine the load.
   */
  public static float
  getLoadAverage() 
    throws IOException
  {
    float load = 0.0f;
    switch(PackageInfo.sOsType) {
    case Unix:
      {
	FileReader reader = new FileReader("/proc/loadavg");
	char[] buf = new char[4];
	if(reader.read(buf, 0, 4) == 4) 
	  load = Float.parseFloat(String.valueOf(buf));
	reader.close();
      } 
      break;
      
    case MacOS:
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("-n");
	args.add("vm.loadavg");
	
	SubProcessLight proc = 
	  new SubProcessLight("LoadAverage", "/usr/sbin/sysctl", 
			      args, new TreeMap<String,String>(), 
			      PackageInfo.sTempPath.toFile());
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
	
	if(!proc.wasSuccessful()) 
	  throw new IOException
	    ("Unable to determine the number of processors:\n" + 
	     proc.getStdErr());
	
	String output[] = proc.getStdOut().split("\\n");
	if(output.length < 1)
	  throw new IOException
	    ("Missing output from sysctl(8)!");

	String values[] = output[0].split("\\s");
	if(values.length < 3)
	  throw new IOException
	    ("Missing output from sysctl(8)!");

	try {
	  load = Float.valueOf(values[0]);
	}
	catch(NumberFormatException ex) {
	  throw new IOException
	    ("Incomprehensible output from sysctl(8):\n" + 
	     getFullMessage(ex));
	}
      }
      break;

    case Windows:
      throw new IOException("The Windows operating system is not supported!");
    }

    return load;
  }

}
