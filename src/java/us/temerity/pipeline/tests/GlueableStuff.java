// $Id: GlueableStuff.java,v 1.3 2004/02/20 22:51:07 jim Exp $

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
    pOk = true;

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
    
    pSSet = new HashSet<String>();
    pSSet.add("Is this ok?");
    pSSet.add("How about this?");

    pColor = GlueableColor.red;

    pNumbers = new Float[5];
    pNumbers[0] = new Float(3.1415);
    pNumbers[1] = null;
    pNumbers[2] = new Float(123.456);
    pNumbers[3] = pNumbers[0];
    pNumbers[4] = new Float(666.666);

    pMixed = new Object[8];
    pMixed[0] = pName;
    pMixed[1] = new GlueableDude("John Doe", 55);
    pMixed[2] = pNumbers;
    pMixed[3] = pDude;
    pMixed[4] = new Boolean(true);
    
    {
      TreeSet<Integer> ts = new TreeSet<Integer>();
      ts.add(45);
      ts.add(13);
      ts.add(99);
      ts.add(-123);
      
      pMixed[6] = ts;
    }

    pMixed[7] = GlueableColor.green;

    {
      GlueableColor tmp[][] = {
	{
	  GlueableColor.yellow, 
	  GlueableColor.blue, 
	  GlueableColor.red 
	}, 
	{
	  GlueableColor.green, 
	  GlueableColor.red 
	}
      };
      pPalette = tmp;
    }
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
    ge.encode("Ok",      pOk);    
    ge.encode("Name",    pName);
    ge.encode("Stuff",   pStuff);
    ge.encode("Dude",    pDude);
    ge.encode("List",    pList);
    ge.encode("Set",     pSet);
    ge.encode("Table",   pTable);
    ge.encode("SSet",    pSSet);
    ge.encode("Color",   pColor);
    ge.encode("Numbers", pNumbers);
    ge.encode("Mixed",   pMixed);
    ge.encode("Palette", pPalette);
  }

  public void 
  fromGlue
  (
   GlueDecoder gd  
  ) 
    throws GlueException
  {
    pOk      = (Boolean)           gd.decode("Ok");
    pName    = (String)            gd.decode("Name");
    pStuff   = (int[][])           gd.decode("Stuff");      
    pDude    = (GlueableDude)      gd.decode("Dude");
    pList    = (ArrayList)         gd.decode("ArrayList");
    pSet     = (HashSet)           gd.decode("HashSet");
    pTable   = (TreeMap)           gd.decode("Table");
    pSSet    = (HashSet<String>)   gd.decode("SSet");
    pColor   = (GlueableColor)     gd.decode("Color");
    pNumbers = (Float[])           gd.decode("Numbers");
    pMixed   = (Object[])          gd.decode("Mixed");
    pPalette = (GlueableColor[][]) gd.decode("Palette");
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private Boolean            pOk;
  private String             pName;
  private int[][]            pStuff;
  private GlueableDude       pDude;
  private ArrayList          pList;
  private Set                pSet;
  private TreeMap            pTable;   
  private Set<String>        pSSet;
  private GlueableColor      pColor;
  private Float[]            pNumbers;
  private Object[]           pMixed; 
  private GlueableColor[][]  pPalette;
}
