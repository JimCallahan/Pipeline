/*
 * Created on Sep 20, 2006 Created by jesse For Use in us.temerity.pipeline.utils
 */
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

    private String pPluginName;
    private String pPluginVendor;
}
