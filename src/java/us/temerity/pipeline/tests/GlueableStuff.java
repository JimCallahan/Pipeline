// $Id: GlueableStuff.java,v 1.1 2004/02/15 16:17:44 jim Exp $

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Test class used by TestGlueApp                                                         */
/*------------------------------------------------------------------------------------------*/

public 
class GlueableStuff
  implements Glueable 
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  GlueableStuff() 
  {
  }

  public 
  GlueableStuff
  (
   String name
  ) 
  {
    pName = name;
    
    {
      int tmp[][] = {
	{ 1, 2, 4 }, 
	{ 5, 8 }
      };
      pStuff = tmp;
    }
   
    pDude = new GlueableDude("Joe Shmo", 13);

    pList = new ArrayList();
    pList.add("Hi there!\n" + 
	     "This text is on the next line...\tAfter a Tab.\n" + 
	     "How about a quote: \" ... ok?\n" + 
	     "Can I use a slash \\ like this?");
    pList.add(new Integer(123));
    pList.add(null);
    pList.add(pStuff);
    pList.add(new Byte("12"));
    pList.add(new Boolean(false));
    pList.add(new Character('\n'));
    pList.add(new Character('J'));
    pList.add(new Float(3.1415));
    pList.add(new Double(3.1415927));
    pList.add(new Short("32"));
    pList.add(new Long("1048576"));
    
    pSet = new HashSet();
    pSet.add("hi");
    pSet.add("there"); 
    pSet.add("buddy");
    
    pTable = new TreeMap();
    pTable.put(new Integer(3), "three");
    pTable.put(new Integer(1), "one");
    pTable.put(new Integer(4), "four");
    pTable.put(new Integer(2), "two");
    
//     pNull = null;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder ge   
  ) 
   throws GlueException
  {
    ge.encode("Name",  pName);
    ge.encode("Stuff", pStuff);
    ge.encode("Dude",  pDude);
    ge.encode("List",  pList);
    ge.encode("Set",   pSet);
    ge.encode("Table", pTable);
    
//     ge.encode("Null", pNull);
  }

  public void 
  fromGlue
  (
   GlueDecoder gd  
  ) 
    throws GlueException
  {
    pName  = (String)       gd.decode("Name");
    pStuff = (int[][])      gd.decode("Stuff");      
    pDude  = (GlueableDude) gd.decode("Dude");
    pList  = (ArrayList)    gd.decode("ArrayList");
    pSet   = (HashSet)      gd.decode("HashSet");
    pTable = (TreeMap)      gd.decode("Table");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String        pName;
  private int[][]       pStuff;
  private GlueableDude  pDude;
  private ArrayList     pList;
  private Set           pSet;
  private TreeMap       pTable;   

//   protected String    pNull;
}
