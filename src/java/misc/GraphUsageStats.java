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
      if(args.length != 3) {
	System.out.print("usage: GraphUsageStats *-users.glue *-days.glue dd.mm.yyyy\n\n"); 
	System.exit(1);
      }

      long stamp = 0L; 
      try {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        Date date = fmt.parse(args[2]);
        stamp = date.getTime();
      }
      catch(ParseException ex2) {
        System.out.print("Bad License Date!\n  " + ex2.getMessage() + "\n");
        System.exit(1);
      }

      new GraphUsageStats(new File(args[0]), new File(args[1]), stamp); 
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
   File dfile, 
   long licStamp
  ) 
    throws GlueException, IOException 
  {
    GregorianCalendar cal = new GregorianCalendar();

    {
      TreeMap<Long,Integer> table = 
        (TreeMap<Long,Integer>) GlueDecoderImpl.decodeFile("DailyUsers", ufile);

      TripleMap<Integer,Integer,Integer,Integer> dailyUsers = 
        new TripleMap<Integer,Integer,Integer,Integer>();
      TreeSet<Long> quarters = new TreeSet<Long>();
      Long firstStamp = table.firstKey(); 
      Long lastStamp  = table.lastKey(); 
      {          
        {
          cal.setTimeInMillis(licStamp);  
          int year  = cal.get(Calendar.YEAR);
          int month = cal.get(Calendar.MONTH); 
          int day   = cal.get(Calendar.DAY_OF_MONTH);   

          cal.setTimeInMillis(firstStamp);  
          cal.set(Calendar.YEAR, year);
          cal.set(Calendar.MONTH, month); 
          cal.set(Calendar.DAY_OF_MONTH, day);   
        }

        Long firstLicStamp = cal.getTimeInMillis();
        while(firstLicStamp > firstStamp) {
          cal.add(Calendar.YEAR, -1);
          firstLicStamp = cal.getTimeInMillis();
        } 
        
        Long lastLicStamp = firstLicStamp;
        while(lastLicStamp < lastStamp) {
          cal.add(Calendar.YEAR, 1);
          lastLicStamp = cal.getTimeInMillis();
        }

        {
          long stamp = firstLicStamp; 
          while(stamp < lastLicStamp) {
            quarters.add(stamp);
            cal.setTimeInMillis(stamp);  
            cal.add(Calendar.MONTH, 3);
            stamp = cal.getTimeInMillis();
          }
          quarters.add(lastLicStamp);

          for(long s : quarters) {
            cal.setTimeInMillis(s); 
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH); 
            int day   = cal.get(Calendar.DAY_OF_MONTH);   
            
            dailyUsers.put(year, month, day, 0); 
          }
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
        rollingAverage(dailyUsers, 5, firstStamp, lastStamp); 

      TripleMap<Integer,Integer,Integer,Double> monthlyUsers = 
        rollingAverage(dailyUsers, 21, firstStamp, lastStamp); 

      TripleMap<Integer,Integer,Integer,Double> quarterlyUsers = 
        rollingAverage(dailyUsers, 64, firstStamp, lastStamp); 

      {
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");

        long graphStart = quarters.first();
        while(graphStart < quarters.last()) {
          cal.setTimeInMillis(graphStart);        
          cal.add(Calendar.YEAR, 1);
          long graphEnd = cal.getTimeInMillis();

          FileWriter out = 
            new FileWriter("./daily-users." + 
                           fmt.format(new Date(graphStart)) + "." +
                           fmt.format(new Date(graphEnd)) + ".raw"); 

          for(Integer year : dailyUsers.keySet()) {
            for(Integer month : dailyUsers.keySet(year)) {
              for(Integer day : dailyUsers.keySet(year, month)) {

                cal.set(year, month, day); 
                Long stamp = cal.getTimeInMillis(); 

                if((graphStart <= stamp) && (stamp <= graphEnd)) {
                  Integer numUsers = dailyUsers.get(year, month, day); 
                  Double wavg = weeklyUsers.get(year, month, day); 
                  Double mavg = monthlyUsers.get(year, month, day); 
                  Double qavg = quarterlyUsers.get(year, month, day); 
                  
                  out.write(stamp + "\t" + numUsers + "\t" + wavg + "\t" + 
                            mavg + "\t" + qavg); 
                
                  if(quarters.contains(stamp)) 
                    out.write("\t\"" + (month+1) + "-" + day + "-" + year + "\"");
                
                  out.write("\n"); 
                }
              }
            }
          }
          out.close(); 

          graphStart = graphEnd;
        }
      }
      
      GlueEncoderImpl.encodeFile
        ("Daily", dailyUsers, new File("./users-baked.glue")); 

      GlueEncoderImpl.encodeFile
        ("Average", quarterlyUsers, new File("./users-avg.glue")); 
    }
  }

  private TripleMap<Integer,Integer,Integer,Double>
  rollingAverage
  (
   TripleMap<Integer,Integer,Integer,Integer> dailyUsers, 
   int window, 
   long firstStamp, 
   long lastStamp
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

          cal.set(year, month, day);   
          switch(cal.get(Calendar.DAY_OF_WEEK)) {
          case Calendar.SATURDAY:
          case Calendar.SUNDAY:
            /* don't consider non-work days */
            break; 

          default:
            fifo.addLast(numUsers); 
            while(fifo.size() > window) 
              fifo.removeFirst(); 
          }

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

          avg = (double) median;

          Long stamp = cal.getTimeInMillis(); 
          if((firstStamp <= stamp) && (stamp <= lastStamp))           
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
