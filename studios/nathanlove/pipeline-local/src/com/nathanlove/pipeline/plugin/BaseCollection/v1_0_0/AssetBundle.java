// $Id: AssetBundle.java,v 1.1 2008/05/26 03:19:49 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   B U N D L E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Combination of an {@link AssetNamer} and the {@link AssetClass} associated with that
 * namer. 
 */
public 
class AssetBundle
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor.
   * @param assetNamer
   *   The AssetNamer.  Should not be <code>null</code>
   */
  public 
  AssetBundle
  (
    AssetNamer assetNamer
  )
    throws PipelineException
  {
    super();
    pAssetClass = assetNamer.getAssetClass();
    pAssetNamer = assetNamer;
  }
  
  /**
   * Get the Asset Class.
   */
  public final AssetClass 
  getAssetClass()
  {
    return pAssetClass;
  }

  /**
   * Get the Asset Namer.
   */
  public final AssetNamer 
  getAssetNamer()
  {
    return pAssetNamer;
  }
  
  
  private AssetClass pAssetClass;
  private AssetNamer pAssetNamer;
}
