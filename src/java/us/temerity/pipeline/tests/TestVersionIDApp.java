// $Id: TestVersionIDApp.java,v 1.1 2004/02/28 19:59:47 jim Exp $

import us.temerity.pipeline.*;

import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   V E R S I O N   I D                                                    */
/*------------------------------------------------------------------------------------------*/

class TestVersionIDApp
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
      TestVersionIDApp app = new TestVersionIDApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
  {
    {
      VersionID vid = new VersionID("1");
      System.out.print("First Version: " + vid + "\n");
      
      incMinorID(vid);
      addMinorID(vid);
      addMinorID(vid);
      incMinorID(vid);
      incMinorID(vid);
      incMajorID(vid);
    
      System.out.print("-----------------------------------\n" + 
		       "Copy...\n");

      TreeSet<VersionID> IDs = new TreeSet<VersionID>();

      VersionID vid2 = new VersionID(vid);
      System.out.print("  " + vid + " == " + vid2 + "\n");
      
      vid2.incMinor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid.incMinor();
      IDs.add(new VersionID(vid));
      compareIDs(vid, vid2);
      
      vid2.addMinor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid.addMinor();
      IDs.add(new VersionID(vid));
      compareIDs(vid, vid2);
      
      vid.incMinor();
      IDs.add(new VersionID(vid));
      compareIDs(vid, vid2);
      
      vid2.addMinor();
      IDs.add(new VersionID(vid2));
      vid2.addMinor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      
      vid2.trivial();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.trivial();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.minor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.trivial();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.major();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.trivial();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.minor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.massive();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.minor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.minor();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);
      
      vid2.trivial();
      IDs.add(new VersionID(vid2));
      compareIDs(vid, vid2);

      System.out.print("-----------------------------------\n" + 
		       "Ordered Set:\n");
      for(VersionID v : IDs) 
	System.out.print("  " + v + "\n");      
    }

    {    
      System.out.print("-----------------------------------\n" + 
		       "Bad String Reps...\n");
      tryVersionID("0.1");
      tryVersionID(null);
      tryVersionID("5");
      tryVersionID("1.2.b.5");
      tryVersionID("");
      tryVersionID("1..2");
      tryVersionID("asd");
      tryVersionID("3.2.1");
      tryVersionID("5.-6.14");
    }
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  protected void 
  tryVersionID
  (
   String str
  ) 
  {
    VersionID vid;

    try {
      vid = new VersionID(str);
    } 
    catch (IllegalArgumentException e) {
      System.out.print("  " + e + "\n");
      return;
    }

    System.out.print("  Good VersionID: " + vid + "\n");
  }


  protected void 
  incMinorID
  (
   VersionID vid
  ) 
  {
    System.out.print("-----------------------------------\n" + 
		     "Test incMinor()...\n");
    vid.incMinor();
    testConvert(vid);
  }


  protected void 
  addMinorID
  (
   VersionID vid
  ) 
  {
    System.out.print("-----------------------------------\n" + 
		     "Test addMinor()...\n");
    vid.addMinor();
    testConvert(vid);
  }


  protected void 
  incMajorID
  (
   VersionID vid
  ) 
  {
    System.out.print("-----------------------------------\n" + 
		     "Test incMajor()...\n");
    vid.incMajor();
    testConvert(vid);
  }


  protected void
  compareIDs
  (
   VersionID a, 
   VersionID b
  ) 
  {
    System.out.print("-----------------------------------\n" + 
		     "Compare...\n");
    System.out.print("  " + a.toString() + " is ");
    if(a.compareTo((Object) b) < 0)
      System.out.print("less-than");
    else if(a.compareTo((Object) b) > 0)
      System.out.print("greater-than");
    else 
      System.out.print("equal-to");
    System.out.print(" " + b.toString() + "\n");
  }

  
  private void 
  testConvert
  ( 
   VersionID vid
  ) 
  {
    String str = vid.toString();
    System.out.print("BEFORE: " + str + "\n");
    
    VersionID vid2 = new VersionID(str);
    System.out.print(" AFTER: " + vid2 + "\n");
    
    assert(str.equals(vid2.toString()));
    assert(vid.equals(vid2));
    assert(vid.compareTo(vid2) == 0);
  }
}
