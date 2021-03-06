// $Id: NativeOS.java,v 1.11 2009/05/31 23:14:51 jim Exp $

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
		
	SubProcessLight proc = null;
        try {
          proc = new SubProcessLight("TotalMemory", "/usr/sbin/sysctl", 
                                     args, new TreeMap<String,String>(), 
                                     PackageInfo.sTempPath.toFile());
          proc.start();
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
        catch(PipelineException ex) {
          throw new IOException
            ("Unable to create the subprocess needed to get total system memory:\n" + 
             "  " + ex.getMessage());
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
	     Exceptions.getFullMessage(ex));
	}
      }
      break;

    case Windows:
      loadLibrary();
      memory = getTotalMemoryNative();
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
    long memory = 0L;
    switch(PackageInfo.sOsType) {
    case Unix:
      {
    	long unused = 0L;
	long cached = 0L;
	
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
          SubProcessLight proc = null;
          try {
            proc = new SubProcessLight("VMStat", "/usr/bin/vm_stat", 
                                       new ArrayList<String>(), new TreeMap<String,String>(), 
                                       PackageInfo.sTempPath.toFile());
            proc.start();
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	       ex.getMessage());
	  }
          catch(PipelineException ex) {
            throw new IOException
              ("Unable to create the subprocess needed to get free system memory:\n" + 
               "  " + ex.getMessage());
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
	     Exceptions.getFullMessage(ex));
	}
      }
      break;  

    case Windows:
      loadLibrary();
      memory = getFreeMemoryNative();
    }

    return memory;
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
	
	SubProcessLight proc = null;
        try {
          proc = new SubProcessLight("NumProcs", "/usr/sbin/sysctl", 
                                     args, new TreeMap<String,String>(), 
                                     PackageInfo.sTempPath.toFile());
          proc.start();
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
        catch(PipelineException ex) {
          throw new IOException
            ("Unable to create the subprocess needed to determine the number of CPUs:\n" + 
             "  " + ex.getMessage());
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
	     Exceptions.getFullMessage(ex));
	}
      }
      break;

    case Windows:
      loadLibrary();
      procs = getNumProcessorsNative(); 
    }
    
    return procs;
  }

  /**
   * Get the system load factor (1-minute average). <P> 
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
	
	SubProcessLight proc = null;
        try {
          proc = new SubProcessLight("LoadAverage", "/usr/sbin/sysctl", 
                                     args, new TreeMap<String,String>(), 
                                     PackageInfo.sTempPath.toFile());
          proc.start();
	  proc.join();
	}
	catch(InterruptedException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	     ex.getMessage());
	}
        catch(PipelineException ex) {
          throw new IOException
            ("Unable to create the subprocess needed to determine the load average:\n" + 
             "  " + ex.getMessage());
        }
	
	if(!proc.wasSuccessful()) 
	  throw new IOException
	    ("Unable to determine the number of processors:\n" + 
	     proc.getStdErr());
	
        String outstr = proc.getStdOut();
	String output[] = outstr.split("\\n");
	if(output.length < 1)
	  throw new IOException
	    ("Missing output from sysctl(8)!");

	try {
          String values[] = output[0].split("\\s");
          switch(values.length) {
          case 3:
            load = Float.valueOf(values[0]);
            break;

          case 5:
            if(values[0].equals("{") && values[4].equals("}")) 
              load = Float.valueOf(values[1]);
            else 
              throw new IOException
                ("Illegal output from sysctl(8):\n" + 
                 outstr);
            break;

          default:
            throw new IOException
              ("Illegal output from sysctl(8):\n" + 
               outstr);
          }
	}
	catch(NumberFormatException ex) {
	  throw new IOException
	    ("Incomprehensible output from sysctl(8):\n" + 
	     outstr + "\n\n" + 
             Exceptions.getFullMessage(ex));
	}
      }
      break;

    case Windows:
      loadLibrary();
      load = getLoadAverageNative(); 
    }

    return load;
  }

   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract file system path location of the given user's home directory.<P> 
   * 
   * This method uses the Unix getpwent(3) system function call and is therefore only valid 
   * on Unix-like operating systems (Unix or MacOS).
   * 
   * @param user
   *   The name of the user.
   * 
   * @throws IOException 
   *   If unable to lookup the path.
   */
  public static Path
  getUserHomePath
  (
   String user
  ) 
    throws IOException
  {
    Path path = null;
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      {
        String dir = getUserHomePathNative(user);
        if(dir != null) 
          path = new Path(dir);  
      }
      break;

    default:
      throw new IOException("This method is not supported on Windows!");
    }

    return path;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E    H E L P E R S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the total amount of system memory (in bytes).
   * 
   * @throws IOException 
   *   If unable to determine the amount of total memory.
   */ 
  public static native long
  getTotalMemoryNative() 
    throws IOException;
  
  /**
   * Get the amount of free system memory (in bytes). 
   * 
   * @throws IOException 
   *   If unable to determine the amount of free memory.
   */
  public static native long
  getFreeMemoryNative() 
    throws IOException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of processors (CPUs).
   * 
   * @throws IOException 
   *   If unable to determine the number of processors. 
   */
  public static native int
  getNumProcessorsNative() 
    throws IOException;

  /**
   * Get the system load factor (1-minute average). <P> 
   * 
   * @throws IOException 
   *   If unable to determine the load.
   */
  public static native float
  getLoadAverageNative() 
    throws IOException;

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract file system path location of the given user's home directory.
   * 
   * @param user
   *   The name of the user.
   * 
   * @throws IOException 
   *   If unable to lookup the path.
   */
  public static native String
  getUserHomePathNative
  (
   String user
  ) 
    throws IOException;
}
