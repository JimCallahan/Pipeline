// $Id: NodeTest.java,v 1.1 2004/03/11 14:12:53 jim Exp $

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Test class used by TestNodeModVersionApp                                               */
/*------------------------------------------------------------------------------------------*/

public 
class NodeTest
  implements Glueable 
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  NodeTest() 
  {
  }

  public 
  NodeTest
  (
    TreeMap<VersionID,NodeVersion> table
  ) 
  {
    pTable = table;
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
    ge.encode("Table", pTable);
  }

  public void 
  fromGlue
  (
   GlueDecoder gd  
  ) 
    throws GlueException
  {
    pTable = (TreeMap<VersionID,NodeVersion>) gd.decode("Table");
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TreeMap<VersionID,NodeVersion>  pTable;
}
