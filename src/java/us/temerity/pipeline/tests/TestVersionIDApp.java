// $Id: TestVersionIDApp.java,v 1.3 2004/03/07 02:47:29 jim Exp $

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
      VersionID vid = new VersionID();
      System.out.print("First Version: " + vid + "\n");
      
      System.out.print("-----------------------------------\n" + 
		       "Copy...\n");

      TreeSet<VersionID> IDs = new TreeSet<VersionID>();

      VersionID vid2 = new VersionID(vid);
      System.out.print("  " + vid + " == " + vid2 + "\n");
      
      {
	VersionID v = new VersionID(vid2, VersionID.Level.Micro);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Micro);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }
      
      {
	VersionID v = new VersionID(vid, VersionID.Level.Minor);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Micro);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Major);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Major);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Micro);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Minor);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Mega);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Mega);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Minor);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Minor);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Micro);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid2, VersionID.Level.Mega);
	IDs.add(v);
	vid2 = v;
	compareIDs(vid, vid2);
      }

      {
	VersionID v = new VersionID(vid, VersionID.Level.Mega);
	IDs.add(v);
	vid = v;
	compareIDs(vid, vid2);
      }


      System.out.print("-----------------------------------\n" + 
		       "Ordered Set:\n");
      for(VersionID v : IDs) 
	System.out.print("  " + v + "\n");      
    }

    {    
      System.out.print("-----------------------------------\n" + 
		       "Bad String Reps...\n");
      tryVersionID("2.1.2");
      tryVersionID("2.1.2.6.7");
      tryVersionID("0.1.2.3");
      tryVersionID(null);
      tryVersionID("5");
      tryVersionID("1.2.b.5");
      tryVersionID("");
      tryVersionID("4.1..2");
      tryVersionID("asd");
      tryVersionID("5.-6.14.9");
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
