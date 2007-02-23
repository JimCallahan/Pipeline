/**
 * 
 */
package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   P A S S   L A Y O U T   G R O U P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of {@link AdvancedLayoutGroup}s and {@link LayoutGroup}s each one
 * representing a different pass of GUI display in a Builder.
 * <P>
 * Builders may wish to have multiple GUI displays, sometimes for clarity's sake and
 * sometimes 
 * 
 * @author jesse clemens
 *
 */
public 
class PassLayoutGroup
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
  PassLayoutGroup() 
  {
    pEntries   = new TreeMap<Integer, AdvancedLayoutGroup>();
    pPassNames = new TreeMap<Integer, String>();
  }

  /**
   * Construct a new default layout group, setting the first pass. <P> 
   * 
   * @param passName
   * 	The name of the first pass to be added. 
   * @param layout
   *   The layout that will be used as the first pass
   */
  public 
  PassLayoutGroup
  (
    String passName, 
    AdvancedLayoutGroup layout
  ) 
  {
    this("Root", "", passName, layout); 
  }
  
  /**
   * Construct a new pass layout group, containing no layouts.
   * 
   * @param name
   *   The name of the group.
   * @param desc 
   *   A short description of the layout group.
   */ 
  public 
  PassLayoutGroup
  (
   String name,   
   String desc
  )
  {
    super(name, desc);
    
    pEntries   = new TreeMap<Integer, AdvancedLayoutGroup>();
    pPassNames = new TreeMap<Integer, String>();    
  }
  
  /**
   * Construct a new layout group, setting up the first pass.
   * 
   * @param name
   *   The name of the group.
   * @param desc 
   *   A short description of the layout group.
   * @param passName
   * 	The name of the first pass to be added. 
   * @param layout
   * 	The layout that will be used as the first pass 
   */ 
  public 
  PassLayoutGroup
  (
   String name,   
   String desc,
   String passName, 
   AdvancedLayoutGroup layout
  ) 
  {
    super(name, desc);
    
    pEntries   = new TreeMap<Integer, AdvancedLayoutGroup>();
    pPassNames = new TreeMap<Integer, String>();
    
    pEntries.put(1, layout);
    pPassNames.put(1, passName);
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
  PassLayoutGroup
  (
   String name,   
   String desc, 
   PassLayoutGroup group
  ) 
  {
    super(name, desc);

    pEntries = new TreeMap<Integer, AdvancedLayoutGroup>(group.pEntries);
    pPassNames = new TreeMap<Integer, String>(group.pPassNames);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * @param passName
   * 	The name of the pass to add
   * @param layout
   * 	The layout group to add
   * @return
   * 	the number of the pass that was added
   */
  public int 
  addPass
  (
    String passName,
    AdvancedLayoutGroup layout
  )
  {
    Integer newPass = pEntries.lastKey() + 1;
    pPassNames.put(newPass, passName);
    pEntries.put(newPass, layout);
    
    return newPass; 
  }
  
  /**
   * Does a particular pass exist in this layout.
   * 
   * @param pass
   * 	The number of the pass.
   * @return
   * 	Pass existance.
   */
  public boolean 
  hasPass
  (
    int pass
  )
  {
   return (pass <= getNumberOfPasses()); 
  }
  
  /**
   * @return a map of all the entries in all the passes
   */
  public SortedMap<Integer, AdvancedLayoutGroup> getAllLayouts()
  {
    return Collections.unmodifiableSortedMap(pEntries);
  }
  
  /**
   * @return a map of all the entries in all the passes
   */
  public SortedMap<Integer, String> getAllPassNames()
  {
    return Collections.unmodifiableSortedMap(pPassNames);
  }
  
  /**
   * Gets the layout group associated with a pass
   * 
   * @param pass
   * 	The pass
   * @return
   * 	The layout group
   */
  public AdvancedLayoutGroup getPassLayout
  (
    Integer pass
  )
  {
    validatePass(pass);
    return pEntries.get(pass); 
  }

  /**
   * Gets the pass name associated with a pass
   * 
   * @param pass
   * 	The pass
   * @return
   * 	The layout group
   */
  public String getPassName
  (
    Integer pass
  )
  {
    validatePass(pass);
    return pPassNames.get(pass); 
  }
  
  /**
   * Replaces an existing layout with a different one.
   * 
   * @param pass
   * 	The pass
   * @param layout
   * 	The layout group
   */

  public void setPassLayout
  (
    Integer pass,
    AdvancedLayoutGroup layout
   )
  {
    validatePass(pass);
    pEntries.put(pass, layout);
  }
  
  /**
   * @return the number of passes that have been initialized.
   */
  public int 
  getNumberOfPasses()
  {
    return pEntries.size();
  }

  /**
   * Returns the number of columns in a pass.
   * @param pass
   * 	The number of the pass.
   */
  public int getNumberOfColumns
  (
    int pass
  )
  {
    return pEntries.get(pass).getNumberOfColumns();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Utility method to generate an exception when access to a non-existant column
   * is attempted.
   * 
   * @param col
   * 	Which pass was the access attempted for.
   * @return
   * 	
   */
  private void validatePass(Integer pass)
  {
    if (pEntries.get(pass) == null)
      throw new IllegalArgumentException
        ("There is no Layout associated with pass ("+pass+")" +
         " in the AdvancedLayoutGroup ("+getName()+")");
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
    
    if(!pPassNames.isEmpty())
      encoder.encode("PassNames", pPassNames);
  }

  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    TreeMap<Integer, AdvancedLayoutGroup> names = (TreeMap<Integer, AdvancedLayoutGroup>) decoder.decode("Entries");
    if(names != null) 
      pEntries = names;
    
    TreeMap<Integer, String> passNames = (TreeMap<Integer, String>) decoder.decode("PassNames");
    if(passNames != null)
      pPassNames = passNames;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3049726571215559521L;  
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The layouts that make up each pass.
   */ 
  private TreeMap<Integer, AdvancedLayoutGroup> pEntries;
  
  /**
   * The name of each pass
   */
  private TreeMap<Integer, String> pPassNames;
}
