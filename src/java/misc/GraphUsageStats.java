// $Id: GraphUsageStats.java,v 1.3 2009/11/12 04:55:15 jim Exp $

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
      if(args.length != 2) {
	System.out.print("usage: GraphUsageStats *-users.glue *-days.glue\n\n"); 
	System.exit(1);
      }

      new GraphUsageStats(new File(args[0]), new File(args[1])); 
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
  GraphUsageStats
  (
   File ufile, 
   File dfile
  ) 
    throws GlueException, IOException 
  {
    GregorianCalendar cal = new GregorianCalendar();

    {
      TreeMap<Long,Integer> table = 
        (TreeMap<Long,Integer>) GlueDecoderImpl.decodeFile("DailyUsers", ufile);

      TripleMap<Integer,Integer,Integer,Integer> dailyUsers = 
        new TripleMap<Integer,Integer,Integer,Integer>();
      Integer lastYear  = null; 
      Integer lastMonth = null; 
      Integer lastDay   = null;
      {
        Long firstStamp = table.firstKey(); 
        Long lastStamp  = table.lastKey(); 
          
        cal.setTimeInMillis(firstStamp); 
        while(cal.getTimeInMillis() <= lastStamp) {
          lastYear  = cal.get(Calendar.YEAR);
          lastMonth = cal.get(Calendar.MONTH); 
          lastDay   = cal.get(Calendar.DAY_OF_MONTH);             
          dailyUsers.put(lastYear, lastMonth, lastDay, 0); 
          cal.add(Calendar.DAY_OF_MONTH, 1);
        }


        for(Long stamp : table.keySet()) {
          Integer numUsers = table.get(stamp); 

          cal.setTimeInMillis(stamp);        
          int year  = cal.get(Calendar.YEAR);
          int month = cal.get(Calendar.MONTH); 
          int day   = cal.get(Calendar.DAY_OF_MONTH); 
            
          dailyUsers.put(year, month, day, numUsers); 
        }
      }

      TripleMap<Integer,Integer,Integer,Double> weeklyUsers = 
        rollingAverage(dailyUsers, 7); 

      TripleMap<Integer,Integer,Integer,Double> monthlyUsers = 
        rollingAverage(dailyUsers, 30); 

      TripleMap<Integer,Integer,Integer,Double> quarterlyUsers = 
        rollingAverage(dailyUsers, 90); 

      {
        FileWriter out = new FileWriter("./daily-users.raw"); 
        
        boolean first = true;
        for(Integer year : dailyUsers.keySet()) {
          for(Integer month : dailyUsers.keySet(year)) {
            for(Integer day : dailyUsers.keySet(year, month)) {
              Integer numUsers = dailyUsers.get(year, month, day); 
              Double wavg = weeklyUsers.get(year, month, day); 
              Double mavg = monthlyUsers.get(year, month, day); 
              Double qavg = quarterlyUsers.get(year, month, day); 
              
              cal.set(year, month, day); 
              Long stamp = cal.getTimeInMillis(); 
              
              out.write(stamp + "\t" + numUsers + "\t" + wavg + "\t" + mavg + "\t" + qavg); 
              
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
      
      GlueEncoderImpl.encodeFile
        ("Daily", dailyUsers, new File("./users-baked.glue")); 

      GlueEncoderImpl.encodeFile
        ("Average", quarterlyUsers, new File("./users-avg.glue")); 
    }

    {
      TreeMap<String,Integer> table = 
        (TreeMap<String,Integer>) GlueDecoderImpl.decodeFile("UserDays", dfile);

      UserDays[] userDays = new UserDays[table.size()]; 
      {
        int i = 0;
        for(String uname : table.keySet()) {
          userDays[i] = new UserDays(uname, table.get(uname));
          i++;
        }

        Arrays.sort(userDays); 
      }

      FileWriter out = new FileWriter("./user-days.raw"); 

      int i = 0; 
      out.write(i + "\t0\t\"\"\t\n"); 
      i++;

      for(UserDays ud : userDays) {
        out.write(i + "\t" + ud + "\n"); 
        i++;
      }

      out.write(i + "\t0\t\"\"\t\n"); 

      out.close(); 
    }      
  }

  private TripleMap<Integer,Integer,Integer,Double>
  rollingAverage
  (
   TripleMap<Integer,Integer,Integer,Integer> dailyUsers, 
   int window
  ) 
  {
    GregorianCalendar cal = new GregorianCalendar();

    TripleMap<Integer,Integer,Integer,Double> averageUsers = 
      new TripleMap<Integer,Integer,Integer,Double>();

    ArrayDeque<Integer> fifo = new ArrayDeque<Integer>(); 

    Double avg = null;
    int cnt = 0;
    for(Integer year : dailyUsers.keySet()) {
      for(Integer month : dailyUsers.keySet(year)) {
        for(Integer day : dailyUsers.keySet(year, month)) {
          Integer numUsers = dailyUsers.get(year, month, day); 

          fifo.addLast(numUsers); 
          while(fifo.size() > window) 
            fifo.removeFirst(); 

          Integer counts[] = fifo.toArray(new Integer[0]);
          Arrays.sort(counts);

          int nonzero = -1; 
          {
            int wk; 
            for(wk=0; wk<counts.length; wk++) {
              if(counts[wk] > 0) {
                nonzero = wk;
                break;
              }
            }
          }
          
          Integer median = null;
          if(nonzero == -1) {
            median = 0;
          }
          else {
            int idx = (nonzero + counts.length - 1) / 2;
            median = counts[idx];
          }

          if(avg == null) 
            avg = (double) median;
          else
            avg = 0.75*avg + 0.25*median;
          averageUsers.put(year, month, day, avg); 

          cnt++;
        }
      }
    }

    return averageUsers; 
  }

  private class
  UserDays
    implements Comparable<UserDays>   
  {
    public
    UserDays
    (
     String name, 
     int numDays
    ) 
    {
      pName = name; 
      pNumDays = numDays;
    }

    public String
    toString()
    {
      return (pNumDays + "\t\"" + pName + "\"");
    }

    public boolean
    equals
    (
     Object obj
    )
    {
      if((obj != null) && (obj instanceof UserDays)) {
        UserDays ud = (UserDays) obj;
        return pNumDays.equals(ud.pNumDays); 
      }
      return false;
    }

    public int
    compareTo
    (
     UserDays ud
    )
    {
      return pNumDays.compareTo(ud.pNumDays);
    }

    private String  pName; 
    private Integer pNumDays; 
  }
}
