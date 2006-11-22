// $Id: LayoutGroup.java,v 1.6 2006/11/22 09:08:00 jim Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L A Y O U T   G R O U P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Hierarchical grouping of parameters and presets associated with plugins which determine 
 * the layout of UI components.
 */
public 
class LayoutGroup
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public 
  LayoutGroup() 
  {
    pEntries   = new LinkedList<String>();
    pSubGroups = new LinkedList<LayoutGroup>();
  }

  /**
   * Construct a new default layout group. <P> 
   * 
   * @param isOpen
   *   Whether the group is initially open.
   */
  public 
  LayoutGroup
  (
   boolean isOpen
  ) 
  {
    this("Root", "", isOpen); 
  }

  /**
   * Construct a new layout group.
   * 
   * @param name
   *   The name of the group.
   * 
   * @param desc 
   *   A short description of the layout group.
   * 
   * @param isOpen
   *   Whether the group is initially open.
   */ 
  public 
  LayoutGroup
  (
   String name,   
   String desc, 
   boolean isOpen
  ) 
  {
    super(name, desc);

    pIsOpen    = isOpen;
    pEntries   = new LinkedList<String>();
    pSubGroups = new LinkedList<LayoutGroup>();
  }

  /**
   * Construct a new layout group which is a copy of the given group but with a 
   * new name and description.
   * 
   * @param name
   *   The name of the group.
   * 
   * @param desc 
   *   A short description of the layout group.
   * 
   * @param group
   *   The layout group to copy.
   */ 
  public 
  LayoutGroup
  (
   String name,   
   String desc, 
   LayoutGroup group
  ) 
  {
    super(name, desc);

    pIsOpen    = group.pIsOpen;
    pEntries   = new LinkedList<String>(group.pEntries);
    pSubGroups = new LinkedList<LayoutGroup>(group.pSubGroups);
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
    StringBuilder buf = new StringBuilder();
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
   * parameter/preset fields.  Otherwise the group title will be shown but the paramter/preset
   * fields will be initially hidden.
   */ 
  public boolean
  isOpen() 
  {
    return pIsOpen;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the parameters and/or presets in the order they should be 
   * displayed in the user interface. <P> 
   * 
   * The return list may contain <CODE>null</CODE> values to indicate the positions of 
   * parameter separators.
   */ 
  public List<String>
  getEntries() 
  {
    return Collections.unmodifiableList(pEntries);
  }

  /**
   * Add the given parameter/preset name to the group. <P> 
   * 
   * @param name
   *   The name of the parameter.
   */ 
  public void 
  addEntry
  (
   String name
  ) 
  {
    pEntries.add(name);
  }

  /**
   * Add a parameter/preset separator to the group. <P>
   */ 
  public void 
  addSeparator() 
  {
    pEntries.add(null);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the parameter subgroups.
   */ 
  public List<LayoutGroup>
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
   LayoutGroup group
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

    if(!pEntries.isEmpty()) 
      encoder.encode("Entries", pEntries);

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
    
    LinkedList<String> names = (LinkedList<String>) decoder.decode("Entries");
    if(names != null) 
      pEntries = names;

    LinkedList<LayoutGroup> groups = (LinkedList<LayoutGroup>) decoder.decode("SubGroups");
    if(groups != null) 
      pSubGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6353586503001120251L;
      

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the member parameters.
   */ 
  private LinkedList<String>  pEntries; 

  /**
   * The layout subgroups.
   */ 
  private LinkedList<LayoutGroup>  pSubGroups; 

  /**
   * Whether the group is initially open.
   */ 
  private boolean  pIsOpen;

}
