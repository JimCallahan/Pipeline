// $Id: GraphMemory.java,v 1.1 2009/12/02 12:19:33 jim Exp $

import java.io.*; 
import java.util.*; 
import java.util.regex.*;
import java.math.*; 
import java.text.*;
import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   G R A P H   M E M O R Y                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public class 
GraphMemory
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
	System.out.print("usage: GraphMemory plmaser.log.0 plmaster-gc.log\n");   
	System.exit(1);
      }

      new GraphMemory(new File(args[0]), new File(args[1])); 
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
  GraphMemory
  (
   File logfile, 
   File gcfile
  ) 
    throws IOException, ParseException
  {
    GregorianCalendar cal = new GregorianCalendar();

    long stamps[] = { Long.MAX_VALUE, 0L };

    LinkedList<JVMHeap> heaps = new LinkedList<JVMHeap>();
    LinkedList<CacheStats> repos = new LinkedList<CacheStats>();
    LinkedList<CacheStats> works = new LinkedList<CacheStats>();
    LinkedList<CacheStats> checks = new LinkedList<CacheStats>();
    LinkedList<CacheStats> annots = new LinkedList<CacheStats>();
    {
      BufferedReader in = new BufferedReader(new FileReader(logfile));
      try {
        while(true) {
          try {
            String line = in.readLine();
            if(line == null) 
              throw new IOException();

            JVMHeap heap = parseJVMHeap(in, line);
            if(heap != null) {
              heaps.add(heap);
              updateStamps(stamps, heap.getStamp());
            }
            else {
              CacheStats repo = parseCacheStats(in, line, sCheckedInReport);
              if(repo != null) {
                repos.add(repo);
                updateStamps(stamps, repo.getStamp());
              }
              else {
                CacheStats work = parseCacheStats(in, line, sWorkingReport);
                if(work != null) {
                  works.add(work);
                  updateStamps(stamps, work.getStamp());
                }
                else {
                  CacheStats check = parseCacheStats(in, line, sCheckSumsReport);
                  if(check != null) {
                    checks.add(check);
                    updateStamps(stamps, check.getStamp());
                  }
                  else {
                    CacheStats annot = parseCacheStats(in, line, sAnnotationsReport);
                    if(annot != null) {
                      annots.add(annot);
                      updateStamps(stamps, annot.getStamp());
                    }
                  }
                }
              }
            }
          }
          catch(IOException ex) {
            break;
          }
        }
      }
      finally {
        in.close();
      }
    }

    {
      BufferedWriter out = new BufferedWriter(new FileWriter("mem.raw"));
      try {
        for(JVMHeap heap : heaps)
          out.write(heap + "\n");
      }
      finally {
        out.close();
      }
    }

    {
      BufferedWriter out = new BufferedWriter(new FileWriter("repo.raw"));
      try {
        for(CacheStats cache : repos)
          out.write(cache + "\n");
      }
      finally {
        out.close();
      }
    }

    {
      BufferedWriter out = new BufferedWriter(new FileWriter("work.raw"));
      try {
        for(CacheStats cache : works)
          out.write(cache + "\n");
      }
      finally {
        out.close();
      }
    }

    {
      BufferedWriter out = new BufferedWriter(new FileWriter("check.raw"));
      try {
        for(CacheStats cache : checks)
          out.write(cache + "\n");
      }
      finally {
        out.close();
      }
    }


    {
      BufferedWriter out = new BufferedWriter(new FileWriter("annot.raw"));
      try {
        for(CacheStats cache : annots)
          out.write(cache + "\n");
      }
      finally {
        out.close();
      }
    }

    {
      BufferedWriter out = new BufferedWriter(new FileWriter("label.raw"));
      try {
        cal.setTimeInMillis(stamps[1]);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);   
        long end = cal.getTimeInMillis();

        cal.setTimeInMillis(stamps[0]);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long start = cal.getTimeInMillis();

        long tm;
        for(tm=start; tm<=end; tm+=21600000) {
          Integer y = cal.get(Calendar.YEAR);
          Integer m = cal.get(Calendar.MONTH);
          Integer d = cal.get(Calendar.DAY_OF_MONTH);
          Integer h = cal.get(Calendar.HOUR_OF_DAY);
        
          if(h == 0) 
            out.write(tm + "\t" + (m+1) + "-" + d + "-" + y + "\n");
          else 
            out.write(tm + "\t" + h + "h\n");

          cal.add(Calendar.HOUR_OF_DAY, 6);       
        }
      }
      finally {
        out.close();
      }
    }
  }

  private void 
  updateStamps
  (
   long bounds[], 
   long stamp
  ) 
  {
    bounds[0] = Math.min(bounds[0], stamp); 
    bounds[1] = Math.max(bounds[1], stamp); 
  }

  private JVMHeap
  parseJVMHeap
  (
   BufferedReader in, 
   String firstLine
  ) 
    throws IOException, ParseException
  {
    Long stamp = null;
    {
      Matcher m = sMemoryReport.matcher(firstLine);
      if(m.matches()) 
        stamp = TimeStamps.parse(m.group(1));
      else
        return null;
    }
      
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();
      if(!line.equals("  ---- JVM HEAP ----------------------"))
        return null;
    }

    long mega = 1024*1024;

    Long free = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sFreeMemory.matcher(line);
      if(m.matches()) {
        free = new Long(m.group(1)); 
        free /= mega;
      }
      else
        return null;
    }
    
    Long min = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sMinMemory.matcher(line);
      if(m.matches()) {
        min = new Long(m.group(1));
        min /= mega;
      }
      else
        return null;
    }
    
    Long max = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sMaxMemory.matcher(line);
      if(m.matches()) {
        max = new Long(m.group(1));
        max /= mega;
      }
      else
        return null;
    }

    return new JVMHeap(stamp, free, min, max);
  }

  private CacheStats
  parseCacheStats
  (
   BufferedReader in,
   String firstLine, 
   Pattern pat
  ) 
    throws IOException, ParseException
  {
    Long stamp = null;
    {
      Matcher m = pat.matcher(firstLine);
      if(m.matches()) 
        stamp = TimeStamps.parse(m.group(1));
      else
        return null;
    }

    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();
      if(!line.equals("  ---- CACHE STATS -------------------"))
        return null;
    }

    Long cached = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sCached.matcher(line);
      if(m.matches()) 
        cached = new Long(m.group(1)); 
      else
        return null;
    }
    
    Long min = null;
    Long max = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sMinMax.matcher(line);
      if(m.matches()) {
        min = new Long(m.group(1));
        max = new Long(m.group(2));
      }
      else
        return null;
    }
    
    Long hit = null;
    Long miss = null;
    {
      String line = in.readLine();
      if(line == null) 
        throw new IOException();

      Matcher m = sHitMiss.matcher(line);
      if(m.matches()) {
        hit = new Long(m.group(1));
        miss = new Long(m.group(2));
      }
      else
        return null;
    }

    return new CacheStats(stamp, cached, min, max, hit, miss); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private class
  Stamp
  {
    public 
    Stamp
    (
     long stamp
    )
    {
      pStamp = stamp;
    }

    public String
    toString()
    {
      return (pStamp + "\t"); 
    }

    public long
    getStamp()
    {
      return pStamp;
    }

    private long pStamp; 
  }

  private class
  JVMHeap
    extends Stamp
  {
    public 
    JVMHeap
    (
     long stamp, 
     long free, 
     long min, 
     long max
    )
    {
      super(stamp);
      pFree = free;
      pMin  = min; 
      pMax  = max;
    }

    public String
    toString()
    {
      return (super.toString() + pFree + "\t" + pMin + "\t" + pMax + "\t");
    }

    private long pFree;
    private long pMin;
    private long pMax;
  }

  private class
  CacheStats
    extends Stamp
  {
    public 
    CacheStats
    (
     long stamp, 
     long cached,
     long min, 
     long max, 
     long hit, 
     long miss
    )
    {
      super(stamp);
      pCached = cached;
      pMin  = min; 
      pMax  = max;
      pHit  = hit;
      pMiss = miss;
      pDisk = (miss+hit > 0) ? ((double) miss) / ((double) miss+hit) : 0.0;
    }

    public String
    toString()
    {
      DecimalFormat fmt = new DecimalFormat("#.##");
      return (super.toString() + pCached + "\t" + pMin + "\t" + pMax + "\t" + pHit + "\t" + 
              pMiss + "\t" + fmt.format(pDisk) + "\t");
    }

    private long pCached;
    private long pMin;
    private long pMax;
    private long pHit;
    private long pMiss;
    private double pDisk;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String sHeader = 
    ("(\\p{Digit}{4}-\\p{Digit}{2}-\\p{Digit}{2} " + 
     "\\p{Digit}{2}:\\p{Digit}{2}:\\p{Digit}{2}) " + 
     "MEM-DEBUG ");

  /*----------------------------------------------------------------------------------------*/

  private static final Pattern sMemoryReport = 
    Pattern.compile(sHeader + "\\[FINE\\]: Memory Report:");
                                                           
  private static final Pattern sFreeMemory = 
    Pattern.compile("\\p{Blank}+Free = (\\p{Digit}+) .+");

  private static final Pattern sMinMemory = 
    Pattern.compile("\\p{Blank}+Minimum = (\\p{Digit}+) .+");

  private static final Pattern sMaxMemory = 
    Pattern.compile("\\p{Blank}+Maximum = (\\p{Digit}+) .+");

  /*----------------------------------------------------------------------------------------*/

  private static final Pattern sCheckedInReport = 
    Pattern.compile(sHeader + "\\[FINER\\]: Checked-In Node Versions:");

  private static final Pattern sWorkingReport = 
    Pattern.compile(sHeader + "\\[FINER\\]: Working Node Versions:");

  private static final Pattern sCheckSumsReport = 
    Pattern.compile(sHeader + "\\[FINER\\]: Working Node CheckSums:");

  private static final Pattern sAnnotationsReport = 
    Pattern.compile(sHeader + "\\[FINER\\]: Per-Node Annotations:");
                                                           
  private static final Pattern sCached = 
    Pattern.compile("\\p{Blank}+Cached = (\\p{Digit}+)");        
      
  private static final Pattern sMinMax = 
    Pattern.compile("\\p{Blank}+Min/Max = (\\p{Digit}+)/(\\p{Digit}+)");

  private static final Pattern sHitMiss = 
    Pattern.compile("\\p{Blank}+Hit/Miss = (\\p{Digit}+)/(\\p{Digit}+)");

}
