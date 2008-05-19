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
  Project, Shot, Scene, Task, Version, Asset, Sequence, User;
  
  @Override
  public String 
  toString()
  {
    return super.toString().toLowerCase();
  }
  
  public String 
  toEntity()
  {
    return super.toString();
  }
}
