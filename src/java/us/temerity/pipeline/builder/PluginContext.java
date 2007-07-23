package us.temerity.pipeline.builder;

public 
class PluginContext
{
    public 
    PluginContext
    (
      String pluginName, 
      String pluginVendor
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
    }

    public 
    PluginContext
    (
      String pluginName
    )
    {
        pPluginName = pluginName;
        pPluginVendor = "Temerity";
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
    
    @Override
    public String toString()
    {
      return("[Plugin Name: " + pPluginName + ", Vendor: " + pPluginVendor + "]");
    }

    private String pPluginName;
    private String pPluginVendor;
}
