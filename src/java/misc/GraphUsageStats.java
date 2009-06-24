// $Id: GraphUsageStats.java,v 1.1 2009/06/24 02:42:36 jim Exp $

import java.io.*; 
import java.util.*; 
import java.math.*; 
import java.text.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G R A P H   U S A G E   S T A T S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
class GraphUsageStats
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
      if(args.length != 1) {
	System.out.print("usage: GraphUsageStats usage-stats.glue\n\n"); 
	System.exit(1);
      }

      File sfile = new File(args[0]);

      TreeMap<Long,TreeMap<String,Integer>> stats = 
        (TreeMap<Long,TreeMap<String,Integer>>) 
          GlueDecoderImpl.decodeFile("UsageStats", sfile);
      
      FileWriter ecounts = new FileWriter("./event-counts.raw"); 
      FileWriter ucounts = new FileWriter("./user-counts.raw"); 
      TreeMap<String,FileWriter> uecounts = new TreeMap<String,FileWriter>(); 

      GregorianCalendar cal = new GregorianCalendar();
      for(Map.Entry<Long,TreeMap<String,Integer>> entry : stats.entrySet()) {
        Long stamp = entry.getKey(); 
        cal.setTimeInMillis(stamp);
        
        int year = cal.get(Calendar.YEAR);
        int day  = cal.get(Calendar.DAY_OF_YEAR);

        double when = year + (((double) day) / 365.0);

        long total = 0L;
        TreeMap<String,Integer> userCounts = entry.getValue();         
        for(String uname : userCounts.keySet()) {
          FileWriter out = uecounts.get(uname);
          if(out == null) {
            out = new FileWriter("./" + uname + ".raw");
            uecounts.put(uname, out);
          }

          int num = userCounts.get(uname);
          total += num;

          out.write(when + "\t" + num + "\n");
        }

        ecounts.write(when + "\t" + total + "\n");          
        ucounts.write(when + "\t" + userCounts.size() + "\n");          
      }
      
      for(FileWriter out : uecounts.values()) 
        out.close(); 
      
      ecounts.close();
      ucounts.close();
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
