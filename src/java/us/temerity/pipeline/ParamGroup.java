// $Id: ParamGroup.java,v 1.1 2004/11/18 09:16:58 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A R A M   G R O U P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Hierarchical grouping of the parameters associated with plugins used to specify the 
 * layout of UI components.
 */
public 
class ParamGroup
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an unnamed open group. 
   */ 
  public 
  ParamGroup() 
  {
    pIsOpen     = true;
    pParamNames = new LinkedList<String>();
    pSubGroups  = new LinkedList<ParamGroup>();
  }

  /**
   * Construct a new parameter group.
   * 
   * @param name
   *   The name of the group.
   * 
   * @param isOpen
   *   Whether the group is initially open.
   */ 
  public 
  ParamGroup
  (
   String name,  
   boolean isOpen
  ) 
  {
    super(name);
    pIsOpen     = isOpen;
    pParamNames = new LinkedList<String>();
    pSubGroups  = new LinkedList<ParamGroup>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets a modified form of the name of the group with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI as the title of grouping components.
   * 
   * @see #getName
   */ 
  public String
  getNameUI()
  {
    StringBuffer buf = new StringBuffer();
    char c[] = getName().toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
  }

  /**
   * Whether the group is initially open. <P> 
   * 
   * If the group is open, the UI components will be created in a state which exposes the 
   * parameter fields.  Otherwise the group title will be shown but the paramter fields 
   * hidden initially.
   */ 
  public boolean
  isOpen() 
  {
    return pIsOpen;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the parameters. <P> 
   * 
   * The return list may contain <CODE>null</CODE> values to indicate the positions of 
   * parameter separators.
   */ 
  public List<String>
  getParamNames() 
  {
    return Collections.unmodifiableList(pParamNames);
  }

  /**
   * Add the given parameter name to the group. <P> 
   * 
   * @param name
   *   The name of the parameter.
   */ 
  public void 
  addParamName
  (
   String name
  ) 
  {
    pParamNames.add(name);
  }

  /**
   * Add a parameter separator to the group. <P>
   */ 
  public void 
  addSeparator() 
  {
    pParamNames.add(null);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the parameter subgroups.
   */ 
  public List<ParamGroup>
  getSubGroups() 
  {
    return Collections.unmodifiableList(pSubGroups);
  }

  /**
   * Add the given parameter subgroup to the group.
   * 
   * @param group
   *   The parameter subgroup.
   */ 
  public void 
  addSubGroup
  (
   ParamGroup group
  ) 
  {
    pSubGroups.add(group);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    if(!pParamNames.isEmpty()) 
      encoder.encode("ParamNames", pParamNames);

    if(!pSubGroups.isEmpty())     
      encoder.encode("SubGroups", pSubGroups);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    LinkedList<String> names = (LinkedList<String>) decoder.decode("ParamNames");
    if(names != null) 
      pParamNames = names;

    LinkedList<ParamGroup> groups = (LinkedList<ParamGroup>) decoder.decode("SubGroups");
    if(groups != null) 
      pSubGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7509392510507154760L;
      

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the member parameters.
   */ 
  private LinkedList<String>  pParamNames; 

  /**
   * The parameters subgroups.
   */ 
  private LinkedList<ParamGroup>  pSubGroups; 

  /**
   * Whether the group is initially open.
   */ 
  private boolean  pIsOpen;

}
