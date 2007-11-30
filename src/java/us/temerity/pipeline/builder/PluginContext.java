package us.temerity.pipeline.builder;

import us.temerity.pipeline.VersionID;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.Range;

public 
class PluginContext
  implements Glueable
{
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from
   * user code.
   */
  public 
  PluginContext()
  {
    pPluginName = null;
    pPluginVendor = null;
    pRange = null;
  }
  
  public 
  PluginContext
  (
    String pluginName, 
    String pluginVendor,
    Range<VersionID> range
  )
  {
    if ( pluginName == null )
      throw new IllegalArgumentException(
	"Cannot pass in a null value for the plugin name.");
    if ( pluginVendor == null )
      throw new IllegalArgumentException(
	"Cannot pass in a null value for the plugin vendor.");
    pPluginName = pluginName;
    pPluginVendor = pluginVendor;
    if (range == null)
      pRange = new Range<VersionID>(null, null);
    else
      pRange = range;
  }
  
  public 
  PluginContext
  (
    String pluginName, 
    String pluginVendor
  )
  {
    this(pluginName, pluginVendor, null); 
  }

  public 
  PluginContext
  (
    String pluginName
  )
  {
    this(pluginName, "Temerity", null);
  }

  /**
   * @return the PluginName
   */
  public String 
  getPluginName()
  {
    return pPluginName;
  }

  /**
   * @return the PluginVendor
   */
  public String 
  getPluginVendor()
  {
    return pPluginVendor;
  }
  
  public Range<VersionID>
  getRange()
  {
    return pRange;
  }

  @Override
  public String toString()
  {
    return("[Plugin Name: " + pPluginName + ", Vendor: " + 
           pPluginVendor + ", Range: " + pRange.toString() + "]");
  }
  
  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
    GlueDecoder decoder
  )
    throws GlueException
  {
    pPluginName = (String) decoder.decode("PluginName");
    pPluginVendor = (String) decoder.decode("PluginVendor");
    pRange = (Range<VersionID>) decoder.decode("Range");
  }

  public void 
  toGlue
  (
    GlueEncoder encoder
  )
    throws GlueException
  {
    encoder.encode("PluginName", pPluginName);
    encoder.encode("PluginVendor", pPluginName);
    encoder.encode("Range", pRange);
  }


  private String pPluginName;
  private String pPluginVendor;
  private Range<VersionID> pRange;
}
