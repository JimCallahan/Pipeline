// $Id: TestFileSeqApp.java,v 1.1 2004/02/14 18:49:17 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   F I L E   S E Q                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestFileSeqApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      TestFileSeqApp app = new TestFileSeqApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  

 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TestFileSeqApp() 
  {
    pRandom = new Random((new Date()).getTime());
  }

  public void 
  run() 
  {
    try {
      String str = null;
      FileSeq fseq = new FileSeq(new FilePattern(str), null);
      test(fseq);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    {
      FileSeq fseq = new FileSeq("dog", "rgb");
      test(fseq);
    }

    {
      FileSeq fseq = new FileSeq(new FilePattern("cat"), null);
      test(fseq);
    }
    
    {
      FileSeq fseq = new FileSeq(new FilePattern("fly", -1, "txt"), null);
      test(fseq);
    }
    
    try {
      FileSeq fseq = new FileSeq(new FilePattern("bird"), new FrameRange(1, 10, 3));
      test(fseq);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    {
      FileSeq fseq = new FileSeq(new FilePattern("duck", "tif"), null);
      test(fseq);
    }
  
    try {
      FileSeq fseq = new FileSeq(new FilePattern("frog", 0, "gif"), null);
      test(fseq);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    {
      FileSeq fseq = new FileSeq(new FilePattern("salamander", 0, "gif"), 
				 new FrameRange(10, 123, 14));
      test(fseq);
    }
    
    {
      FileSeq fseq = new FileSeq(new FilePattern("lizard", 4, "yuv"), 
				 new FrameRange(12));
      test(fseq);
    }
    
    try {
      FileSeq fseq = new FileSeq(new FilePattern("toad", 4, ""), 
				 new FrameRange(1, 100, 25));
      test(fseq);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    {
      FileSeq fseq = new FileSeq(new FilePattern("heron", 6, "rgb"), 
				 new FrameRange(1, 100, 25));
      test(fseq);
    }

    try {
      FrameRange range = new FrameRange(-3);
      test(range);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }
    
    try {
      FrameRange range = new FrameRange(5);
      test(range);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }
    
    try {
      FrameRange range = new FrameRange(12, 5, 1);
      test(range);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }
    
    try {
      FrameRange range = new FrameRange(-5, 5, 1);
      test(range);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }

    try {
      FrameRange range = new FrameRange(2, 20, 0);
      test(range);
    }
    catch(IllegalArgumentException ex) {
      System.out.print("Caught: " + ex.getMessage() + "\n\n");
    }
  }


  public void 
  test
  (
   FileSeq fseq 
  )
  {
    System.out.print("-----------------------------------\n" +
		     "FileSeq: " + fseq + "\n");
    int wk;
    for(wk=0; wk<fseq.numFrames(); wk++) 
      System.out.print("  " + fseq.getFile(wk) + "\n");

    System.out.print("  ---\n");

    for(File file : fseq.getFiles()) 
      System.out.print("  " + file + "\n");

    System.out.print("  ---\n");

    try {
      GlueEncoder ge = new GlueEncoder("FileSeq", fseq);
      System.out.print(ge.getText());

      GlueDecoder gd = new GlueDecoder(ge.getText());
      FileSeq fseq2 = (FileSeq) gd.getObject();
      
      System.out.print("FileSeq (Glue): " + fseq2 + "\n");
      assert(fseq.equals(fseq2));
      assert(fseq.hashCode() == fseq2.hashCode());
    }
    catch(GlueException ex) {
      System.out.print(ex + "\n");
    }

    {
      FileSeq fseq2 = new FileSeq(fseq);
      assert(fseq.equals(fseq2));
      assert(fseq.hashCode() == fseq2.hashCode());      
    }

    {
      int idx = pRandom.nextInt(fseq.numFrames());
      FileSeq fseq2 = new FileSeq(fseq, idx);
      System.out.print("Extracted [" + idx + "]: " + fseq2 + "\n");
      assert(fseq2.isSingle());
      assert(fseq.hasFrameNumbers() == fseq2.hasFrameNumbers());
    }

    System.out.print("\n");
  } 


  public void 
  test
  (
   FrameRange range 
  )
  {
    System.out.print("FrameRange: " + range + "\n\n");
  } 


  private Random pRandom; 
}
