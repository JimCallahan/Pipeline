package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

import java.util.TreeMap;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   E N T I T Y   B U N D L E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Shotgun Entity
 * <p>
 * Contains the type of the entity and its ID (which are what are needed to reference it in
 * Shotgun).  Can also, optionally, contain the name of the entity. 
 */
public 
class ShotgunEntityBundle
{
  
  /**
   * Constructor
   * @param entity
   *   The Shotgun Entity
   * @param id
   *   The Unique ID
   * @throws PipelineException
   *   If the Entity is <code>null</code>.
   */
  public
  ShotgunEntityBundle
  (
    ShotgunEntity entity,
    int id
  )
    throws PipelineException
  {
    this(entity, id, null);
  }
  
  /**
   * Constructor
   * @param entity
   *   The Shotgun Entity
   * @param id
   *   The Unique ID
   * @param name
   *   The name of the entity.
   * @throws PipelineException
   *   If the Entity is <code>null</code>.
   */
  public
  ShotgunEntityBundle
  (
    ShotgunEntity entity,
    int id,
    String name
  )
    throws PipelineException
  {
    if (entity == null)
      throw new PipelineException
        ("Cannot have a null Shotgun Entity in a ShotgunEntityBundle");
    pEntity = entity;
    pID = id;
    pName = name; 
  }
  
  /**
   * Get the entity.
   */
  public ShotgunEntity 
  getEntity()
  {
    return pEntity;
  }

  /**
   * Get the ID.
   */
  public Integer 
  getID()
  {
    return pID;
  }

  /**
   * Get the name.
   */
  public String 
  getName()
  {
    return pName;
  }
  
  /**
   * Return the entity and the id in a format that is ready to be passed to a filter or
   * a link in Shotgun.
   */
  public TreeMap<String, Object>
  formatForShotgun()
  {
    TreeMap<String, Object> entityMap = new TreeMap<String, Object>();
    entityMap.put("type", pEntity.toEntity());
    entityMap.put("id", pID);
    return entityMap;
  }

  @Override
  public String 
  toString()
  {
    if (pName != null)
      return "Entity: " + pEntity.toEntity() + ", " + pID + " (" + pName + ")";
    else
      return "Entity: " + pEntity.toEntity() + ", " + pID;
  }
  
  private ShotgunEntity pEntity;
  private Integer pID;
  private String pName;

}
