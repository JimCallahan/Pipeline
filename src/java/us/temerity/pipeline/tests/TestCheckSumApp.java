// $Id: TestCheckSumApp.java,v 1.2 2004/03/09 06:24:07 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*;
import java.util.logging.*;

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
    /* init loggers */ 
    Logs.init();
    Logs.sum.setLevel(Level.INFO);

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
    pCheckSum = new CheckSum("MD5", dir, new File(dir, "checksum"));
  }


  public void 
  run() 
    throws PipelineException, IOException
  {
    /* generate some test paths */ 
    TreeMap<Integer,ArrayList<File>> table = new TreeMap<Integer,ArrayList<File>>();
    {
      System.out.print("-----------------------------------\n" + 
		       "Generating Data Files: \n");

      int wk;
      for(wk=4; wk<21; wk++) {
	ArrayList<File> paths = new ArrayList<File>();
	
	int size = 2 << wk;

	String names[] = { "testA", "testB", "testC", "testD", "testE", "testF" };
	long seeds[] = { 123, 123, 456, 789, 789, 012 };
	int fk;
	for(fk=0; fk<names.length; fk++) {
	  File file = new File("/working/jim/default/" + names[fk] + "-" + size);
	  genTestFile(file, seeds[fk], size);
	  paths.add(file);
	}

	table.put(size, paths);
      }

      System.out.print("\n");
    }

      
    /* tests */ 
    {
      System.out.print("-----------------------------------\n" + 
		       "Rebuilding CheckSums: \n\n");
      for(Integer size : table.keySet()) {
	ArrayList<File> paths = table.get(size);

	long mtotal = 0;
	{
	  System.out.print("  Memory Mapped [" + size + "]:\n");
	  mtotal += refresh(paths.get(0), 0);
	  mtotal += refresh(paths.get(1), 0);
	  mtotal += refresh(paths.get(2), 0);

	  double rate = (((double) size) / ((double) (mtotal))) * (3000.0 / (1024.0*1024.0));
	  System.out.print("   TOTAL = " + mtotal + " msec\n" + 
			   "    RATE = " + ((float) rate) + " MB/sec\n\n");
	}
	
	long stotal = 0;
	{
	  System.out.print("  Stream I/O [" + size + "]:\n");
	  stotal += refresh(paths.get(3), Long.MAX_VALUE);
	  stotal += refresh(paths.get(4), Long.MAX_VALUE);
	  stotal += refresh(paths.get(5), Long.MAX_VALUE);

	  double rate = (((double) size) / ((double) (stotal))) * (3000.0 / (1024.0*1024.0));
	  System.out.print("   TOTAL = " + stotal + " msec\n" + 
			   "    RATE = " + ((float) rate) + " MB/sec\n\n");
	}

	if(mtotal < stotal) {
	  float factor = ((float) stotal) / ((float) mtotal);
	  System.out.print("  Memory Mapped: " + factor + " (times faster)\n\n\n");
	}
	else {
	  float factor = ((float) mtotal) / ((float) stotal);
	  System.out.print("  Stream I/O: " + factor + " (times faster)\n\n\n");
	}
      }
    }

    {
      System.out.print("-----------------------------------\n" + 
		       "Comparing with CheckSums: \n\n");

      Date start = new Date();
      for(Integer size : table.keySet()) {
	ArrayList<File> paths = table.get(size);

	test(paths.get(0), paths.get(1), true);
	test(paths.get(1), paths.get(2), false);
	test(paths.get(2), paths.get(3), false);
	test(paths.get(3), paths.get(4), true);
	test(paths.get(4), paths.get(5), false);
      }

      long time = (new Date()).getTime() - start.getTime();
      float rate = ((float) (table.keySet().size() * 5000)) / ((float) time);
      System.out.print("    Time = " + time + " msec\n" + 
		       "    Rate = " + rate + " files/sec\n\n");
    }

    System.exit(0);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private long 
  refresh
  (
   File path, 
   long size
  ) 
    throws PipelineException
  {
    Date start = new Date();
    pCheckSum.refresh(path, size); 
    long time = (new Date()).getTime() - start.getTime();
   
    System.out.print("    Time = " + time + " msec\n");

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
    
    System.out.print("  Comparing: \n" +
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
   int size      /* IN: size of file */ 
  ) 
    throws IOException
  {
    File file = new File(pDir, path.getPath());
    if(file.isFile()) 
      return;

    System.out.print("  " + file.getName() + "\n");

    file.getParentFile().mkdirs();
    FileOutputStream out = new FileOutputStream(file);
    
    Random rand = new Random(seed);
    byte[] bytes = new byte[size];
    rand.nextBytes(bytes);

    out.write(bytes);
    
    out.flush();
    out.close();
  }    



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private File      pDir;
  private CheckSum  pCheckSum;
}
