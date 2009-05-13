// $Id: ShotgunEntity.java,v 1.3 2009/05/13 19:01:58 jesse Exp $

package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   E N T I T Y                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The different sorts of Entities in Shotgun that Pipeline can address.
 */
public enum 
ShotgunEntity
{
  Project, Shot, Scene, Task, Version, Asset, Sequence, User, TemerityNode, Note,
  
  /*
   * Anything below this list is currently not being used in the shotgun connector.  
   */
  Practical,
  Tool, 
  Element, 
  File,
  CustomEntity01,
  CustomEntity02,
  CustomEntity03,
  CustomEntity04,
  CustomEntity05,
  CustomEntity06,
  CustomEntity07,
  CustomEntity08,
  CustomEntity09,
  CustomEntity10;
  
  @Override
  public String 
  toString()
  {
    if (this == TemerityNode)
      return "temerity_node";
    return super.toString().toLowerCase();
  }
  
  public String 
  toEntity()
  {
    return super.toString();
  }
}
