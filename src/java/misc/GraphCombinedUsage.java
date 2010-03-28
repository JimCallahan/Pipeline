// $Id: GraphUsageStats.java,v 1.3 2009/11/12 04:55:15 jim Exp $

import java.io.*; 
import java.util.*; 
import java.math.*; 
import java.text.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G R A P H   C O M B I N E D   U S A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
class GraphCombinedUsage
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
      if((args.length < 4) || (args.length%2 != 0)){
	System.out.print("usage: GraphCombinedUsage A-users-baked.glue A-users-avg.glue\n" + 
                         "          B-users-baked.glue B-users-avg.glue ...\n\n"); 
	System.exit(1);
      }

      ArrayList<File> baked = new ArrayList<File>();
      ArrayList<File> averaged = new ArrayList<File>();
      
      int wk;
      for(wk=0; wk<args.length-1; wk+=2) {
        baked.add(new File(args[wk]));
        averaged.add(new File(args[wk+1]));
      }

      new GraphCombinedUsage(baked, averaged); 
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  GraphCombinedUsage
  (
   ArrayList<File> baked,
   ArrayList<File> averaged
  ) 
    throws GlueException, IOException 
  {
    GregorianCalendar cal = new GregorianCalendar();

    /* sum up the daily usage from each site */ 
    TripleMap<Integer,Integer,Integer,Integer> totalDailyUsers = 
      new TripleMap<Integer,Integer,Integer,Integer>();
    for(File bfile : baked) {
      TripleMap<Integer,Integer,Integer,Integer> dailyUsers = 
        (TripleMap<Integer,Integer,Integer,Integer>) 
          GlueDecoderImpl.decodeFile("Daily", bfile);

      for(Integer year : dailyUsers.keySet()) {
        for(Integer month : dailyUsers.keySet(year)) {
          for(Integer day : dailyUsers.keySet(year, month)) {
            Integer total = totalDailyUsers.get(year, month, day);
            Integer numUsers = dailyUsers.get(year, month, day);
            if(numUsers != null) {
              if(total != null) 
                totalDailyUsers.put(year, month, day, numUsers+total); 
              else 
                totalDailyUsers.put(year, month, day, numUsers); 
            }
          }
        }
      }
    }

    /* sum up the quarterly average usage from each site */ 
    TripleMap<Integer,Integer,Integer,Double> totalQuarterlyUsers = 
      new TripleMap<Integer,Integer,Integer,Double>();
    for(File afile : averaged) {
      TripleMap<Integer,Integer,Integer,Double> quarterlyUsers = 
        (TripleMap<Integer,Integer,Integer,Double>) 
           GlueDecoderImpl.decodeFile("Average", afile);
      
      for(Integer year : quarterlyUsers.keySet()) {
        for(Integer month : quarterlyUsers.keySet(year)) {
          for(Integer day : quarterlyUsers.keySet(year, month)) {
            Double total = totalQuarterlyUsers.get(year, month, day);
            Double numUsers = quarterlyUsers.get(year, month, day);
            if(numUsers != null) {
              if(total != null) 
                totalQuarterlyUsers.put(year, month, day, numUsers+total); 
              else 
                totalQuarterlyUsers.put(year, month, day, numUsers); 
            }
          }
        }
      }
    }

    /* write the raw file input for gnuplot */ 
    {
      FileWriter out = new FileWriter("./combined-users.raw"); 

      Integer lastYear  = totalDailyUsers.lastKey();
      Integer lastMonth = totalDailyUsers.get(lastYear).lastKey();
      Integer lastDay   = totalDailyUsers.get(lastYear, lastMonth).lastKey();

      boolean first = true;
      for(Integer year : totalDailyUsers.keySet()) {
        for(Integer month : totalDailyUsers.keySet(year)) {
          for(Integer day : totalDailyUsers.keySet(year, month)) {
            Integer numUsers = totalDailyUsers.get(year, month, day); 
            Double qavg = totalQuarterlyUsers.get(year, month, day); 
            
            cal.set(year, month, day); 
            Long stamp = cal.getTimeInMillis(); 
            
            out.write(stamp + "\t" + numUsers + "\t" + qavg); 
            
            if(first || 
               (day == 1) || 
               (lastYear.equals(year) && lastMonth.equals(month) && lastDay.equals(day))) {
              out.write("\t\"" + (month+1) + "-" + day + "-" + year + "\"");
              first = false;
            }
            
            out.write("\n"); 
          }
        }
      }
      
      out.close(); 
    }
  }

}
