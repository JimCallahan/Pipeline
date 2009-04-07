// $Id: GenPropertyStateTest.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G E N   P R O P E R T Y   S T A T E   T E S T                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates the Java source code for the PropertyStateTest class.
 */ 
public 
class GenPropertyStateTest
  extends BaseGenStateTest
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new Java source code generator.
   */ 
  public
  GenPropertyStateTest() 
    throws ParseException 
  {
    super("PROPERTY STATE TEST", 
          "PropertyStateTest", "SimpleNodeStateTest", "PropertyState");
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   P A R S I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Parse the spreadsheet text file. 
   */ 
  public void 
  parseSpreadsheet
  (
   File file
  ) 
    throws ParseException
  {
    BooleanValue bval = new BooleanValue(); 
    
    EnumValue vval = null;
    {
      TreeSet<String> values = new TreeSet<String>();
      values.add("Pending");
      values.add("CheckedIn");
      values.add("NotLatest");
      values.add("Identical");

      vval = new EnumValue("VersionState", values);
    }

    EnumResult pres = null;
    {
      TreeSet<String> values = new TreeSet<String>();
      values.add("Pending");
      values.add("CheckedIn");
      values.add("Conflicted");
      values.add("Modified");
      values.add("BaseModified");
      values.add("NotLatest");
      values.add("Identical");

      pres = new EnumResult("PropertyState", values);
    }

    try {
      BufferedReader in = new BufferedReader(new FileReader(file)); 

      try {
        int row = 1;
        while(true) {
          String line = in.readLine(); 
          if(line == null) 
            break;
          
          String cells[] = line.split("\\t"); 
          if(cells.length != 5) 
            throw new ParseException
              ("An error occured while parsing the spreadsheet (" + file + ") at " + 
               "row (" + row + "): The row did not contain the required 5 columns!"); 
          
          BaseGenerator parent = pRootGenerator;
          
          CellPolicy policies[] = { vval, bval, bval, bval, pres }; 

          int hcol = 0;  /* human column index starting from 1 */ 
          try { 
            int col;
            for(col=0; col<cells.length; col++) {
              hcol = col+1; 

              String text = cells[col].trim();
              try {
                if(!text.equals("-")) {
                  Comparable key = policies[col].parseKey(text); 
                  
                  parent.validateChildColumn(hcol); 
                  BaseGenerator child = parent.getChild(key);
                  if(child == null) {
                    switch(col) {
                    case 0:
                      child = new EnumTestGenerator(key, vval, "pVersionState"); 
                      break;
                      
                    case 1:
                      child = new SimpleBooleanTestGenerator
                        (key, bval, "pWork.identicalProperties(pBase)"); 
                      break;
                      
                    case 2:
                      child = new SimpleBooleanTestGenerator
                        (key, bval, "pWork.identicalProperties(pLatest)"); 
                      break;
                      
                    case 3:
                      child = new SimpleBooleanTestGenerator
                        (key, bval, "pBase.identicalProperties(pLatest)"); 
                      break;
                      
                    case 4:
                      child = new EnumResultGenerator(key, pres); 
                    }
                    
                    parent.addChild(hcol, child);
                  }
                  
                  parent = child;
                }
              }
              catch(ParseException ex2) {
                throw new ParseException
                  ("The text of the current cell is (" + text + "):\n" + ex2.getMessage());
              }
            }
          }
          catch(ParseException ex) {
            throw new ParseException
              ("An error occured while parsing the spreadsheet (" + file + ") at " + 
               "row (" + row + ") at column (" + hcol + "):\n" + ex.getMessage());
          }

          row++;
        }
      }
      finally {
        in.close(); 
      }
    }
    catch(IOException ex) {
      throw new ParseException
        ("Unable to read the spreadsheet file (" + file + ")!", ex); 
    }
  }
  

}



