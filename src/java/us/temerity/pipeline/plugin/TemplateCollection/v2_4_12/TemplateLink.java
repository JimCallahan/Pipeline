// $Id: TemplateLink.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCollection.v2_4_12;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   L I N K                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A link between two nodes in a template.
 */
public 
class TemplateLink
{
  /**
   * Constructor.
   * 
   * @param targetName
   *   The name of source node in the link.
   *   
   * @param contexts
   *   The set of contexts that apply to this links.
   *   
   * @param ignorable
   *   Whether the link is ignorable.
   *   
   * @param offset
   *   The name of the offset assigned to this link or <code>null</code> if there is no 
   *   offset.
   */
  public 
  TemplateLink
  (
    String targetName,
    Set<String> contexts,
    boolean ignorable,
    String offset
  )
  {
    pSourceName = targetName;
    pContexts = new TreeSet<String>();
    if (contexts != null)
      pContexts.addAll(contexts);
    pIgnorable = ignorable;
    pOffset = offset;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the source node.
   */
  public final String
  getSourceName()
  {
    return pSourceName;
  }
  
  /**
   * Does this link have contexts assigned to it.
   */
  public final boolean
  hasContexts()
  {
    return !pContexts.isEmpty();
  }

  /**
   * Get the contexts assigned to the link.
   */
  public final Set<String> 
  getContexts()
  {
    return Collections.unmodifiableSet(pContexts);
  }

  /**
   * Is this link ignorable from the perspective of the downstream node.
   */
  public final boolean 
  isIgnorable()
  {
    return pIgnorable;
  }
  
  /**
   * Get the name of the offset associated with this link.
   */
  public final String
  getOffset()
  {
    return pOffset;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private String pSourceName;

  private TreeSet<String> pContexts;
  
  private boolean pIgnorable;
  
  private String pOffset;
}
