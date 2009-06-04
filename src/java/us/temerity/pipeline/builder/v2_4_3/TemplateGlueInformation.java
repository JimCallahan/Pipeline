// $Id: TemplateGlueInformation.java,v 1.7 2009/06/04 09:26:58 jim Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E    I N F O R M A T I O N                                     */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateGlueInformation
  extends Described
  implements Glueable
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
  TemplateGlueInformation()
  {
    pReplacements = new ListSet<String>();
    pReplacementDefaults = new TreeMap<String, String>();
    pReplacementParamNames = new TreeMap<String, String>();
    
    pContexts = new MappedListSet<String, String>();
    pContextDefaults = new MappedArrayList<String, TreeMap<String,String>>();
    pContextParamNames = new DoubleMap<String, String, String>();
    
    pNodesInTemplate = new TreeSet<String>();
    
    pFrameRanges = new TreeSet<String>();
    pFrameRangeDefaults = new TreeMap<String, FrameRange>();
    
    pAOEModes = new TreeMap<String, ActionOnExistence>();
    
    pExternals = new ListSet<String>();
    pOptionalBranches = new ListMap<String, Boolean>();
  }
  
  public 
  TemplateGlueInformation
  (
    String name,
    String desc
  )
  {
    super(name, desc);
    
    pReplacements = new ListSet<String>();
    pReplacementDefaults = new TreeMap<String, String>();
    pReplacementParamNames = new TreeMap<String, String>();
    
    pContexts = new MappedListSet<String, String>();
    pContextDefaults = new MappedArrayList<String, TreeMap<String,String>>();
    pContextParamNames = new DoubleMap<String, String, String>();
    
    pNodesInTemplate = new TreeSet<String>();
    
    pFrameRanges = new TreeSet<String>();
    pFrameRangeDefaults = new TreeMap<String, FrameRange>();
    pAOEModes = new TreeMap<String, ActionOnExistence>();
    
    pExternals = new ListSet<String>();
    pOptionalBranches = new ListMap<String, Boolean>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a list of the String replacements for the template. 
   */
  public final ListSet<String> 
  getReplacements()
  {
    return new ListSet<String>(pReplacements);
  }
  
  /**
   * Set the list of of the String replacements for the template.
   */
  public final void 
  setReplacements
  (
    ListSet<String> replacements
  )
  {
    if (replacements == null)
      pReplacements = new ListSet<String>();
    else
      pReplacements = new ListSet<String>(replacements);
  }

  /**
   *  Get the list of default values for the String replacements. 
   */
  public final TreeMap<String, String> 
  getReplacementDefaults()
  {
    return new TreeMap<String, String>(pReplacementDefaults);
  }

  
  /**
   * Set the list of default values for the String replacements.
   * <p>
   * All values in the keyset must already exist in the list of replacements for
   * this template.
   */
  public final void 
  setReplacementDefaults
  (
    TreeMap<String, String> replacementDefaults
  )
  {
    if (replacementDefaults == null)
      pReplacementDefaults = new TreeMap<String, String>();
    else {
      for (String key : replacementDefaults.keySet()) {
        if (!pReplacements.contains(key))
          throw new IllegalArgumentException
            ("The replacement key (" + key + ") specified while setting the default " +
             "replacement values is not specified as valid key for this template"); 
      }
      pReplacementDefaults = new TreeMap<String, String>(replacementDefaults);
    }
  }
  
  /**
   *  Get the list of alternate param names for the replacements. 
   */
  public final TreeMap<String, String> 
  getReplacementParamNames()
  {
    return new TreeMap<String, String>(pReplacementParamNames);
  }
  
  /**
   * Set the list of alternate param names for the String replacements.
   * <p>
   * All values in the keyset must already exist in the list of replacements for
   * this template.
   */
  public final void 
  setReplacementParamNames
  (
    TreeMap<String, String> paramNames
  )
  {
    if (paramNames == null)
      pReplacementParamNames = new TreeMap<String, String>();
    else {
      for (String key : paramNames.keySet()) {
        if (!pReplacements.contains(key))
          throw new IllegalArgumentException
            ("The replacement key (" + key + ") specified while setting the alternate " +
             "param names is not specified as valid key for this template");
        String paramName = paramNames.get(key);
        if (!isValidName(paramName)) {
          throw new IllegalArgumentException
          ("The parameter name (" + paramName + ") specifed for the key ( " + key + ") is not " +
           "a valid parameter name.");
        }
      }
      pReplacementParamNames = new TreeMap<String, String>(paramNames);
    }
  }
  
  /**
   * Return a list of the contexts and the string replacements for each context. 
   */
  public final MappedListSet<String, String> 
  getContexts()
  {
    return new MappedListSet<String, String>(pContexts);
  }
  
  /**
   * Set the list of the contexts and the string replacements for each context. 
   */
  public final void 
  setContexts
  (
    MappedListSet<String, String> contexts
  )
  {
    if (contexts == null)
      pContexts = new MappedListSet<String, String>();
    else {
      pContexts = new MappedListSet<String, String>(contexts);
    }
  }
  
  /**
   * Get the default values for the String replacements in the template's contexts.
   */
  public final TreeMap<String, ArrayList<TreeMap<String, String>>> 
  getContextDefaults()
  {
    return new TreeMap<String, ArrayList<TreeMap<String,String>>>(pContextDefaults);
  }

  /**
   * Set the list of default values for the String replacements for each context.
   * <p>
   * All contexts and their values in the keyset must already exist in the list of
   * replacements for each context in this template.
   */
  public final void 
  setContextDefaults
  (
    MappedArrayList<String, TreeMap<String, String>> contextDefaults
  )
  {
    if (contextDefaults == null)
      pContextDefaults = new MappedArrayList<String, TreeMap<String, String>>();
    else {
      for (String context : contextDefaults.keySet()) {
        if (!pContexts.keySet().contains(context))
          throw new IllegalArgumentException
            ("The context (" + context + ") specified while setting the default context " +
             "values is not set as a valid context for this template");
        ListSet<String> cValues = pContexts.get(context);
        for (TreeMap<String, String> values : contextDefaults.get(context)) {
          for (String key : values.keySet()) {
            if (!cValues.contains(key))
              throw new IllegalArgumentException
                ("The replacement key (" + key + ") specified as a default value for the " +
                 "context (" + context + ") while setting the default context values is " +
                 "not a valid string replacement key.");
          }
        }
      }
      pContextDefaults = 
        new MappedArrayList<String, TreeMap<String, String>>(contextDefaults);
    }
  }
  
  public final DoubleMap<String, String, String>
  getContextParamNames()
  {
    return new DoubleMap<String, String, String>(pContextParamNames);
  }
  
  /**
   * Set the list of default values for the String replacements for each context.
   * <p>
   * All contexts and their values in the keyset must already exist in the list of
   * replacements for each context in this template.
   */
  public final void 
  setContextParamNames
  (
    DoubleMap<String, String, String> contextParamNames
  )
  {
    if (contextParamNames == null)
      pContextParamNames = new DoubleMap<String, String, String>(); 
    else {
      for (String context : contextParamNames.keySet()) {
        if (!pContexts.keySet().contains(context))
          throw new IllegalArgumentException
            ("The context (" + context + ") specified while setting the context param " +
             "names is not set as a valid context for this template");
        ListSet<String> cValues = pContexts.get(context);
        TreeMap<String, String> paramNames = contextParamNames.get(context); 
        for (String key : paramNames.keySet()) {
          if (!cValues.contains(key))
            throw new IllegalArgumentException
              ("The replacement key (" + key + ") specified having a param name for the " +
               "context (" + context + ") while setting the param names is " +
               "not a valid string replacement key.");
          String paramName = paramNames.get(key);
          if (!isValidName(paramName)) 
            throw new IllegalArgumentException
            ("The parameter name (" + paramName + ") specifed for the key ( " + key + ") in " +
             "the context (" + context + ") is not a valid parameter name.");
        }
      }
      pContextParamNames = 
        new DoubleMap<String, String, String>(contextParamNames);
    }
  }

  
  /**
   * Get a list of nodes in the template.
   */
  public final TreeSet<String> 
  getNodesInTemplate()
  {
    return new TreeSet<String>(pNodesInTemplate);
  }
  
  /**
   * Set the list of nodes to be built in the template.
   */
  public final void 
  setNodesInTemplate
  (
    TreeSet<String> nodesInTemplate
  )
  {
    if (nodesInTemplate == null)
      pNodesInTemplate = new TreeSet<String>();
    else
      pNodesInTemplate = new TreeSet<String>(nodesInTemplate);
  }
  
  /**
   * Get a list of frame ranges in the template.
   */
  public final TreeSet<String>
  getFrameRanges()
  {
    return new TreeSet<String>(pFrameRanges); 
  }
  
  /**
   * Set the list of frame ranges in the template.
   */
  public final void
  setFrameRanges
  (
    TreeSet<String> frameRanges  
  )
  {
    if (frameRanges == null)
      pFrameRanges = new TreeSet<String>();
    else
      pFrameRanges = new TreeSet<String>(frameRanges);
  }
  
  /**
   *  Get the list of default values for the frame ranges. 
   */
  public final TreeMap<String, FrameRange> 
  getFrameRangeDefaults()
  {
    return new TreeMap<String, FrameRange>(pFrameRangeDefaults);
  }

  
  /**
   * Set the list of default values for the frame ranges.
   * <p>
   * All values in the keyset must already exist in the list of frame ranges for
   * this template.
   */
  public final void 
  setFrameRangeDefaults
  (
    TreeMap<String, FrameRange> replacementDefaults
  )
  {
    if (replacementDefaults == null)
      pFrameRangeDefaults = new TreeMap<String, FrameRange>();
    else {
      for (String key : replacementDefaults.keySet()) {
        if (!pFrameRanges.contains(key))
          throw new IllegalArgumentException
            ("The key (" + key + ") specified while setting the default " +
             "frame range values is not specified as valid key for this template"); 
      }
      pFrameRangeDefaults = new TreeMap<String, FrameRange>(replacementDefaults);
    }
  }
  
  /**
   * Get the list of AOEModes and the default value for each one.
   */
  public final TreeMap<String, ActionOnExistence>
  getAOEModes()
  {
    return new TreeMap<String, ActionOnExistence>(pAOEModes);
  }

  /**
   * Set the AOE modes in the template.
   */
  public final void
  setAOEModes
  (
    TreeMap<String, ActionOnExistence> aoes
  )
  {
    if (aoes == null)
      pAOEModes = new TreeMap<String, ActionOnExistence>();
    else
      pAOEModes = new TreeMap<String, ActionOnExistence>(aoes);
  }

  /**
   * Get the list of optional branches and the default value for each one.
   */
  public final ListMap<String, Boolean>
  getOptionalBranches()
  {
    return new ListMap<String, Boolean>(pOptionalBranches);
  }

  /**
   * Set the optional branches in the template.
   */
  public final void
  setOptionalBranches
  (
    ListMap<String, Boolean> ob
  )
  {
    if (ob == null)
      pOptionalBranches = new ListMap<String, Boolean>();
    else
      pOptionalBranches = new ListMap<String, Boolean>(ob);
  }  
  
  /**
   * Set the list of the externals for the template.
   */
  public final void 
  setExternals
  (
    ListSet<String> externals
  )
  {
    if (externals == null)
      pExternals = new ListSet<String>();
    else
      pExternals = new ListSet<String>(externals);
  }
  
  public final ListSet<String>
  getExternals()
  {
    return new ListSet<String>(pExternals);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  public static boolean
  isValidName
  (
   String name
  ) 
  { 
    return Identifiers.hasAlphaNumericChars(name); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U A B L E                                                                        */
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
    super.fromGlue(decoder);
    
    {
      Object o = decoder.decode(aContextDefaults);
      if (o != null)
        pContextDefaults = 
          (MappedArrayList<String, TreeMap<String, String>>) o;
    }
    
    {
      Object o = decoder.decode(aContexts);
      if (o != null) {
        if (o instanceof MappedListSet)
          pContexts = (MappedListSet<String, String>) o;
        else if (o instanceof MappedSet) {
          pContexts = new MappedListSet<String, String>((MappedSet<String, String>) o);
        }
        else
          throw new GlueException("Unable to decode (Contexts)");
      }
    }
    
    {
      Object o = decoder.decode(aContextParamNames);
      if (o != null)
        pContextParamNames = (DoubleMap<String, String, String>) o;
    }
    
    {
      Object o = decoder.decode(aNodesInTemplate);
      if (o != null)
        pNodesInTemplate = (TreeSet<String>) o;
    }
    
    {
      Object o = decoder.decode(aReplacements);
      if (o != null) {
        if (o instanceof ListSet) {
          pReplacements = (ListSet<String>) o;
        }
        else if (o instanceof TreeSet){
          TreeSet<String> set = (TreeSet<String>) o;
          pReplacements = new ListSet<String>(set);
        }
        else
          throw new GlueException("Unable to decode (Replacements)");
      }
    }
    
    {
      Object o = decoder.decode(aReplacementDefaults);
      if (o != null)
        pReplacementDefaults = (TreeMap<String, String>) o;
    }
    
    {
      Object o = decoder.decode(aReplacementParamNames);
      if (o != null)
        pReplacementParamNames = (TreeMap<String, String>) o;
    }
    
    {
      Object o = decoder.decode(aFrameRanges);
      if (o != null)
        pFrameRanges = (TreeSet<String>) o;
    }
    
    {
      Object o = decoder.decode(aFrameRangeDefaults);
      if (o != null)
        pFrameRangeDefaults = (TreeMap<String, FrameRange>) o;
    }
    {
      Object o = decoder.decode(aAOEModes);
      if (o != null)
        pAOEModes = (TreeMap<String, ActionOnExistence>) o;
    }
    {
      Object o = decoder.decode(aExternals);
      if (o != null)
        pExternals = (ListSet<String>) o;
    }
    {
      Object o = decoder.decode(aOptionalBranches);
      if (o != null)
        pOptionalBranches = (ListMap<String, Boolean>) o;
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
    super.toGlue(encoder);
    
    if (!pContextDefaults.isEmpty())
      encoder.encode(aContextDefaults, pContextDefaults);
    if (!pContexts.isEmpty())
      encoder.encode(aContexts, pContexts);
    if (!pNodesInTemplate.isEmpty())
      encoder.encode(aNodesInTemplate, pNodesInTemplate);
    if (!pReplacements.isEmpty())
      encoder.encode(aReplacements, pReplacements);
    if (!pReplacementDefaults.isEmpty())
      encoder.encode(aReplacementDefaults, pReplacementDefaults);
    if (!pReplacementParamNames.isEmpty())
      encoder.encode(aReplacementParamNames, pReplacementParamNames);
    if (!pFrameRanges.isEmpty())
      encoder.encode(aFrameRanges, pFrameRanges);
    if (!pFrameRanges.isEmpty())
      encoder.encode(aFrameRanges, pFrameRanges);
    if (!pFrameRangeDefaults.isEmpty())
      encoder.encode(aFrameRangeDefaults, pFrameRangeDefaults);
    if (!pContextParamNames.isEmpty())
      encoder.encode(aContextParamNames, pContextParamNames);
    if (!pAOEModes.isEmpty())
      encoder.encode(aAOEModes, pAOEModes);
    if (!pExternals.isEmpty())
      encoder.encode(aExternals, pExternals);
    if (!pOptionalBranches.isEmpty())
      encoder.encode(aOptionalBranches, pOptionalBranches);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8707667177593745789L;

  public static final String aReplacements = "Replacements";
  public static final String aReplacementDefaults = "ReplacementsDefaults";
  public static final String aReplacementParamNames = "ReplacementParamNames";
  public static final String aContexts = "Contexts";
  public static final String aContextDefaults = "ContextDefaults";
  public static final String aContextParamNames = "ContextParamNames";
  public static final String aNodesInTemplate = "NodesInTemplate";
  public static final String aFrameRanges = "FrameRanges";
  public static final String aFrameRangeDefaults = "FrameRangeDefaults";
  public static final String aAOEModes = "AOEModes";
  public static final String aExternals = "Externals";
  public static final String aOptionalBranches = "OptionalBranches";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private ListSet<String> pReplacements;
  private TreeMap<String, String> pReplacementParamNames;
  private TreeMap<String, String> pReplacementDefaults;
  private MappedListSet<String, String> pContexts;
  private DoubleMap<String, String, String> pContextParamNames;
  private MappedArrayList<String, TreeMap<String, String>> pContextDefaults;
  
  private TreeSet<String> pFrameRanges;
  private TreeMap<String, FrameRange> pFrameRangeDefaults;
  
  private TreeMap<String, ActionOnExistence> pAOEModes;
  
  private ListMap<String, Boolean> pOptionalBranches;
  
  private ListSet<String> pExternals;
  
  private TreeSet<String> pNodesInTemplate;

}
