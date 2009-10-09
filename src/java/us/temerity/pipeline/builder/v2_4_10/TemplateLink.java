// $Id: TemplateLink.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.builder.v2_4_10;

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
  public 
  TemplateLink
  (
    String targetName,
    Set<String> contexts,
    boolean ignorable
  )
  {
    pTargetName = targetName;
    pContexts = new TreeSet<String>();
    if (contexts != null)
      pContexts.addAll(contexts);
    pIgnorable = ignorable;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the target node.
   */
  public final String
  getTargetName()
  {
    return pTargetName;
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private String pTargetName;

  private TreeSet<String> pContexts;
  
  private boolean pIgnorable;
}
