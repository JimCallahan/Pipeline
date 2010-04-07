// $Id: CanOffline.java,v 1.1 2009/12/11 01:24:23 jim Exp $

import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   D U M M Y   S E R V I C E                                                              */
/*------------------------------------------------------------------------------------------*/

public class
DummyService
{
  public static void 
  main
  (
   String[] args  
  )
  {
    try {
      if(args.length != 1)
        usage(); 

      File logfile = new File(args[0]);
      FileWriter out = new FileWriter(logfile); 
      try {
        int cnt = 1;
        while(true) {
          out.write("DummyService: " + cnt + "\n"); 
          out.flush();

          Thread.sleep(15000);

          cnt++;
        }
      }
      finally {
        out.close();
      }
    }
    catch(Exception ex) {
      System.err.print("ERROR:" + ex.getMessage()); 
    }

    System.exit(0);
  }

  private static void 
  usage()
    throws IOException
  {
    throw new IOException
      ("usage: DummyService logfile\n" + 
       "\n" +
       "Just loops forever logging output to the given logfile."); 
  }
}
  
