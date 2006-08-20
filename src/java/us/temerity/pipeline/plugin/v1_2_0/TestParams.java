// $Id: TestParams.java,v 1.1 2006/08/20 05:51:41 jim Exp $

package us.temerity.pipeline.plugin.v1_2_0;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   P A R A M S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The per-source parameters for the TestAction
 */
public
class TestParams
{  
  public static TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
	new IntegerActionParam("AnotherInteger", 
			       "An integer parameter.",
			       123);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new DoubleActionParam("AnotherDouble", 
			      "An double parameter.",
			      123.456);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new EnumActionParam("AnotherEnum",
			    "An enum parameter.",
			    "Cyan", TestEnum.titles()); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new IntegerActionParam("SecondInteger", 
			       "A second integer parameter.",
			       2);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam("AnotherBoolean", 
			       "A boolean parameter.",
			       true);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam("AnotherLink",
			    "An upstream link parameter.",
			    null);
      params.put(param.getName(), param);
    }

    return params;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private enum TestEnum {
    Red,
    Yello, 
    Green, 
    Cyan, 
    Blue, 
    Purple, 
    Pink;

    public static ArrayList<String> 
    titles() 
    {
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("Red");
      colors.add("Yellow");
      colors.add("Green");
      colors.add("Cyan");
      colors.add("Blue");
      colors.add("Purple");
      colors.add("Pink");

      return colors; 
    }

    private static String sTitles[] = {
      "Red",
      "Yello",
      "Green",
      "Cyan",
      "Blue",
      "Purple", 
      "Pink"
    };
  }
  
}
