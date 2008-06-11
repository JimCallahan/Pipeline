// $Id: SubDir.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

/**
 * Low level path structural component.
 * <p>
 * To change the naming of the directory, simply override the {@link #toString()} method
 * of this enum to return a different name. 
 */
public 
enum SubDir
{
  edit, approve, submit, prepare, product, thumb, prereq, focus;

  public String 
  dirName() {
    switch (this) {
    case prepare:
      return "submit/prepare";
    case product:
      return "approve/product";
    case thumb:
      return "submit/thumb";
    case focus:
      return "submit/focus";
    default:
      return super.toString();
    }
  }
}
