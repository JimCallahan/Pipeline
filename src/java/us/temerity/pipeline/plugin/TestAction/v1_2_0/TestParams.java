// $Id: TestParams.java,v 1.2 2007/07/31 14:58:40 jim Exp $

package us.temerity.pipeline.plugin.TestAction.v1_2_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

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

    {
      ActionParam param = 
	new Color3dActionParam("AnotherColor3d",
                               "A color parameter.", 
                               new Color3d(1.0, 0.7, 0.1)); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new Tuple2iActionParam("AnotherTuple2i",
                               "A tuple parameter with (2) Integer components.", 
                               new Tuple2i(2, 3)); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new Tuple3iActionParam("AnotherTuple3i",
                               "A tuple parameter with (3) Integer components.", 
                               new Tuple3i(3, 4, 5)); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new Tuple2dActionParam("AnotherTuple2d",
                               "A tuple parameter with (2) Double components.", 
                               new Tuple2d(2.3, 3.4)); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new Tuple3dActionParam("AnotherTuple3d",
                               "A tuple parameter with (3) Double components.", 
                               new Tuple3d(2.3, 3.4, 5.6)); 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new Tuple4dActionParam("AnotherTuple4d",
                               "A tuple parameter with (4) Double components.", 
                               new Tuple4d(4.5, 5.6, 6.7, 7.8)); 
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
