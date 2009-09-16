// $Id: FakeHostsGenerator.java,v 1.2 2009/09/16 23:36:41 jesse Exp $

import java.io.*; 
import java.util.*; 
import java.math.*; 
import java.text.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F A K E   H O S T S   G E N E R A T O R                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates the GLUE file format used to store Job Server hosts filling it with fake
 * entries.  This is useful for testing the speed of the UI with large numbers of servers
 * without actually having the servers.
 */ 
class FakeHostsGenerator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    try {
      if(args.length != 3) {
	System.out.print("usage: FakeHostsGenerator num-hosts num-slots out-file\n\n"); 
	System.exit(1);
      }

      int numHosts = new Integer(args[0]);
      int numSlots = new Integer(args[1]);
      Path out = new Path(args[2]);
      
      TreeMap<String,QueueHostInfo> hosts = new TreeMap<String,QueueHostInfo>();

      DecimalFormat fmt = new DecimalFormat("0000");   

      int ck; 
      for(ck=0; ck<numHosts; ck++) {
        QueueHostInfo info = 
          new QueueHostInfo("fake" + fmt.format(ck), 
                            QueueHostStatus.Shutdown, 
                            null, 0, numSlots, null, null, null, null, null, null, 
                            null, "Unix", null, null, null, JobGroupFavorMethod.None,
                            EditableState.Manual, 
                            EditableState.Manual, 
                            EditableState.Manual, 
                            EditableState.Manual, 
                            EditableState.Manual,
                            EditableState.Manual,
                            EditableState.Manual,
                            EditableState.Manual); 
        hosts.put(info.getName(), info); 
      }
      
      GlueEncoderImpl.encodeFile("Hosts", hosts, out.toFile());
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
