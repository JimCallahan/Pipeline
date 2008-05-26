// $Id: AssetClass.java,v 1.1 2008/05/26 03:19:49 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   C L A S S                                                                  */
/*------------------------------------------------------------------------------------------*/


/**
 * The different classes of assets in the Base Collection
 */
public 
enum AssetClass
{
  /**
   * Assets built with the {@link AssetBuilder}.
   */
  Asset, 
  
  /**
   * Assets built with the {@link SimpleAssetBuilder}.
   */
  SimpleAsset,
  
  /**
   * Assets built with the {@link CameraAssetBuilder}
   */
  Camera
}
