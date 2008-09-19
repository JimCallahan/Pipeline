// $Id: TemplateSettingsAnnotation.java,v 1.1 2008/09/19 03:30:09 jesse Exp $

package us.temerity.pipeline.plugin.TemplateSettingsAnnotation.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S E T T I N G S   A N N O T A T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * An annotation containing various settings to control the Template Builder when it makes a
 * copy of this network.
 * <p>
 * This annotation defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Clone Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Clone the files of the primary file sequence when making a copy of the template node.  
 *     This will only be effective if the templated node either does not have an action or 
 *     has a disabled action.  This will not clone secondary file sequences, due to how the
 *     cloneFiles() method in {@link MasterMgrClient} works.
 *   </DIV> <BR> 
 *   
 *   Pre Enable Action<BR>
 *   <DIV style="margin-left: 40px;">
 *     Enable the action of the node during the construction phase.  When combined with the 
 *     Disable Action setting, this will use the Action to initialize the node and then disable
 *     it so that it is user editable.
 *   </DIV> <BR>
 *   
 *   Unlink All<BR>
 *   <DIV style="margin-left: 40px;">
 *     Once the node has been built and queued, unlink all the source nodes. This is useful
 *     when building scenes using placeholder scripts which later need to be disconnected.  If
 *     disconnecting all the nodes is not fine-grained enough, the TemplateUnlinkAnnotation can
 *     be used instead.  Note that this will almost always make a node Dubious, if it does not 
 *     have an enabled action, and so should usually be combined with Vouch.
 *   </DIV> <BR>
 *   
 *   Vouch<BR>
 *   <DIV style="margin-left: 40px;">
 *     Once everything has finished, vouch for this node. This assumes that the constructed
 *     node will end up with either no action or a disabled action.  If that is not the case, 
 *     this setting will have no affect
 *   </DIV> <BR>
 *   
 *   Post Remove Action<BR>
 *   <DIV style="margin-left: 40px;">
 *     After the action has been run, remove it.
 *   </DIV> <BR>
 *   
 *   Post Disable Action<BR>
 *   <DIV style="margin-left: 40px;">
 *     After the action has been run, disable it.
 *   </DIV> <BR>
 */
public 
class TemplateSettingsAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TemplateSettingsAnnotation()
  {
    super("TemplateSettings", new VersionID("2.4.3"), "Temerity", 
          "Settings to control the Template Builder when it makes a copy of this network");
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aCloneFiles,
         "Clone the files when making a copy of the template node.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aPreEnableAction,
         "Enable the action of the node during construction.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aUnlinkAll,
         "Once the node has been built and queued, unlink all the source nodes.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aVouch,
         "Once everything has finished, vouch for this node.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aPostRemoveAction,
         "After the action has been run, remove it.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aPostDisableAction,
         "After the action has been run, disable it.",
         false);
      addParam(param);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aUnlinkAll);
      layout.add(aVouch);
      layout.add(aCloneFiles);
      layout.add(null);
      layout.add(aPreEnableAction);
      layout.add(aPostRemoveAction);
      layout.add(aPostDisableAction); 

      setLayout(layout);      
    }
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 596856736121177610L;
  
  public static final String aCloneFiles = "CloneFiles";
  public static final String aPreEnableAction = "PreEnableAction";
  public static final String aUnlinkAll ="UnlinkAll";
  public static final String aVouch = "Vouch";
  public static final String aPostRemoveAction = "PostRemoveAction";
  public static final String aPostDisableAction = "PostDisableAction";
  
}
