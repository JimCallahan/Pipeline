// $Id: TemplateInfoBuilder.java,v 1.11 2009/05/07 03:25:29 jesse Exp $

package us.temerity.pipeline.builder.v2_4_3;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   I N F O   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateInfoBuilder
  extends TaskBuilder
{
  /**
   * Create a new Template Info Builder.
   * 
   * @param mclient
   *   The instance of MasterMgrClient to use in the builder.
   * 
   * @param qclient
   *   The instance of QueueMgrClient to use in the builder.
   * 
   * @param builderInformation
   *   The instance of the global information class used to share information between all the
   *   Builders that are invoked.e 
   * 
   * @param templatePath
   *   The path of the node containing the template glue information.
   * 
   * @param author
   *   The user whose working area the template information node.
   * 
   * @param view
   *   The user's working area containing the template information node. 
   * 
   * @throws PipelineException
   */
  public
  TemplateInfoBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    String templatePath,
    String author,
    String view
  )
    throws PipelineException
  {
    super("TaskInfoBuilder",
      "Builder which reads a template information file and then uses that information to" +
      "run the template and create an instance of it for particular project.",
      mclient, qclient, builderInformation, EntityType.Ignore);
    
    noDefaultConstructPasses();
    
    pTemplateStartNode = templatePath;
    
    {
      NodeID nodeID = 
        new NodeID(author, view, templatePath);
      pTemplateInfoMod = mclient.getWorkingVersion(nodeID);
      FileSeq seq = pTemplateInfoMod.getPrimarySequence();
      Path p = new Path
        (PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + seq.getFile(0));
      pFile = p.toFile();

      try {
        pTemplateGlueInfo = 
          (TemplateGlueInformation) GlueDecoderImpl.decodeFile(aTemplateGlueInfo, pFile);
      }
      catch (GlueException ex) {
        throw new PipelineException
        (Exceptions.getFullMessage
        ("Error reading the glue file with template information", ex));
      }

      // This is a task builder
      if (pTemplateGlueInfo.getNodesInTemplate().isEmpty()) {
        for (String source : pTemplateInfoMod.getSourceNames()) {
          TreeMap<String, BaseAnnotation> annots = getTaskAnnotations(source);
          if (annots.isEmpty())
            throw new PipelineException
            ("There were no nodes specified in the Glue information file and at least one " +
              "of the nodes attached to the template definition node do not contain task " +
              "annotations.  The Template Info Builder is not able to determine what to " +
              "build.");
        }
      }
    }

    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aAllowZeroContexts,
          "Allow contexts to have no replacements.",
          false);
      addParam(param);
    }

    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aInhibitFileCopy,
          "Inhibit the CopyFile flag on all nodes in the template.",
          false);
      addParam(param);
    }


    AdvancedLayoutGroup layout = new AdvancedLayoutGroup
    ("Information Pass", 
      "The First pass of the Template Info Builder",
      "BasicInformation", true);

    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
    layout.addEntry(1, null);
    layout.addEntry(1, aAllowZeroContexts);
    layout.addEntry(1, aInhibitFileCopy);
    
    layout.addColumn("Replacements", true);
    layout.addColumn("Contexts", true);
    layout.addColumn("FrameRanges", true);
    
    pReplacementParams = new ArrayList<KeyValueUtilityParam>();
    pContextParams = new ArrayList<KeyIntValueUtilityParam>();
    pFrameRangeParams = new TreeMap<String, FrameRangeUtilityParam>();
    
    TreeMap<String, String> rDefaults = pTemplateGlueInfo.getReplacementDefaults();
    TreeMap<String, String> rParamNames = pTemplateGlueInfo.getReplacementParamNames();
    int i = 1;
    for (String replacement : pTemplateGlueInfo.getReplacements()) {
      String name = "Replace" + String.valueOf(i); 
      
      if (rParamNames != null) {
        String rName = rParamNames.get(replacement);
        if (rName != null && !rName.equals(""))
          name = rName;
      }
      
      KeyValueUtilityParam param = 
        new KeyValueUtilityParam
        (name,
         "The string replacement value for the key (" + replacement + ")",
         replacement,
         rDefaults.get(replacement));
      addParam(param);
      layout.addEntry(2, name);
      pReplacementParams.add(param);
      i++;
    }
    
    i = 1;
    for (String context : pTemplateGlueInfo.getContexts().keySet()) {
      String name = "Context" + String.valueOf(i); 
      KeyIntValueUtilityParam param =
        new KeyIntValueUtilityParam
        (name,
         "How many replacements are needed for the context (" + context + ")",
         context,
         1);
      addParam(param);
      pContextParams.add(param);
      layout.addEntry(3, name);
      i++;
    }
    
    for (String range : pTemplateGlueInfo.getFrameRanges()) {
      FrameRangeUtilityParam param =
        new FrameRangeUtilityParam
        (range,
         "The frame range",
         pTemplateGlueInfo.getFrameRangeDefaults().get(range)
        );
      addParam(param);
      pFrameRangeParams.put(range, param);
      LayoutGroup group = new LayoutGroup(range, "The frame range", true);
      group.addEntry(range);
      layout.addSubGroup(4, group);
    }
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InformationPass());
    addSetupPass(new ContextInfoPass());
    
    pAOEModes = pTemplateGlueInfo.getAOEModes();
    for (String mode : pAOEModes.keySet()) {
      ActionOnExistence aoe = pAOEModes.get(mode);
      addAOEMode(mode, aoe);
    }

    PassLayoutGroup passLayout = new PassLayoutGroup(layout.getName(), layout);
    passLayout.addPass("ContextInfoPass", new AdvancedLayoutGroup("ContextInfoPass", true));
    setLayout(passLayout);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the TemplateBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      getStageInformation().setDoAnnotations(true);
      
      pReplacements = new TreeMap<String, String>();
      for (KeyValueUtilityParam param : pReplacementParams) {
        String value = param.getValueValue();
        if (value == null)
          value = "";
        pReplacements.put(param.getKeyValue(), value);
      }
      
      pContextValues = new TreeMap<String, Integer>();
      for (KeyIntValueUtilityParam param : pContextParams) {
        
        Integer value = param.getValueValue();
        if (value == null)
          throw new PipelineException
            ("A (null) value was specified for the number of context replacements for the " +
             "context (" + param.getKeyValue() + ")");
        pContextValues.put(param.getKeyValue(), value);
      }

      pFrameRanges = new TreeMap<String, FrameRange>();
      for (String frameRange: pFrameRangeParams.keySet()) {
        FrameRangeUtilityParam param = pFrameRangeParams.get(frameRange);
        pFrameRanges.put(frameRange, param.getFrameRangeValue());
      }
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      pContextBuilders = new TreeMap<String, ContextInfoBuilder>();
      int i = 0;
      for (String context : pContextValues.keySet()) {
        Integer num = pContextValues.get(context);
        ContextInfoBuilder builder = 
          new ContextInfoBuilder
            (pClient, pQueue, getBuilderInformation(), context, i, num, 
             pTemplateGlueInfo.getContexts().get(context),  
             pTemplateGlueInfo.getContextDefaults().get(context),
             pTemplateGlueInfo.getContextParamNames().get(context));
        addSubBuilder(builder);
        pContextBuilders.put(context, builder);
        i++;
      }
    }
    
    private static final long serialVersionUID = 6263150226803329092L;
  }
  
  private 
  class ContextInfoPass
    extends SetupPass
  {
    public 
    ContextInfoPass()
    {
      super("Context Information Pass", 
            "Context Information pass for the TemplateBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pContexts = new TreeMap<String, ArrayList<TreeMap<String,String>>>();
      for (String context : pContextBuilders.keySet()) {
        ArrayList<TreeMap<String,String>> values = 
          pContextBuilders.get(context).getContextValues();
        pContexts.put(context, values);
      }
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TreeSet<String> nodes = pTemplateGlueInfo.getNodesInTemplate();
      if (nodes.isEmpty()) {
        TemplateTaskBuilder builder = 
          new TemplateTaskBuilder(pClient, pQueue, getBuilderInformation(),
            pTemplateStartNode, pReplacements, pContexts, pFrameRanges, pAOEModes);
        addSubBuilder(builder);
        addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);
        addMappedParam(builder.getName(), aAllowZeroContexts, aAllowZeroContexts);
        addMappedParam(builder.getName(), aInhibitFileCopy, aInhibitFileCopy);
      }
      else {
        TemplateBuildInfo info = new TemplateBuildInfo();
        info.setNodesToBuild(nodes);
        TemplateBuilder builder = 
          new TemplateBuilder(pClient, pQueue, getBuilderInformation(),
            info, pReplacements, pContexts, pFrameRanges, pAOEModes);
        addSubBuilder(builder);
        addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);
        addMappedParam(builder.getName(), aAllowZeroContexts, aAllowZeroContexts);
        addMappedParam(builder.getName(), aInhibitFileCopy, aInhibitFileCopy);
      }
    }
    
    private static final long serialVersionUID = -2407515691903472636L;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   B U I L D E R S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Need to pass in:
   *   The number of runs.
   *   The list of context values.
   *   Any default values.
   */
  private
  class ContextInfoBuilder
    extends BaseBuilder
  {
    private
    ContextInfoBuilder
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient,
      BuilderInformation builderInformation,
      String contextName,
      Integer contextNum,
      Integer numValues,
      ListSet<String> contextReplacements,
      ArrayList<TreeMap<String, String>> defaultValues,
      TreeMap<String, String> paramNames
    ) 
      throws PipelineException
    {
      super("Context" + String.valueOf(contextNum),
            "The values for the context (" + contextName + ")",
            mclient, qclient, builderInformation);
      
      pParams = new ArrayList<ArrayList<KeyValueUtilityParam>>();
      
      noDefaultConstructPasses();
      
      AdvancedLayoutGroup layout = new AdvancedLayoutGroup
        ("ContextInformation", "Values for the context", "Context: " + contextName, true);
      
      ArrayList<TreeMap<String, String>> defaults = null;
      if (defaultValues == null)
         defaults = new ArrayList<TreeMap<String,String>>();
      else 
        defaults = new ArrayList<TreeMap<String,String>>(defaultValues);

      TreeMap<String, String> lastGoodReplacement = null;
      
      TreeMap<String, String> pNames = new TreeMap<String, String>();
      if (paramNames != null)
        pNames.putAll(paramNames);
      
      for (int i = 0; i < numValues; i++ ) {
        LayoutGroup group = new LayoutGroup(true);
        
        ArrayList<KeyValueUtilityParam> params = 
          new ArrayList<KeyValueUtilityParam>();
        TreeMap<String, String> defaultsV = null;
        if (!defaults.isEmpty()) {
          defaultsV = defaults.remove(0);
          lastGoodReplacement = defaultsV;
        }
        else
          defaultsV = lastGoodReplacement;
        
        String base = "C" + String.valueOf(i);
        int j = 0;
        for (String key : contextReplacements) {
          
          //Compute the name of the parameter
          String name = base + "V" + String.valueOf(j);
          {
            String paramName = pNames.get(key);
            if (paramName  != null && !paramName.equals("")) {
              name = paramName + String.valueOf(i);
            }
          }
          
          String value = null;
          if (defaultsV != null)
            value = defaultsV.get(key);
          KeyValueUtilityParam param = 
            new KeyValueUtilityParam
            (name,
             "Context key and value",
             key,
             value);
          addParam(param);
          params.add(param);
          group.addEntry(name);
          j++;
        }
        layout.addSubGroup(1, group);
        pParams.add(params);
      }
      
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aActionOnExistence);
      layout.addEntry(1, aReleaseOnError);
      
      addSetupPass(new ContextInformationPass());
      
      PassLayoutGroup pLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(pLayout);
    }

    private
    class ContextInformationPass
      extends SetupPass
    {
      private 
      ContextInformationPass()
      {
        super("ContextInformationPass", 
              "The pass which gets the information about the contexts");
      }
      
      @Override
      public void validatePhase()
        throws PipelineException
      {
        validateBuiltInParams();
        
        pToReturn = new ArrayList<TreeMap<String,String>>();
        
        for (ArrayList<KeyValueUtilityParam> params : pParams) {
          TreeMap<String, String> values = new TreeMap<String, String>();
          for (KeyValueUtilityParam param : params ) {
            String value = param.getValueValue();
            if (value == null)
              value = "";
            values.put(param.getKeyValue(), value);
          }
          pToReturn.add(values);
        }
      }
      
      private static final long serialVersionUID = -669583086246605723L;
    }
    
    
    public ArrayList<TreeMap<String, String>>
    getContextValues()
    {
      return pToReturn;
    }
    
  
    
    /*--------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                    */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = 552411695120650964L;

    
    
    /*--------------------------------------------------------------------------------------*/
    /*  I N T E R N A L S                                                                   */
    /*--------------------------------------------------------------------------------------*/
    
    private ArrayList<ArrayList<KeyValueUtilityParam>> pParams;
    private ArrayList<TreeMap<String, String>> pToReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -711763397951071286L;

  public static final String aTemplateGlueInfo = "TemplateGlueInfo";
  public static final String aAllowZeroContexts = "AllowZeroContexts";
  public static final String aInhibitFileCopy   = "InhibitFileCopy";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TemplateGlueInformation pTemplateGlueInfo;
  
  private TreeMap<String, String> pReplacements;
  
  private ArrayList<KeyValueUtilityParam> pReplacementParams;

  private ArrayList<KeyIntValueUtilityParam> pContextParams;
  
  private TreeMap<String, Integer> pContextValues;
  
  private TreeMap<String, ContextInfoBuilder> pContextBuilders;
  
  private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
  
  private TreeMap<String, FrameRange> pFrameRanges;
  
  private TreeMap<String, FrameRangeUtilityParam> pFrameRangeParams;
  
  private TreeMap<String, ActionOnExistence> pAOEModes;

  private File pFile;
  
  private NodeMod pTemplateInfoMod;
  
  private String pTemplateStartNode;
}
