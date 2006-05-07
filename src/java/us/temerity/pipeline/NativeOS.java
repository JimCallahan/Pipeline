// $Id: NativeOS.java,v 1.2 2006/05/07 21:30:07 jim Exp $

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
  extends Native
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
	  StringBuffer buf = new StringBuffer();
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
    case Windows:
      loadLibrary();
      memory = getTotalMemoryNative();    
      break;
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
    	long unused   = 0;
	long cached = 0;
	
	FileReader reader = new FileReader("/proc/meminfo");
	while(true) {
	  /* read a line */ 
	  StringBuffer buf = new StringBuffer();
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

	if(output.length < 2)
	  throw new IOException
	    ("Missing output from vm_stat(1)!");

	try {
	  long pageSize = Long.parseLong(output[0].split("\\s")[7]);

	  String fields[] = output[1].split("\\s");
	  String str = fields[fields.length-1];
	  long freePages = Long.parseLong(str.substring(0, str.length()-1));

	  memory = pageSize * freePages;
	}
	catch(Exception ex) {
	  throw new IOException
	    ("Incomprehensible output from vm_stat(1):\n" + 
	     getFullMessage(ex));
	}
      }
      break;  

    case Windows:
      loadLibrary();
      return getFreeMemoryNative(); 
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
    StringBuffer buf = new StringBuffer();
     
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
	  StringBuffer buf = new StringBuffer();
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
    case Windows:
      loadLibrary();
      procs = getNumProcessorsNative();
      break;
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
      loadLibrary();
      load = getLoadAverageNative();
      break;

    case Windows:
      loadLibrary();      
      sLoadSamples.add(getLoadAverageNative());
      for(Float sample : sLoadSamples) 
	load += sample;
      load /= sLoadSamples.size();
      break;
    }

    return load;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E    H E L P E R S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the amount of free system memory (in bytes). 
   * 
   * @throws IOException 
   *   If unable to determine the amount of total memory.
   */ 
  private static native long 
  getFreeMemoryNative() 
    throws IOException;
  
  /**
   * Get the total amount of system memory (in bytes).
   * 
   * @throws IOException 
   *   If unable to determine the amount of total memory.
   */ 
  private static native long 
  getTotalMemoryNative() 
    throws IOException;
  
  /**
   * Get the number of processors (CPUs).
   * 
   * @throws IOException 
   *   If unable to determine the load.
   */
  private static native int
  getNumProcessorsNative() 
    throws IOException;

  /**
   * Get the system load factor (1-minute average).
   * 
   * @throws IOException 
   *   If unable to determine the load.
   */
  private static native float
  getLoadAverageNative() 
    throws IOException;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A ring buffer used to store the last 12 instantaneous processor active percentage 
   * samples (collected at 5-second intervals) from a Windows system to be used to compute 
   * a 1-minute average system load equilavent. <P> 
   * 
   * The values are initialized to (1.0) to over report the system load during the 
   * first minute of sampling.
   */ 
  private static Ring<Float>  sLoadSamples = new Ring<Float>(12, new Float(1.0f));
}
