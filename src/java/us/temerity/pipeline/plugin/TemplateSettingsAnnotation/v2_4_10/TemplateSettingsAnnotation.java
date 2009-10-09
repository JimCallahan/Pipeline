// $Id: TemplateSettingsAnnotation.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateSettingsAnnotation.v2_4_10;

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
 *     has a disabled action.  This will clone secondary file sequences.
 *   </DIV> <BR> 
 *   
 *   Touch Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Assign the Touch action to the node, temporarily.  This allow networks to be
 *     queued just like normal.  During a special second finalize stage (after all other 
 *     staleness effecting changes have been made), this node will have its Action returned
 *     to its originally specified Action and the files will be touched (so it will never
 *     actually be queued).  The end effect will be that the node has the right Action, but
 *     the expensive calculation of the Action will never be done.  Of course, anything 
 *     downstream that depends on those files will either need be able to deal with empty
 *     files or will have to have use the Touch Files setting as well.
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
 *   Post Remove Action<BR>
 *   <DIV style="margin-left: 40px;">
 *     After the action has been run, remove it.
 *   </DIV> <BR>
 *   
 *   Post Disable Action<BR>
 *   <DIV style="margin-left: 40px;">
 *     After the action has been run, disable it.
 *   </DIV> <BR>
 *   
 *   Vouchable <BR>
 *   <DIV style="margin-left: 40px;">
 *     Designate a node that can be vouched for by the builder during any queue phase.  If a 
 *     node is in a Dubious state and does not have this setting enabled, the builder will 
 *     throw an exception and cease execution. 
 *   </DIV> <BR>
 *   
 *   Intermediate <BR>
 *   <DIV style="margin-left: 40px;">
 *     Designate a node that should be made an Intermediate node. 
 *   </DIV> <BR>
 *   
 *   Modify Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Apply the string replaces that were used to modify the node name of this node to the
 *     contents of the file as well.  This will obviously only work if the file in question is
 *     an ascii file, not a binary file.  This uses Java's built in string processing, which
 *     makes it nicely cross-platform, but will also not be the fastest thing ever.  Using 
 *     this setting on extremely large files may slow down the template builder quite a bit.
 *     Using this setting without the CloneFiles option will result in an error. 
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
    super("TemplateSettings", new VersionID("2.4.10"), "Temerity", 
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
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aTouchFiles,
         "Instead of running the action, simply touch the files during the build phase.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aVouchable,
         "Designate a node which the template builder can vouch for during any queue phases " +
         "of the builder.",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aIntermediate,
         "Designate a node which will be made an intermediate node",
         false);
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BooleanAnnotationParam
        (aModifyFiles,
         "Apply the string replacements used in the file name to the contents of the file " +
         "as well.",
         false);
      addParam(param);
    }
    
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aPreEnableAction);
      layout.add(aPostRemoveAction);
      layout.add(aPostDisableAction);
      layout.add(null);
      layout.add(aVouchable);
      layout.add(aIntermediate);
      layout.add(null);
      layout.add(aCloneFiles);
      layout.add(aTouchFiles);
      layout.add(aModifyFiles);
      layout.add(null);
      layout.add(aUnlinkAll);
      
      setLayout(layout);      
    }
    
    underDevelopment();

    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1301856935267293922L;
  
  public static final String aCloneFiles        = "CloneFiles";
  public static final String aIntermediate      = "Intermediate";
  public static final String aTouchFiles        = "TouchFiles";
  public static final String aPreEnableAction   = "PreEnableAction";
  public static final String aUnlinkAll         = "UnlinkAll";
  public static final String aPostRemoveAction  = "PostRemoveAction";
  public static final String aPostDisableAction = "PostDisableAction";
  public static final String aVouchable         = "Vouchable";
  public static final String aModifyFiles       = "ModifyFile";
}
