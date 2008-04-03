// $Id: DeliverTool.java,v 1.2 2008/04/03 10:30:47 jim Exp $

package com.intelligentcreatures.pipeline.plugin.DeliverTool.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;


/*------------------------------------------------------------------------------------------*/
/*   Q T   D E L I V E R   T O O L                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A tool which makes it easier to select a specific version of the checked-in images node 
 * to deliver. <P> 
 * 
 * This tool will eventually be replaced by a more full featured stand-alone application
 * which can be used by producers and non-technical artists to select among the possible
 * checked-in images to deliver.<P> 
 * 
 * Most of the functionality is still provided by the QtDeliver builder, this tool just 
 * make it easier to select the source images.
 */
public 
class DeliverTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DeliverTool() 
  {
    super("Deliver", new VersionID("1.0.0"), "ICVFX", 
	  "A tool which makes it easier to select a specific version of the checked-in " + 
          "images node to deliver."); 
    
    addPhase(new FirstPhase());
    addPhase(new SecondPhase());

    underDevelopment();
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
  public boolean 
  showLogHistory() 
  {
    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Validate the selected images node and find the possible checked-in versions to deliver. 
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
          ("Please select the images node and only the images node you wish to deliver!"); 

      return " : Validating Task";
    }

    @SuppressWarnings("unused")
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      /* determine whether this is at least a focus or product node */ 
      {
        TreeMap<String, BaseAnnotation> annotations = mclient.getAnnotations(pPrimary);
        for(String aname : annotations.keySet()) {
          if(aname.equals("Task") || aname.startsWith("AltTask")) {
            BaseAnnotation annot = annotations.get(aname);
            String purpose = lookupPurpose(pPrimary, aname, annot);
            if(purpose.equals(aFocus) || purpose.equals(aProduct)) {
              pSourceNode = pPrimary;
              break;
            }
          }
        }
      }

      if(pSourceNode == null) 
        throw new PipelineException
          ("The selected node (" + pPrimary + ") does not have the appropriate " + 
           "annotations to be a task " + aFocus + " or " + aProduct + " node!");

      pSourceVersionIDs = 
        new ArrayList<VersionID>(mclient.getCheckedInVersionIDs(pSourceNode)); 
      Collections.reverse(pSourceVersionIDs);

      try {
        NodeVersion vsn = mclient.getCheckedInVersion(pSourceNode, null);
        pSourcePattern = vsn.getPrimarySequence().getFilePattern(); 
      }
      catch(PipelineException ex) {
        throw new PipelineException
          ("No checked-in versions exist of (" + pSourceNode + ") to deliver!"); 
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
    @SuppressWarnings("unused")
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

        {
          ArrayList<String> choices = new ArrayList<String>(); 
          for(VersionID vid : pSourceVersionIDs) 
            choices.add("v" + vid);
          
          pSourceVersionField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Source Version:", sTSize, 
             vpanel, choices, sVSize, 
             "The revision number of the source images node to deliver."); 
        }

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
          ArrayList<String> choices = new ArrayList<String>(); 
          choices.add("QuickTime Movie"); 
          choices.add("DPX Images"); 
          choices.add("Cineon Images"); 
          
          pDeliveryFormatField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Delivery Format:", sTSize, 
             vpanel, choices, sVSize, 
             "The choice of delivery format."); 
        }
      }
      
      JToolDialog diag = 
        new JToolDialog("Deliver Images:  " + pSourcePattern, body, "Run Builder");

      diag.setVisible(true);
      if(diag.wasConfirmed()) 
	return " : Delivery Builder Started";

      return null;
    }
    
    @SuppressWarnings("unused")
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      try {
	runBuilder(mclient, qclient); 
      } 
      catch(PipelineException ex) {
	throw ex; 
      }
      catch(Exception ex) {
	throw new PipelineException(null, ex, true, true); 
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
    String builderName = null;
    {
      ArrayList<String> names = new ArrayList<String>();
      names.add("QtDeliver"); 
      names.add("DpxDeliver"); 
      names.add("CineonDeliver"); 
      
      builderName = names.get(pDeliveryFormatField.getSelectedIndex());
    }

    /* construct the builder parameters */ 
    MultiMap<String, String> params = new MultiMap<String, String>();
    {
      LinkedList<String> bkey = new LinkedList<String>();  
      bkey.add(builderName);

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
	keys.add(aSourceNode);

	params.putValue(keys, pSourceNode, true);
      }

      {
	LinkedList<String> keys = new LinkedList<String>(bkey); 
	keys.add(aSourceVersion);

	VersionID vid = pSourceVersionIDs.get(pSourceVersionField.getSelectedIndex());

	params.putValue(keys, vid.toString(), true);
      }
    }

    /* create a new builder collection */ 
    BaseBuilderCollection collection = 
      PluginMgrClient.getInstance().newBuilderCollection
        ("WtmBuilders", new VersionID("1.0.0"), "ICVFX");
    
    /* instantiate the builder */ 
    BaseBuilder builder = 
      collection.instantiateBuilder(builderName, null, null, 
                                    true, true, false, false, params);
    if(builder == null)
      throw new PipelineException
        ("Unable to instantiate the builder (" + builderName + ")!"); 

    /* run it! */ 
    builder.run();
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S  (these should become part of a CommonTaskUtils eventually)            */
  /*----------------------------------------------------------------------------------------*/

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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1008562788581876936L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;

  public static final String aSourceNode    = "SourceNode";  
  public static final String aSourceVersion = "SourceVersion";  

  public static final String aPurpose = "Purpose";
  public static final String aFocus   = "Focus";
  public static final String aProduct = "Product"; 



  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The fully resolved name of the source images node and version to deliver. 
   */ 
  private String pSourceNode; 
  private ArrayList<VersionID> pSourceVersionIDs; 
  private FilePattern pSourcePattern; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The source images node revision number selection field.
   */ 
  private JCollectionField pSourceVersionField; 

  /**
   * The delivery format selection field.
   */ 
  private JCollectionField pDeliveryFormatField; 

}
