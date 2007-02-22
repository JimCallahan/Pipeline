// $Id: TestCheckSumApp.java,v 1.9 2007/02/22 16:14:02 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   C H E C K   S U M                                                      */
/*------------------------------------------------------------------------------------------*/

class TestCheckSumApp
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
    //LogMgr.getInstance().setLevel(LogMgr.Kind.Sub, LogMgr.Level.Finest);
    FileCleaner.init();

    try {
      File dir = new File(System.getProperty("user.dir") + "/data/prod");
      {
	File cdir = new File(dir, "checksum");
	cdir.mkdirs();
      }

      TestCheckSumApp app = new TestCheckSumApp(dir);
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }


  public
  TestCheckSumApp
  (
   File dir
  )
  {
    pDir = dir;
    pCheckSum = new CheckSum("MD5", dir);
  }


  public void 
  run() 
    throws PipelineException, IOException
  {
    /* generate some test paths */ 
    TreeMap<Long,ArrayList<File>> table = new TreeMap<Long,ArrayList<File>>();
    {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sum, LogMgr.Level.Info,
	 "-----------------------------------\n" + 
	 "Generating Data Files: \n");

      int wk;
      for(wk=0; wk<15; wk++) {
	ArrayList<File> paths = new ArrayList<File>();

        long blocks = 2 << wk;
	long size = 1024 * blocks; 
	
	long seeds[] = { 123, 123, 456, 789, 789, 012, 245, 64, 75, 763, 547 };
	int fk;
	for(fk=0; fk<seeds.length; fk++) {
	  File file = new File("/working/jim/default/test" + fk + "-" + size);
	  genTestFile(file, seeds[fk], blocks);
	  paths.add(file);
	}

	table.put(size, paths);
      }

      System.out.print("\n");
    }

      
    /* tests */ 
    {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sum, LogMgr.Level.Info,
	 "-----------------------------------\n" + 
	 "Rebuilding CheckSums (openssl): \n\n");

      for(Long size : table.keySet()) {
	ArrayList<File> paths = table.get(size);

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sum, LogMgr.Level.Info,
	     "  File Size: " + size + ":\n");
          
          long total = 0;
          for(File path : paths) 
            total += refreshSSL(path);

          double rate = ((((double) (size*paths.size())) / ((double) total)) * 
                         (6000.0 / (1024.0*1024.0)));
	  System.out.print("   TOTAL = " + total + " msec\n" + 
			   "    RATE = " + ((float) rate) + " MB/sec\n\n");
	}
      }
    }
    
    {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sum, LogMgr.Level.Info,
	 "-----------------------------------\n" + 
	 "Rebuilding CheckSums (java): \n\n");

      for(Long size : table.keySet()) {
	ArrayList<File> paths = table.get(size);

	{
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Sum, LogMgr.Level.Info,
	     "  File Size: " + size + ":\n");

          long total = 0;
          for(File path : paths) 
            total += refreshSSL(path);

          double rate = ((((double) (size*paths.size())) / ((double) total)) * 
                         (6000.0 / (1024.0*1024.0)));
	  System.out.print("   TOTAL = " + total + " msec\n" + 
			   "    RATE = " + ((float) rate) + " MB/sec\n\n");
	}
      }
    }



//     {
//       LogMgr.getInstance().log
// 	(LogMgr.Kind.Sum, LogMgr.Level.Info,
// 	 "-----------------------------------\n" + 
// 	 "Comparing with CheckSums: \n\n");

//       Date start = new Date();
//       for(Long size : table.keySet()) {
// 	ArrayList<File> paths = table.get(size);

// 	test(paths.get(0), paths.get(1), true);
// 	test(paths.get(1), paths.get(2), false);
// 	test(paths.get(2), paths.get(3), false);
// 	test(paths.get(3), paths.get(4), true);
// 	test(paths.get(4), paths.get(5), false);
//       }

//       long time = (new Date()).getTime() - start.getTime();
//       float rate = ((float) (table.keySet().size() * 5000)) / ((float) time);

//       LogMgr.getInstance().log
// 	(LogMgr.Kind.Sum, LogMgr.Level.Info,
// 	 "    Time = " + time + " msec\n" + 
// 	 "    Rate = " + rate + " files/sec\n\n");
//     }

    System.exit(0);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private long 
  refresh
  (
   File path
  ) 
    throws PipelineException
  {
    Date start = new Date();
    pCheckSum.refresh(path); 
    long time = (new Date()).getTime() - start.getTime();
   
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Info,
       "    File = " + path + "\n" +
       "    Time = " + time + " msec\n");

    return time;
  }


  private long 
  refreshSSL
  (
   File path
  ) 
    throws PipelineException
  {
    Date start = new Date();
    
    File in  = new File(pDir, path.toString());
    File out = pCheckSum.checkSumFile(path);

    ArrayList<String> args = new ArrayList<String>();
    args.add("dgst");
    args.add("-md5");
    args.add("-binary");
    args.add("-out");
    args.add(out.toString() + ".ssl");
    args.add(in.toString());
    
    SubProcessLight proc = 
      new SubProcessLight("CheckSum", "openssl", args, System.getenv(), pDir);
    try {
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
        LogMgr.getInstance().log
          (LogMgr.Kind.Sum, LogMgr.Level.Severe,
           proc.getStdErr());	
    }
    catch(InterruptedException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Sum, LogMgr.Level.Severe,
           "Interrupted."); 
    }

    long time = (new Date()).getTime() - start.getTime();
   
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Info,
       "    File = " + path + "\n" +
       "    Time = " + time + " msec\n");

    return time;
  }


  private void
  test
  (
   File pathA,     /* IN: first path */  
   File pathB,     /* IN: second path */ 
   boolean expect  /* IN: expected result */ 
  ) 
    throws PipelineException
  {
    boolean result = pCheckSum.compare(pathA, pathB);
    
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Info,
       "  Comparing: \n" +
       "    FileA: " + pathA + "\n" + 
       "    FileB: " + pathB + "\n" + 
       "    Equal: " + result + " (" + expect + ")\n\n");

    assert(expect == result);
  }


  private void 
  genTestFile
  (
   File path,    /* IN: path to file */ 
   long seed,    /* IN: random seed */ 
   long blocks   /* IN: number of 1024 byte blocks to write */ 
  ) 
    throws IOException
  {
    File file = new File(pDir, path.getPath());
    if(file.isFile()) 
      return;

    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Info,
       "  " + file.getName() + "\n");

    file.getParentFile().mkdirs();
    FileOutputStream out = new FileOutputStream(file);
    
    Random rand = new Random(seed);
    byte[] bytes = new byte[1024];

    int wk; 
    for(wk=0; wk<blocks; wk++) {
      rand.nextBytes(bytes);
      out.write(bytes);
    }
    
    out.flush();
    out.close();
  }    



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private File      pDir;
  private CheckSum  pCheckSum;
}
