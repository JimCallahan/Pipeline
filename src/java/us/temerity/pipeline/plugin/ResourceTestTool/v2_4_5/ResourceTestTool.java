// $Id: ResourceTestTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.ResourceTestTool.v2_4_5;

import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/**
 *
 */
public class
ResourceTestTool
  extends CommonToolUtils
{
  /**
   * An example of a plugin using resources.
   */
  public
  ResourceTestTool()
  {
    super("ResourceTestTool", new VersionID("2.4.5"), "Temerity",
          "A simple example of using resources in a Plugin.");

    addPhase(new PhaseOne());

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();

      String vendor = getVendor();
      PluginType ptype = getPluginType();
      String name = getName();
      VersionID vid = getVersionID();

      pPluginInfo = vendor + "/" + ptype + "/" + name + "/" + vid;
    }

    /**
     * This is an example of loading resources in a plugin.  This tool only 
     * displays images and does not interact with the selected node[s].
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      String imageResourceName = "imgs/TemerityCircleLogo-sm.png";

      byte[] resourceBytes = readResource(imageResourceName);
      BufferedImage resourceImage = readImage(imageResourceName);
      URL resourceURL = getResource(imageResourceName);

      Box body = new Box(BoxLayout.X_AXIS);

      String[] labels = {
	"  Image loaded using ImageIcon(byte[])", 
	"  Image loaded using ImageIcon(URL)", 
	"  Image loaded using ImageIcon(ImageIO.read(URL))", 
      };

      Box[] boxes = new Box[labels.length];

      for(int i = 0 ; i < boxes.length ; i++) {
	boxes[i] = new Box(BoxLayout.Y_AXIS);

	switch(i) {
	  case 0:
	    boxes[i].add(new JLabel(new ImageIcon(resourceBytes)));
	    break;
	  case 1:
	    boxes[i].add(new JLabel(new ImageIcon(resourceURL)));
	    break;
	  case 2:
	    boxes[i].add(new JLabel(new ImageIcon(resourceImage)));
	    break;
	}

	boxes[i].add(new JLabel(labels[i]));

	body.add(boxes[i]);
      }

      JToolDialog diag = new JToolDialog
	("Resource Example:", 
	 body, 
	 "Continue");

      diag.setVisible(true);
      if(!diag.wasConfirmed())
        return null;

      return ": Done.";
    }
  }

  /**
   * Reads a resource into a byte[]
   */
  private byte[]
  readResource
  (
   String resourceName
  )
    throws PipelineException
  {
    URL resourceURL = getResource(resourceName);

    if(resourceURL == null) {
      throw new PipelineException
	("There is no resource (" + resourceName + ") " + 
	 "for plugin (" + pPluginInfo + ")!");
    }

    byte[] resourceBytes = null;
    long filesize = getResourceSize(resourceName);

    try {
      InputStream in = resourceURL.openStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      byte[] buf = new byte[4096];

      while(true) {
	int len = in.read(buf, 0, buf.length);
	if(len == -1)
	  break;
	out.write(buf, 0, len);
      }

      if(filesize != out.size()) {
	throw new IOException("File size is invalid!");
      }

      resourceBytes = out.toByteArray();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Error reading (" + resourceName + ") " + 
	 "from plugin (" + pPluginInfo + ") " + 
	 "(" + ex.getMessage() + ")!");
    }

    if(resourceBytes == null) {
      throw new PipelineException
	("Error reading (" + resourceName + ") " + 
	 "from plugin (" + pPluginInfo + ")!");
    }

    return resourceBytes;
  }

  /**
   * Reads an Image resource using ImageIO.read(URL)
   */
  private BufferedImage
  readImage
  (
   String resourceName
  )
    throws PipelineException
  {
    URL resourceURL = getResource(resourceName);

    if(resourceURL == null) {
      throw new PipelineException
	("There is no resource (" + resourceName + ") " + 
	 "for plugin (" + pPluginInfo + ")!");
    }

    BufferedImage image = null;
    try {
      image = ImageIO.read(resourceURL);
    }
    catch(IOException ex) {
      throw new PipelineException
	("Error reading (" + resourceName + ") " + 
	 "from plugin (" + pPluginInfo + ")!");
    }

    if(image == null) {
      throw new PipelineException
	("Error reading (" + resourceName + ") " + 
	 "from plugin (" + pPluginInfo + ")!");
    }

    return image;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 12630682509146213L;

  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * String to represent vendor/type/name/version
   */
  private String  pPluginInfo;
}

