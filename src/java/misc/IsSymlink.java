import us.temerity.pipeline.*;
import java.io.*;

public 
class IsSymlink {
  public static void main(String args[]) {
    for(String s : args) {
      File file = new File(s);
      try {
        System.out.print(s + " " + (NativeFileSys.isSymlink(file) ? "IS a symlink." : "is NOT a symlink.") + "\n");
      }
      catch(IOException ex) {
        System.out.print
          ("Unable to determine whether " + s + " is a symlink:\n" + ex.getMessage() + "\n"); 
      }
    }
  }
}
