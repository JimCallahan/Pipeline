// $Id: Identifiers.java,v 1.2 2009/09/25 22:36:14 jlee Exp $

package us.temerity.pipeline;

import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   I D E N T I F I E R S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**                                                                                   
 * A collection of static utility methods for validating strings and characters used 
 * in various identifier names.
 */
public 
class Identifiers
{  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z"
   */ 
  public static boolean
  isAlpha
  (
   char c
  ) 
  {
    if(isAlphaStrict(c)) 
      return true;
   
    if(sIsStrict) 
      return false; 
    else if(Character.isLetter(c)) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated character (" + c + ") detected in identifier!", true));
      return true;
    }
    
    return false;
  }
  

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", "0"-"9"
   */ 
  public static boolean
  isAlphaNumeric
  (
   char c
  ) 
  {
    if(isAlphaNumericStrict(c)) 
      return true;

    if(sIsStrict) 
      return false; 
    else if(Character.isLetterOrDigit(c)) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated character (" + c + ") detected in identifier!", true));
      return true;
    }
    
    return false; 
  }

  /**
   * Whether the given character is one of the following characters: "_", "-", "~"
   */ 
  public static boolean
  isSeparator
  (
   char c
  ) 
  {
    return isSeparatorStrict(c); 
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~"
   */ 
  public static boolean
  isIdent
  (
   char c
  ) 
  {
    if(isIdentStrict(c)) 
      return true;

    if(sIsStrict) 
      return false; 
    else if(Character.isLetterOrDigit(c) || isSeparator(c)) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated character (" + c + ") detected in identifier!", true));
      return true;
    }
    
    return false; 
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", "."
   */ 
  public static boolean
  isExtendedIdent
  (
   char c
  ) 
  {
    if(isExtendedIdentStrict(c)) 
      return true;

    if(sIsStrict) 
      return false; 
    else if(Character.isLetterOrDigit(c) || isSeparator(c) || (c == '.')) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated character (" + c + ") detected in identifier!", true));
      return true;
    }
    
    return false; 
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", ".", "/"
   */ 
  public static boolean
  isPath
  (
   char c
  ) 
  {
    if(isPathStrict(c)) 
      return true;

    if(sIsStrict) 
      return false; 
    else if(Character.isLetterOrDigit(c) || isSeparator(c) || (c == '.') || (c == '/')) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated character (" + c + ") detected in identifier!", true));
      return true;
    }
    
    return false; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z" <P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  public static boolean
  hasAlphaChars
  (
   String str
  ) 
  {
    if(hasAlphaCharsStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false; 
    else if(str.length() > 0) {  
      for(char c : str.toCharArray()) {
        if(!Character.isLetter(c))
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated characters detected in the identifier (" + str + ")!", true)); 
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  public static boolean
  hasAlphaNumericChars
  (
   String str
  ) 
  { 
    if(hasAlphaNumericCharsStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false; 
    else if(str.length() > 0) {  
      for(char c : str.toCharArray()) {
        if(!Character.isLetterOrDigit(c))
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated characters detected in the identifier (" + str + ")!", true)); 
    }

    return true;
  }
  
  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  public static boolean
  hasIdentChars
  (
   String str
  ) 
  {
    if(hasIdentCharsStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false; 
    else if(str.length() > 0) {  
      for(char c : str.toCharArray()) {
        if(!(Character.isLetterOrDigit(c) || isSeparator(c))) 
          return false;
      }
        
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated characters detected in the identifier (" + str + ")!", true)); 
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  public static boolean
  hasExtendedIdentChars
  (
   String str
  ) 
  {
    if(hasExtendedIdentCharsStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false; 
    else if(str.length() > 0) {  
      for(char c : str.toCharArray()) {
        if(!(Character.isLetterOrDigit(c) || isSeparator(c) || (c == '.'))) 
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated characters detected in the identifier (" + str + ")!", true)); 
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", ".", "/"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  public static boolean
  hasPathChars
  (
   String str
  ) 
  {
    if(hasPathCharsStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false; 
    else if(str.length() > 0) {  
      for(char c : str.toCharArray()) {
        if(!(Character.isLetterOrDigit(c) || isSeparator(c) || (c == '.') || (c == '/'))) 
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
           ("Deprecated characters detected in the identifier (" + str + ")!", true)); 
    }

    return true;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  public static boolean
  isAlphaNumericIdent
  (
   String str
  ) 
  { 
    if(isAlphaNumericIdentStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false;
    if(str.length() > 0) {  
      char cs[] = str.toCharArray();
      if(!Character.isLetter(cs[0]))
        return false;
      
      int wk;
      for(wk=1; wk<cs.length; wk++) {
        if(!Character.isLetterOrDigit(cs[wk])) 
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
         ("Deprecated characters detected in the identifier (" + str + ")!", true)); 

      return true;
    }
   
    return false;
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  public static boolean
  isIdent
  (
   String str
  ) 
  {
    if(isIdentStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false;
    if(str.length() > 0) {  
      char cs[] = str.toCharArray();
      if(!Character.isLetter(cs[0]))
        return false;
      
      int wk;
      for(wk=1; wk<cs.length; wk++) {
        if(!(Character.isLetterOrDigit(cs[wk]) || isSeparator(cs[wk])))
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
         ("Deprecated characters detected in the identifier (" + str + ")!", true)); 

      return true;
    }

    return false;
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  public static boolean
  isExtendedIdent
  (
   String str
  ) 
  {
    if(isExtendedIdentStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false;
    if(str.length() > 0) {  
      char cs[] = str.toCharArray();
      if(!Character.isLetter(cs[0]))
        return false;
      
      int wk;
      for(wk=1; wk<cs.length; wk++) {
        if(!(Character.isLetterOrDigit(cs[wk]) || isSeparator(cs[wk])  || (cs[wk] == '.')))
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
         ("Deprecated characters detected in the identifier (" + str + ")!", true)); 

      return true;
    }

    return false;
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z", "0"-"9") 
   * followed by zero or more of the following characters: 
   * "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  public static boolean
  isExtendedNumerIdent
  (
   String str
  ) 
  {
    if(isExtendedNumerIdentStrict(str)) 
      return true; 

    if(sIsStrict) 
      return false;
    if(str.length() > 0) {  
      char cs[] = str.toCharArray();
      if(!Character.isLetterOrDigit(cs[0]))
        return false;
      
      int wk;
      for(wk=1; wk<cs.length; wk++) {
        if(!(Character.isLetterOrDigit(cs[wk]) || isSeparator(cs[wk])  || (cs[wk] == '.')))
          return false;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Warning,
         Exceptions.getFullMessage
         ("Deprecated characters detected in the identifier (" + str + ")!", true)); 

      return true;
    }

    return false;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z"
   */ 
  private static boolean
  isAlphaStrict
  (
   char c
  ) 
  {
    switch(c) {
    case 'a':
    case 'b':
    case 'c':
    case 'd':
    case 'e':
    case 'f':
    case 'g':
    case 'h':
    case 'i':
    case 'j':
    case 'k':
    case 'l':
    case 'm':
    case 'n':
    case 'o':
    case 'p':
    case 'q':
    case 'r':
    case 's':
    case 't':
    case 'u':
    case 'v':
    case 'w':
    case 'x':
    case 'y':
    case 'z':
    case 'A':
    case 'B':
    case 'C':
    case 'D':
    case 'E':
    case 'F':
    case 'G':
    case 'H':
    case 'I':
    case 'J':
    case 'K':
    case 'L':
    case 'M':
    case 'N':
    case 'O':
    case 'P':
    case 'Q':
    case 'R':
    case 'S':
    case 'T':
    case 'U':
    case 'V':
    case 'W':
    case 'X':
    case 'Y':
    case 'Z':
      return true;

    default:
      return false;
    }
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", "0"-"9"
   */ 
  private static boolean
  isAlphaNumericStrict
  (
   char c
  ) 
  {
    if(isAlphaStrict(c)) 
      return true;
    
    switch(c) {
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
      return true;

    default:
      return false;
    }
  }

  /**
   * Whether the given character is one of the following characters: "_", "-", "~"
   */ 
  private static boolean
  isSeparatorStrict
  (
   char c
  ) 
  {
    if(isAlphaStrict(c)) 
      return true;
    
    switch(c) {
    case '_':
    case '-':
    case '~':
      return true;

    default:
      return false;
    }
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~"
   */ 
  private static boolean
  isIdentStrict
  (
   char c
  ) 
  {
    return (isAlphaNumericStrict(c) || isSeparatorStrict(c));
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", "."
   */ 
  private static boolean
  isExtendedIdentStrict
  (
   char c
  ) 
  {
    return (isIdentStrict(c) || (c == '.'));
  }

  /**
   * Whether the given character is one of the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", ".", "/"
   */ 
  private static boolean
  isPathStrict
  (
   char c
  ) 
  {
    return (isExtendedIdentStrict(c) || (c == '/'));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z" <P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  private static boolean
  hasAlphaCharsStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return true;

    for(char c : str.toCharArray()) {
      if(!isAlphaStrict(c)) 
        return false;
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  private static boolean
  hasAlphaNumericCharsStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return true;

    for(char c : str.toCharArray()) {
      if(!isAlphaNumericStrict(c)) 
        return false;
    }

    return true;
  }
  
  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  private static boolean
  hasIdentCharsStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return true;

    for(char c : str.toCharArray()) {
      if(!isIdentStrict(c)) 
        return false;
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  private static boolean
  hasExtendedIdentCharsStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return true;

    for(char c : str.toCharArray()) {
      if(!isExtendedIdentStrict(c)) 
        return false;
    }

    return true;
  }

  /**
   * Whether the given string contains only the following characters: "a"-"z", "A"-"Z", 
   * "0"-"9", "_", "-", "~", ".", "/"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>true</CODE>.
   */ 
  private static boolean
  hasPathCharsStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return true;

    for(char c : str.toCharArray()) {
      if(!isPathStrict(c)) 
        return false;
    }

    return true;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  private static boolean
  isAlphaNumericIdentStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return false;

    char cs[] = str.toCharArray();
    if(!isAlphaStrict(cs[0]))
      return false;

    int wk;
    for(wk=1; wk<cs.length; wk++) {
      if(!isAlphaNumericStrict(cs[wk])) 
        return false;
    }

    return true; 
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~"<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  private static boolean
  isIdentStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return false;

    char cs[] = str.toCharArray();
    if(!isAlphaStrict(cs[0]))
      return false;

    int wk;
    for(wk=1; wk<cs.length; wk++) {
      if(!isIdentStrict(cs[wk])) 
        return false;
    }

    return true; 
   
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z") followed by zero or 
   * more of the following characters: "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  private static boolean
  isExtendedIdentStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return false;

    char cs[] = str.toCharArray();
    if(!isAlphaStrict(cs[0]))
      return false;

    int wk;
    for(wk=1; wk<cs.length; wk++) {
      if(!isExtendedIdentStrict(cs[wk])) 
        return false;
    }

    return true; 
  }

  /**
   * Whether the given string starts with a letter ("a"-"z", "A"-"Z", "0"-"9") 
   * followed by zero or more of the following characters: 
   * "a"-"z", "A"-"Z", "0"-"9", "_", "-", "~", "."<P> 
   * 
   * An empty string will cause this predicate to return <CODE>false</CODE>.
   */ 
  private static boolean
  isExtendedNumerIdentStrict
  (
   String str
  ) 
  {
    if(str.length() == 0) 
      return false;

    char cs[] = str.toCharArray();
    if(!isAlphaNumericStrict(cs[0]))
      return false;

    int wk;
    for(wk=1; wk<cs.length; wk++) {
      if(!isExtendedIdentStrict(cs[wk])) 
        return false;
    }

    return true; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the identifier tests should be performed using the new stricter rules.
   */ 
  private static final boolean sIsStrict = false;

}



