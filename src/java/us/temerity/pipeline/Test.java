
package us.temerity.pipeline;

import java.util.*;
import java.io.*;


public 
class Test
  implements Glueable 
{ 
  public 
  Test() 
  {
  }

  
  public 
  Test
  (
   String name
  ) 
  {
    pName = name;
    
    int tmp[][] = {
      { 1, 2, 4 }, 
      { 5, 8 }
    };
    pStuff = tmp;
    
//     pFoo = new ArrayList();
//     pFoo.add("asd");
//     pFoo.add(new Integer(123));
//     pFoo.add(null);
//     pFoo.add(pStuff);
//     pFoo.add(new Byte("12"));
//     pFoo.add(new Boolean(false));
//     pFoo.add(new Character('\n'));
//     pFoo.add(new Character('J'));
//     pFoo.add(new Float(3.1415));
//     pFoo.add(new Double(3.1415927));
//     pFoo.add(new Short("32"));
//     pFoo.add(new Long("1048576"));
    
//     pBar = new HashSet();
//     pBar.add("hi");
//     pBar.add("there"); 
//     pBar.add("buddy");
    
//     pPig = new TreeMap();
//     pPig.put(new Integer(3), "three");
//     pPig.put(new Integer(1), "one");
//     pPig.put(new Integer(4), "four");
//     pPig.put(new Integer(2), "two");
    
//     pNull = null;
  }
  
  public void 
  writeGlue
  ( 
   GlueEncoder ge   /* IN: the current GLUE encoder */ 
  ) 
   throws GlueError
  {
    ge.writeEntity("Name", pName);
    
    ge.writeEntity("Stuff", pStuff);
    
//     ge.writeEntity("Foo", pFoo);
//     ge.writeEntity("Bar", pBar);
//     ge.writeEntity("Pig", pPig);
    
//     ge.writeEntity("Null", pNull);
  }

  public void 
  readGlue
  (
   GlueDecoder gd  /* IN: the current GLUE decoder */ 
  ) 
    throws GlueError
  {
    pName  = (String) gd.readEntity("Name");
    
    pStuff = (int[][]) gd.readEntity("Stuff");      
  }
  

  protected String pName;

  protected int[][] pStuff;

//   protected ArrayList pFoo; 
//   protected Set       pBar;
//   protected TreeMap   pPig; 

//   protected String    pNull;
}
