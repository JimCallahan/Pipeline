// $Id: TestSubProcessApp.java,v 1.10 2009/11/09 18:49:45 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   T A S K   T I M E R                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TestTaskTimerApp
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
      TestTaskTimerApp app = new TestTaskTimerApp();
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
    /*  */ 
    try {
      System.out.print("-----------------------------------\n");

      {
        TaskTimer timer = new TaskTimer("Wait(3s) Run(2s)"); 
        timer.acquire();
        pause(3000);
        timer.resume();
        pause(2000);
        timer.suspend();
        System.out.println(timer.toString());
      }

      {
        System.out.print("------------------------------\n");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        
        {  
          TaskTimer timer = new TaskTimer("Serialization"); 

          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());
          timer.acquire();
          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());

          pause(2000);

          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());
          timer.resume();
          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());

          pause(3000);

          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());
          timer.acquire();
          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());

          pause(5000);

          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());
          timer.suspend();
          out.writeObject(new TaskTimer(timer));  System.out.println(timer.toString());

          out.close();

          System.out.println("---\n");
        }

        {
          ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
          ObjectInputStream in = new ObjectInputStream(bin);

          while(true) {
            try {
              TaskTimer timer = (TaskTimer) in.readObject();
              System.out.println(timer.toString());
            }
            catch(EOFException ex) {
              break;
            }
          }
        }
      }

    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);      
    }
  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  pause
  (
   long msec
  ) 
  {
    try {
      Thread.sleep(msec);
    }
    catch(InterruptedException ex) {
      System.out.print("Interrupted while pausing!");
    }
  }


}
