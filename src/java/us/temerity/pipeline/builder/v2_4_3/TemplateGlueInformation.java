// $Id: TemplateGlueInformation.java,v 1.1 2008/10/17 03:36:46 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
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
    pReplacements = new TreeSet<String>();
    pReplacementDefaults = new TreeMap<String, String>();
    
    pContexts = new MappedSet<String, String>();
    pContextDefaults = new MappedArrayList<String, TreeMap<String,String>>();
    
    pNodesInTemplate = new TreeSet<String>();
  }
  
  public 
  TemplateGlueInformation
  (
    String name,
    String desc
  )
  {
    super(name, desc);
    
    pReplacements = new TreeSet<String>();
    pReplacementDefaults = new TreeMap<String, String>();
    
    pContexts = new MappedSet<String, String>();
    pContextDefaults = new MappedArrayList<String, TreeMap<String,String>>();
    
    pNodesInTemplate = new TreeSet<String>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return a list of the String replacements for the template. 
   */
  public final TreeSet<String> 
  getReplacements()
  {
    return new TreeSet<String>(pReplacements);
  }
  
  /**
   * Set the list of of the String replacements for the template.
   */
  public final void 
  setReplacements
  (
    TreeSet<String> replacements
  )
  {
    if (replacements == null)
      pReplacements = new TreeSet<String>();
    else
      pReplacements = new TreeSet<String>(replacements);
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
   * Return a list of the contexts and the string replacements for each context. 
   */
  public final TreeMap<String, TreeSet<String>> 
  getContexts()
  {
    return new TreeMap<String, TreeSet<String>>(pContexts);
  }
  
  /**
   * Set the list of the contexts and the string replacements for each context. 
   */
  public final void 
  setContexts
  (
    MappedSet<String, String> contexts
  )
  {
    if (contexts == null)
      pContexts = new MappedSet<String, String>();
    else {
      pContexts = new MappedSet<String, String>(contexts);
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
      for (String context : pContextDefaults.keySet()) {
        if (!pContexts.keySet().contains(context))
          throw new IllegalArgumentException
            ("The context (" + context + ") specified while setting the default context " +
             "values is not set as a valid context for this template");
        TreeSet<String> cValues = pContexts.get(context);
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
      if (o != null)
        pContexts = (MappedSet<String, String>) o;
    }
    
    {
      Object o = decoder.decode(aNodesInTemplate);
      if (o != null)
        pNodesInTemplate = (TreeSet<String>) o;
    }
    
    {
      Object o = decoder.decode(aReplacements);
      if (o != null)
        pReplacements = (TreeSet<String>) o;
    }
    
    {
      Object o = decoder.decode(aReplacementDefaults);
      if (o != null)
        pReplacementDefaults = (TreeMap<String, String>) o;
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
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8707667177593745789L;

  
  public static final String aReplacements = "Replacements";
  public static final String aReplacementDefaults = "ReplacementsDefaults";
  public static final String aContexts = "Contexts";
  public static final String aContextDefaults = "ContextDefaults";
  public static final String aNodesInTemplate = "NodesInTemplate";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TreeSet<String> pReplacements;
  private TreeMap<String, String> pReplacementDefaults;
  private MappedSet<String, String> pContexts;
  private MappedArrayList<String, TreeMap<String, String>> pContextDefaults;
  
  
  private TreeSet<String> pNodesInTemplate;

}
