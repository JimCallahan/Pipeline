// $Id: StudioDefs.java,v 1.1 2009/04/16 17:57:25 jesse Exp $

package com.nathanlove.pipeline.plugin.BuilderInfo;

import java.io.*;


public 
class StudioDefs
  implements Serializable
{
  public StudioDefs
  (
    String projectName  
  )
  {
    pProjectName = projectName;
  }
  
  public String
  getProjectName()
  {
    return pProjectName;
  }
  
  String pProjectName;
  
  private static final long serialVersionUID = -5009218080328848382L;
}
