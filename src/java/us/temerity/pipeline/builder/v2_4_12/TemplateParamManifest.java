package us.temerity.pipeline.builder.v2_4_12;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   P A R A M   M A N I F E S T                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A record of the parameters and values used to run a template builder. <p>
 * 
 * This can be combined with either a {@link TemplateGlueInformation} file or a 
 * {@link TemplateDescManifest} to fully instantiate a template.
 */
public 
class TemplateParamManifest
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new Template Param Manifest.
   */
  public
  TemplateParamManifest()
  {
    super();
    
    pReplacements = new TreeMap<String, String>();
    pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    pFrameRanges = new TreeMap<String, FrameRange>();
    pExternals = new TreeMap<String, TemplateExternalData>();
    pOptionalBranches = new TreeMap<String, Boolean>();
    pOffsets = new TreeMap<String, Integer>();
    pAOEModes = new TreeMap<String, ActionOnExistence>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of basic replacements.
   */
  public final TreeMap<String, String> 
  getReplacements()
  {
    return pReplacements;
  }
  
  /**
   * Set the map of basic replacements. <p>
   * 
   * @param replacements
   *   The map of replacements or <code>null</code> to clear the set.
   */
  public final void 
  setReplacements
  (
    TreeMap<String, String> replacements
  )
  {
    if (replacements == null)
      pReplacements = new TreeMap<String, String>();
    else
      pReplacements = replacements;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the contextual replacements.
   */
  public final TreeMap<String, ArrayList<TreeMap<String, String>>> 
  getContexts()
  {
    return pContexts;
  }
  
  /**
   * Set the contextual replacements.
   * 
   * @param contexts
   *   The contextual replacements or <code>null</code> to clear all existing replacements.
   */
  public final void 
  setContexts
  (
    TreeMap<String, ArrayList<TreeMap<String, String>>> contexts
  )
  {
    if (contexts == null)
      pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
    else
      pContexts = contexts;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the template frame range values.
   */
  public final TreeMap<String, FrameRange> 
  getFrameRanges()
  {
    return pFrameRanges;
  }
  
  /**
   * Set the template frame range values.
   * 
   * @param frameRanges
   *   The frame range values to set or <code>null</code> to clear all frame range values.
   */
  public final void 
  setFrameRanges
  (
    TreeMap<String, FrameRange> frameRanges
  )
  {
    if (frameRanges == null)
      pFrameRanges = new TreeMap<String, FrameRange>();
    else
      pFrameRanges = frameRanges;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the template external data.
   */
  public final TreeMap<String, TemplateExternalData> 
  getExternals()
  {
    return pExternals;
  }
  
  /**
   * Set the template external data.
   * 
   * @param externals
   *   The external data or <code>null</code> to clear all external data.
   */
  public final void 
  setExternals
  (
    TreeMap<String, TemplateExternalData> externals
  )
  {
    pExternals = externals;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the template optional branch options.
   */
  public final TreeMap<String, Boolean> 
  getOptionalBranches()
  {
    return pOptionalBranches;
  }
  
  /**
   * Set the template optional branch values.
   * 
   * @param optionalBranches 
   *   The optional branch values or <code>null</code> to clear all optional branches.
   */
  public final void 
  setOptionalBranches
  (
    TreeMap<String, Boolean> optionalBranches
  )
  {
    if (optionalBranches == null)
      pOptionalBranches = new TreeMap<String, Boolean>();
    else
      pOptionalBranches = optionalBranches;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the template offset values.
   */
  public final TreeMap<String, Integer> 
  getOffsets()
  {
    return pOffsets;
  }
  
  /**
   * Set the template offset values.
   * 
   * @param offsets
   *   The offsets or <code>null</code> to clear all offset values.
   */
  public final void 
  setOffsets
  (
    TreeMap<String, Integer> offsets
  )
  {
    pOffsets = offsets;
  }
  
/*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the AOE Mode values.
   */
  public final TreeMap<String, ActionOnExistence> 
  getAOEModes()
  {
    return pAOEModes;
  }
  
  /**
   * Set the AOE Mode values.
   * 
   * @param modes
   *   The AOE Modes or <code>null</code> to clear all AOE Mode values.
   */
  public final void 
  setAOEModes
  (
    TreeMap<String, ActionOnExistence> modes
  )
  {
    pAOEModes= modes;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G L U E A B L E                                                                       */
  /*----------------------------------------------------------------------------------------*/

  @SuppressWarnings({ "unchecked" })
  @Override
  public void 
  fromGlue
  (
    GlueDecoder decoder
  )
    throws GlueException
  {
    {
      Object o = decoder.decode(aReplacements);
      if (o != null)
        pReplacements = (TreeMap<String, String>) o;
    }
    {
      Object o = decoder.decode(aContexts);
      if (o != null)
        pContexts = (TreeMap<String, ArrayList<TreeMap<String, String>>>) o;
    }
    {
      Object o = decoder.decode(aFrameRanges);
      if (o != null)
        pFrameRanges = (TreeMap<String, FrameRange>) o;
    }
    {
      Object o = decoder.decode(aExternals);
      if (o != null)
        pExternals = (TreeMap<String, TemplateExternalData>) o;
    }
    {
      Object o = decoder.decode(aOptionaBranches);
      if (o != null)
        pOptionalBranches = (TreeMap<String, Boolean>) o;
    }
    {
      Object o = decoder.decode(aOffsets);
      if (o != null)
        pOffsets = (TreeMap<String, Integer>) o;
    }
    {
      Object o = decoder.decode(aAOEModes);
      if (o != null)
        pAOEModes= (TreeMap<String, ActionOnExistence>) o;
    }
  }
  
  @Override
  public void 
  toGlue
  (
    GlueEncoder encoder
  )
    throws GlueException
  {
    if (!pReplacements.isEmpty())
      encoder.encode(aReplacements, pReplacements);
    if (!pContexts.isEmpty())
      encoder.encode(aContexts, pContexts);
    if (!pFrameRanges.isEmpty())
      encoder.encode(aFrameRanges, pFrameRanges);
    if (!pExternals.isEmpty())
      encoder.encode(aExternals, pExternals);
    if (!pOptionalBranches.isEmpty())
      encoder.encode(aOptionaBranches, pOptionalBranches);
    if (!pOffsets.isEmpty())
      encoder.encode(aOffsets, pOffsets);
    if (!pAOEModes.isEmpty())
      encoder.encode(aAOEModes, pAOEModes);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final String aReplacements    = "Replacements";
  private static final String aContexts        = "Contexts";
  private static final String aFrameRanges     = "FrameRanges";
  private static final String aExternals       = "Externals";
  private static final String aOptionaBranches = "OptionalBranches";
  private static final String aOffsets         = "Offsets";
  private static final String aAOEModes        = "AOEModes";
  
 
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private TreeMap<String, String> pReplacements;
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  private TreeMap<String, FrameRange> pFrameRanges;
  private TreeMap<String, TemplateExternalData> pExternals;
  private TreeMap<String, Boolean> pOptionalBranches;
  private TreeMap<String, Integer> pOffsets;
  private TreeMap<String, ActionOnExistence> pAOEModes;
}
