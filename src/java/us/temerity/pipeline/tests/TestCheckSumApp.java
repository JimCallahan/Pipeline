// $Id: TestCheckSumApp.java,v 1.1 2004/03/09 05:02:37 jim Exp $

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
    long size = 64*1024*1024;

    /* generate some test paths */ 
    File pathA = null;
    File pathB = null;
    File pathC = null;
    File pathD = null;
    File pathE = null;
    File pathF = null;
    {
      System.out.print("Generating: \n");

      pathA = new File("/working/jim/default/testA");
      genTestFile(pathA, 123, size);
      
      pathB = new File("/working/jim/default/testB");
      genTestFile(pathB, 456, size);
      
      pathC = new File("/working/jim/default/testC");
      genTestFile(pathC, 456, size);
      
      pathD = new File("/working/jim/default/testD");
      genTestFile(pathD, 123, size);
      
      pathE = new File("/working/jim/default/testE");
      genTestFile(pathE, 456, size);
      
      pathF = new File("/working/jim/default/testF");
      genTestFile(pathF, 456, size);
      
      System.out.print("\n");
    }

      
    /* tests */ 
    {
      refresh(pathA, Long.MAX_VALUE);
      refresh(pathB, Long.MAX_VALUE);
      refresh(pathC, Long.MAX_VALUE);

      System.out.print("---\n");

      refresh(pathD, 0);
      refresh(pathE, 0);
      refresh(pathF, 0);

      System.out.print("---\n");

      test(pathA, pathB, false);
      test(pathB, pathC, true);
      test(pathC, pathA, false);
    }

    System.exit(0);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
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
   
    System.out.print("Time = " + time + " msec\n");
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
    
    System.out.print("Comparing: \n" +
		     "  FileA: " + pathA + "\n" + 
		     "  FileB: " + pathB + "\n" + 
		     "  Equal: " + result + " (" + expect + ")\n\n");

    assert(expect == result);
  }


  private void 
  genTestFile
  (
   File path,    /* IN: path to file */ 
   long seed,    /* IN: random seed */ 
   long size     /* IN: size of file */ 
  ) 
    throws IOException
  {
    File file = new File(pDir, path.getPath());
    if(file.isFile()) 
      return;

    System.out.print("  " + file.toString() + "\n");

    file.getParentFile().mkdirs();
    FileWriter out = new FileWriter(file);
    
    Random rand = new Random(seed);
    long wk;
    for(wk=0; wk<size; wk++) 
      out.write(String.valueOf(rand.nextInt(9)), 0, 1);
    
    out.flush();
    out.close();
  }    



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private File      pDir;
  private CheckSum  pCheckSum;
}
