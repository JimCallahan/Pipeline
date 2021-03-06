// $Id: TemplateGlueBuilder.java,v 1.6 2009/05/07 03:12:50 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueCollection.v2_4_5;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_3.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   B U I L D E R                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for running templates using the TemplateGlueInformation format from the
 * command-line using plbuilder.
 * <p>
 * This is not the preferred way to invoke templates.  Both the {@link TemplateBuilder}
 * and the {@link TemplateTaskBuilder} have constructors which are meant to be called
 * directly from other programs.  Passing command-line arguments in to the Task Builder
 * through this mechanism is using two layers of indirection and will be noticeably clunky. 
 */
public 
class TemplateGlueBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor for to launch the builder.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   */ 
  public
  TemplateGlueBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    super("TemplateGlueBuilder",
         "A builder for running templates with plbuilder.", 
         mclient, qclient, builderInfo);
    

    {
      UtilityParam param =
        new StringUtilityParam
        (aTemplateNode,
         "The Name of the node to find the template information in.", 
         null);
      addParam(param);
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
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
          (aCheckOutTemplate,
           "Checkout the template before running it.",
           false);
      addParam(param);
    }
    
    addCheckinWhenDoneParam();
    
    /* create the setup passes */ 
    addSetupPass(new InitTemplate());
    
    disableParam(new ParamMapping(aActionOnExistence));

    AdvancedLayoutGroup layout = 
      new AdvancedLayoutGroup
      ("Builder Information", 
       "The pass where all the basic information about the asset is collected " +
       "from the user.", 
       "BuilderSettings", 
       true);
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
    layout.addEntry(1, null);
    layout.addEntry(1, aTemplateNode);
    layout.addEntry(1, aAllowZeroContexts);
    layout.addEntry(1, aInhibitFileCopy);
    layout.addEntry(1, aCheckOutTemplate);
    
    PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
    
    setLayout(finalLayout);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class InitTemplate
    extends SetupPass
  {
    public 
    InitTemplate()
    {
      super("InitTemplate", 
            "Setup pass"); 
    }

    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pNodeName = getStringParamValue(new ParamMapping(aTemplateNode));
      
      if (!nodeExists(pNodeName))
        throw new PipelineException
          ("The node (" + pNodeName + ") specified as the template node is not a " +
           "Pipeline node.");
      
      boolean checkOut = getBooleanParamValue(new ParamMapping(aCheckOutTemplate));
      
      if (checkOut) {
        TreeSet<String> checkedIn = pClient.getCheckedInNames(pNodeName);
        if (checkedIn != null && checkedIn.contains(pNodeName))
          checkOutNewer(pNodeName, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      }
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TemplateInfoBuilder builder = 
        new TemplateInfoBuilder(pClient, pQueue, getBuilderInformation(), pNodeName, getAuthor(), getView());
      addSubBuilder(builder, false, 50);
      addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);
      addMappedParam(builder.getName(), aAllowZeroContexts, aAllowZeroContexts);
      addMappedParam(builder.getName(), aInhibitFileCopy, aInhibitFileCopy);
      addMappedParam(builder.getName(), aUtilContext, aUtilContext);
      addMappedParam(builder.getName(), aReleaseOnError, aReleaseOnError);
    }
    private static final long serialVersionUID = -3615571748207002085L;
    
    private String pNodeName;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aTemplateNode      = "TemplateNode";
  public static final String aAllowZeroContexts = "AllowZeroContexts";
  public static final String aInhibitFileCopy   = "InhibitFileCopy";
  public static final String aCheckOutTemplate  = "CheckOutTemplate";
  
  private static final long serialVersionUID = 7681162001215421131L;

}
