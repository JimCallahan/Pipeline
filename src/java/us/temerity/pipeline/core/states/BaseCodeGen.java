// $Id: BaseCodeGen.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   C O D E   G E N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for all Java code generation classes.
 */ 
public 
class BaseCodeGen
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BaseCodeGen() 
  {}


  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a string consisting the the given character repeated N number of times.
   */ 
  public String
  repeat
  (
   char c,
   int size
  ) 
  {
    StringBuilder buf = new StringBuilder();
    int wk;
    for(wk=0; wk<size; wk++) 
      buf.append(c);
    return buf.toString();
  }

  /**
   * Generate a horizontal bar.
   */ 
  public String
  bar
  (
   int size
  ) 
  {
    return repeat('-', size);
  }

  /**
   * Pad the given string so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str, 
   char c,
   int size
  ) 
  {
    return (str + repeat(c, Math.max(0, size - str.length())));
  }

  /**
   * Pad the given string with spaces so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str,
   int size
  ) 
  {
    return pad(str, ' ', size);
  }

  /**
   * Generate N spaces. 
   */ 
  public String
  pad
  (
   int size
  ) 
  {
    return repeat(' ', size);
  }
  
  /** 
   * Create spaces to indent to the given level.
   */ 
  public String 
  indent
  (
   int level
  ) 
  {
    return pad(level*2);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create a header banner.
   */ 
  public String 
  genHeader
  (
   String title, 
   int level
  ) 
  {
    StringBuilder buf = new StringBuilder();

    buf.append(indent(level) + genBar(sLineLength-level*2)); 
     
    buf.append(indent(level) + "/*  ");
    char cs[] = title.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) 
      buf.append(" " + cs[wk]);
    buf.append(pad(sLineLength - 6 - level*2 - cs.length*2) + "*/\n");

    buf.append(indent(level) + genBar(sLineLength-level*2)); 

    return buf.toString();
  }
  
  /** 
   * Create a header banner.
   */ 
  public String 
  genHeader
  (
   String title
  ) 
  {
    return genHeader(title, 0);
  }

  /** 
   * Create a divider bar comment.
   */ 
  public String 
  genBar
  (
   int size
  ) 
  {
    return ("/*" + bar(size-4) + "*/\n");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Max number of characters in a line of generated code.
   */ 
  protected static final int sLineLength = 94;

}



