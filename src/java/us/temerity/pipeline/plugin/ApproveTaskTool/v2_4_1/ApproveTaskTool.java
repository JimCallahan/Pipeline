// $Id: ApproveTaskTool.java,v 1.6 2009/09/16 15:56:45 jesse Exp $

package us.temerity.pipeline.plugin.ApproveTaskTool.v2_4_1;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;


/*------------------------------------------------------------------------------------------*/
/*   A P P R O V E   T A S K   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A tool which allows the supervisor to run the approval builder for a task using a 
 * selected submit node as the entry point.
 */
public 
class ApproveTaskTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ApproveTaskTool() 
  {
    super("ApproveTask", new VersionID("2.4.1"), "Temerity", 
	  "A tool which allows the supervisor to run the approval builder for a task " + 
          "using a selected submit node as the entry point."); 
    
    addPhase(new FirstPhase());
    addPhase(new SecondPhase());

  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   T O O L   O V E R R I D E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to perform a node status update upon successfully executing the tool. <P> 
   */ 
  @Override
  public boolean 
  updateOnExit()
  {
    return false;
  }
  
  /**
   * Whether to display the Log History dialog when running this tool.
   */ 
  @Override
  public boolean 
  showLogHistory() 
  {
    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Validate the selected submit node, find the possible checked-in versions to approve 
   * and the corresponding approve node for the task.
   */ 
  private 
  class FirstPhase
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      if(pPrimary == null || pSelected.size() > 1) 
	throw new PipelineException
          ("Please select the submit node and only the submit node for the task you " + 
           "wish the approve!"); 

      return " : Validating Task";
    }

    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      TreeMap<String, BaseAnnotation> tannots = getTaskAnnotation(pPrimary, mclient);
      if(tannots == null) 
        throw new PipelineException
          ("The selected node (" + pPrimary + ") does not have the appropriate " + 
           "annotations to be part of a task.");
      
      BaseAnnotation tannot = tannots.get(tannots.firstKey());
      
      /* save the task information */ 
      pProjectName = lookupProjectName(pSubmitNode, tannot);
      pTaskName    = lookupTaskName(pSubmitNode, tannot);
      pTaskType    = lookupTaskType(pSubmitNode, tannot);
      
      String editNode = getEditNode(pPrimary, mclient);
      
      String nodes[] = getSubmitAndApproveNodes(editNode, mclient);
      pSubmitNode = nodes[0];
      pApproveNode = nodes[1];
      
      
      {
        pSubmitVersionIDs = 
	  new ArrayList<VersionID>(mclient.getCheckedInVersionIDs(pSubmitNode)); 
	Collections.reverse(pSubmitVersionIDs);
      }

      {
        /* lookup the approval builder */ 
        TreeMap<String, BaseAnnotation> annotations = 
          getTaskAnnotation(pApproveNode, mclient);

        for(String aname : annotations.keySet()) {
          if(aname.equals("Task") || aname.startsWith("AltTask")) {
            BaseAnnotation annot = annotations.get(aname);
            if(lookupPurpose(pApproveNode, aname, annot).equals(aApprove)) { 
              BuilderID builderID = (BuilderID) annot.getParamValue(aApprovalBuilder);
              if(builderID == null) 
                throw new PipelineException
                ("No " + aApprovalBuilder + " parameter was specified in the " + 
                  "ApproveTask annotation for the node (" + pApproveNode + ")!"); 

              pApproveBuilderID = builderID;
            }
          }
        }
      }
      return NextPhase.Continue;
    }
  }
  
  /**
   * Query the supervisor for a suitable check-in message and version level and 
   * then run the approval builder.
   */ 
  private 
  class SecondPhase
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
	
        UIFactory.createTitledTextField
          (tpanel, "Project Name:", sTSize, 
           vpanel, pProjectName, sVSize, 
           "The name of the project this task part of achieving."); 
        
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        UIFactory.createTitledTextField
          (tpanel, "Task Name:", sTSize, 
           vpanel, pTaskName, sVSize, 
           "The name of the overall production goal this node is used to achieve.  " + 
           "Typically, this is the name of a shot or the asset name.");

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        UIFactory.createTitledTextField
          (tpanel, "Task Type:", sTSize, 
           vpanel, pTaskType, sVSize, 
           "A unique type of production goal this node is used to achieve which is not one " +
           "of the standard type available in TaskType."); 

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
          ArrayList<String> choices = new ArrayList<String>(); 
          for(VersionID vid : pSubmitVersionIDs) 
            choices.add("v" + vid);
          
          pSubmitVersionField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Submit Version:", sTSize, 
             vpanel, choices, sVSize, 
             "The revision number of the submit node being approved.");
        }

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
          ArrayList<String> choices = new ArrayList<String>(); 
          for(VersionID.Level level : VersionID.Level.all()) 
            choices.add(level.toString()); 
          
          pCheckInLevelField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Check-In Level:", sTSize, 
             vpanel, choices, sVSize, 
             "The level of the check-in for the approve node."); 

          pCheckInLevelField.setSelected(VersionID.Level.Minor.toString());
        }

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        {
          pApprovalMessageArea = 
            UIFactory.createTitledEditableTextArea
            (tpanel, "Approval Message:", sTSize, 
             vpanel, "", sVSize, 5, true, 
             "The check-in message describing the reason for approving this task.");
        }

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  pWaitOnBuilderField = 
            UIFactory.createTitledBooleanField
	    (tpanel, "Wait on Builder:", sTSize, 
	     vpanel, sVSize,
	     "Whether to have the tool wait for the builder to complete before returning " + 
	     "control back to the user.  If set to (NO), then the builder will be run in " + 
	     "the background.  In either case, builder progress can be monitored in the " + 
	     "Log History dialog."); 

	  pWaitOnBuilderField.setValue(true); 
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	
	{
          pReleaseViewField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Release View:", sTSize, 
             vpanel, ReleaseView.titles(), sVSize,
             "Whether the approval builder should release the special created working area" +
             "when it finished running."); 

          pReleaseViewField.setSelected(ReleaseView.Never.toTitle()); 
        }
      }
      
      JToolDialog diag = 
        new JToolDialog("Approve Task: " + pTaskName + " " + pTaskType, body, "Approve");

      diag.setVisible(true);
      if(diag.wasConfirmed()) 
	return " : Approval Builder Started";

      return null;
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      if(pWaitOnBuilderField.getValue()) {
	try {
	  runBuilder(mclient, qclient); 
	} 
	catch(PipelineException ex) {
	  throw ex; 
	}
	catch(Exception ex) {
	  throw new PipelineException(null, ex, true, true); 
	}
      }
      else {
	RunBuilderTask task = new RunBuilderTask();
	task.start(); 
      }
      
      return NextPhase.Finish;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   B U I L D E R   E X E C U T I O N                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Instantiate and run the builder.
   */ 
  private void 
  runBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {	
    /* construct the builder parameters */ 
    MultiMap<String, String> params = new MultiMap<String, String>();
    {
      LinkedList<String> bkey = new LinkedList<String>();  
      bkey.add(pApproveBuilderID.getBuilderName());

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(BaseUtil.aUtilContext);
	keys.add(UtilContextUtilityParam.aAuthor); 
          
	params.putValue(keys, getAuthor(), true);
      }
        
      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(BaseUtil.aUtilContext);
	keys.add(UtilContextUtilityParam.aView); 
          
	params.putValue(keys, getView(), true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aApproveNode);

	params.putValue(keys, pApproveNode, true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aSubmitNode);

	params.putValue(keys, pSubmitNode, true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aSubmitVersion);

	VersionID vid = pSubmitVersionIDs.get(pSubmitVersionField.getSelectedIndex());

	params.putValue(keys, vid.toString(), true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aCheckInLevel);

	VersionID.Level level = 
	  VersionID.Level.all().get(pCheckInLevelField.getSelectedIndex());

	params.putValue(keys, level.toString(), true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aApprovalMessage);

	String msg = pApprovalMessageArea.getText();

	params.putValue(keys, msg, true);
      }
      
      {
        LinkedList<String> keys = new LinkedList<String>(bkey); 
        keys.add(aReleaseView);

        String rv = pReleaseViewField.getSelected();

        params.putValue(keys, rv, true);
      }
    }

    /* create a new builder collection */ 
    BaseBuilderCollection collection = 
      PluginMgrClient.getInstance().newBuilderCollection
      (pApproveBuilderID.getName(), 
       pApproveBuilderID.getVersionID(), 
       pApproveBuilderID.getVendor()); 
    
    /* instantiate the builder */ 
    BaseBuilder builder = 
      collection.instantiateBuilder(pApproveBuilderID.getBuilderName(), mclient, qclient, 
                                    false, true, false, false, params);
    if(builder == null)
      throw new PipelineException
        ("Unable to instantiate the builder " + pApproveBuilderID.getBuilderName() + " " + 
         "(v" + pApproveBuilderID.getVersionID() + ") from Vendor " + 
         "(" + pApproveBuilderID.getVendor() + ")!"); 
    
    /* run it! */ 
    builder.run();
  }

  /**
   * Lookup the value of the TaskName annotation parameter.
   * 
   * @param name
   *   The fully resolved name of the node having the given annotation.
   * 
   * @param aname
   *   The name of the annotation instance.
   * 
   * @param annot
   *   The annotation instance.
   */ 
  private String
  lookupPurpose
  (
   String name, 
   String aname, 
   BaseAnnotation annot   
  ) 
    throws PipelineException
  {
    String purpose = (String) annot.getParamValue(aPurpose);
    if(purpose == null) 
      throw new PipelineException
        ("No " + aPurpose + " parameter was specified for the (" + aname + ") " + 
	 "annotation on the node (" + name + ")!"); 

    return purpose;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Run the builder in a separate thread so control can be returned to plui(1).
   */ 
  private
  class RunBuilderTask
    extends Thread
  {
    public 
    RunBuilderTask() 
    {
      super("ApproveTaskTool:RunBuilderTask");
    }
    
    @Override
    public void 
    run() 
    {	
      //FIXME Should This get a connection from UIMaster?
      try {
	runBuilder(new MasterMgrClient(), new QueueMgrClient()); 
      } 
      catch(PipelineException ex) {
        LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      catch(Exception ex) {
        LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Severe,
	   Exceptions.getFullMessage(ex));
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -340798787084943636L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;

  public static final String aApprovalBuilder = "ApprovalBuilder";

  public static final String aApproveNode     = "ApproveNode";    
  public static final String aSubmitNode      = "SubmitNode"; 
  public static final String aSubmitVersion   = "SubmitVersion";
  public static final String aApprovalMessage = "ApprovalMessage";
  public static final String aCheckInLevel    = "CheckInLevel";
  public static final String aReleaseView     = "ReleaseView";
  
  public static final String aSubmit  = "Submit";
  public static final String aApprove = "Approve"; 



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The fully resolved name of the task submit node and the valid checked-in versions.
   */ 
  private String pSubmitNode; 
  private ArrayList<VersionID> pSubmitVersionIDs; 
  
  /** 
   * The task identifiers.
   */ 
  protected String pProjectName;
  protected String pTaskName;
  protected String pTaskType;

  /**
   * The fully resolved name and approval builder ID for the task approve node.
   */ 
  private String pApproveNode; 
  private BuilderID pApproveBuilderID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The submit node revision number selection field.
   */ 
  private JCollectionField pSubmitVersionField; 

  /**
   * The check-in level selection field.
   */ 
  private JCollectionField pCheckInLevelField; 

  /**
   * The check-in message text area.
   */ 
  private JTextArea pApprovalMessageArea;

  /**
   * Whether the tool should wait on the builder to complete.
   */ 
  private JBooleanField  pWaitOnBuilderField; 
  
  /**
   * Whether the tool should release the working area when it finishes.
   */
  private JCollectionField  pReleaseViewField;
}
