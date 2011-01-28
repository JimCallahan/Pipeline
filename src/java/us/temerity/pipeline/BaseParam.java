// $Id: BaseParam.java,v 1.9 2009/06/04 09:26:58 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all parameters associated with plugins.
 */
public abstract
class BaseParam
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected
  BaseParam() 
  {
    super();
  }
  
  /** 
   * Construct a parameter with the given name and description.
   * <p>
   * This constructor should only be used by classes that are not implementing 
   * the validate method, and therefore are not using the setValue functionality
   * of BaseParam.
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   */ 
  protected 
  BaseParam
  (
   String name,  
   String desc
  ) 
  {
    super(name, desc);

    if(pName.length() == 0) 
      throw new IllegalArgumentException
        ("Parameter names cannot be the empty string!");
    
    if(!isValidName(pName)) 
      throw new IllegalArgumentException
	("The parameter name (" + name + ") may contain only alphanumeric characters " + 
	 "without whitespace!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets a modified form of the name of this instance with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI to label fields and table columns in a more human 
   * friendly manner.
   * 
   * @see #getName
   */ 
  public final String
  getNameUI()
  {
    StringBuilder buf = new StringBuilder();
    char c[] = getName().toCharArray();
    int wk;
    buf.append(c[0]);
    if(c.length > 1) {
      for(wk=1; wk<(c.length-1); wk++) {
        if(Character.isUpperCase(c[wk]) && 
           (Character.isLowerCase(c[wk-1]) ||
            Character.isLowerCase(c[wk+1])))
	  buf.append(" ");
        
        buf.append(c[wk]);
      }
      buf.append(c[wk]);
    }

    return (buf.toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pName = stripInvalid(pName);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the given name contain only alphanumeric characters.
   */ 
  private boolean
  isValidName
  (
   String name
  ) 
  {
    return Identifiers.hasAlphaNumericChars(name);
  }

  /**
   * Strip any non-alphanumeric characters from the name.
   */ 
  private String
  stripInvalid
  (
   String name
  ) 
    throws GlueException
  {
    if(Identifiers.hasAlphaNumericChars(name))
      return name;
    
    StringBuilder buf = new StringBuilder(); 
    for(char c : name.toCharArray()) {
      if(Identifiers.isAlphaNumeric(c))
        buf.append(c);
    }
    
    String vname = buf.toString();
    if(vname.length() > 0)
      return vname;

    throw new GlueException
      ("Unable to convert invalid GLUE parameter name (" + name + ") to a legal name by " + 
       "stripping away non-alphanumeric characters!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -841968510233915472L;
}



