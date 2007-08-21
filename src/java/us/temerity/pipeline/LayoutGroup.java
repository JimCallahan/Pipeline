// $Id: LayoutGroup.java,v 1.8 2007/08/21 09:38:08 jesse Exp $

package us.temerity.pipeline;

import  us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L A Y O U T   G R O U P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Hierarchical grouping of parameters and presets associated with plugins which determine 
 * the layout of UI components.
 */
public 
class LayoutGroup
  extends AdvancedLayoutGroup
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
    super();
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
    super(name, desc, name, isOpen);
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
    super(name, desc, group);
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
   * Overriden to throw an Exception if you attempt to add a column. <P>
   * 
   * If multiple columns as needed, then an {@link AdvancedLayoutGroup} needs to
   * be used.
   * 
   * @throws IllegalStateException
   * @see us.temerity.pipeline.AdvancedLayoutGroup#addColumn(java.lang.String, boolean)
   */
  @Override
  public Integer addColumn
  (
    String colName, 
    boolean isOpen
  )
  {
    throw new IllegalStateException
      ("This method cannot be called on a LayoutGroup.\n" +
       "If multiple columns are needed, then an AdvancedLayoutGroup must be used.");
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
    return isOpen(1);
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
    return getEntries(1);
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
    addEntry(1, name);
  }
  
  /**
   * Adds the given parameter/preset name to the beginning of the group. <P> 
   * 
   * @param name
   *   The name of the parameter.
   */ 
  public void 
  prependEntry
  (
   String name
  ) 
  {
    prependEntry(1, name);
  }

  /**
   * Add a parameter/preset separator to the group. <P>
   */ 
  public void 
  addSeparator() 
  {
    addSeparator(1);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the parameter subgroups.
   */ 
  public List<LayoutGroup>
  getSubGroups() 
  {
    return Collections.unmodifiableList(getSubGroups(1));
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
    addSubGroup(1, group);
  }

  /**
   * Add the given parameter subgroup to the beginning of the group.
   * 
   * @param group
   *   The parameter subgroup.
   */ 
  public void 
  prependSubGroup
  (
   LayoutGroup group
  ) 
  {
    prependSubGroup(1, group);
  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6353586503001120251L;
      
}
