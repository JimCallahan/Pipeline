package com.nathanlove.pipeline.plugin.BuilderIDAnnotation.v1_0_0;

import us.temerity.pipeline.*;


public 
class BuilderIDAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BuilderIDAnnotation()
  {
    super("BuilderID", new VersionID("1.0.0"), "NathanLove", 
          "Signifies the nodes that make up a common production goal (task).");

    
    {
      AnnotationParam param = 
        new BuilderIDAnnotationParam
        (aApprovalBuilder, 
         "If specified, the name of a custom approval builder within a specific builder " + 
         "collection to run after the task has been approved in order to update and " + 
         "check-in this node.  If not given, the approval network will need to be " + 
         "manually updated and checked-in.", 
          new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask")); 
      addParam(param);
    }
    
    underDevelopment();
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5826891230466727943L;
  
  public static final String aApprovalBuilder = "ApprovalBuilder";
  
}
