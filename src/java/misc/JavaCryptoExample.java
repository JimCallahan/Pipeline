import javax.crypto.*;
import javax.crypto.spec.*;
import sun.misc.*;
/*
 * @author nikk
*/
class JavaCryptoExample 
{
  public static void 
  main
  (
   String args[]
  ) 
  {
    String instr = "My data string";
    String b64es;
    String decstr;
    byte[] encrypted;
    byte[] toEncrypt;
    byte[] fromEncrypt;
    byte[] es;
    // Create a new 128 bit key. 
    byte[] mykey = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 
		     0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50 };
    // Create a new 64 bit initialization vector
    byte[] iv = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 };
    // or
    // byte[] mykey = "ABCDEFGHIJKLMNOP".getBytes();
    // byte[] iv = "ABCDEFGH".getBytes();
    try {
      // timer counter
      long StartTime = System.currentTimeMillis();
      System.out.println("Start of Encryption Test");
      
      // encrypt
      //  Constructs a secret key from mykey using RC2 algorithm
      SecretKeySpec key = new SecretKeySpec(mykey, "RC2");
      
      // Constructs a parameter set for RC2 from the given effective key size 
      // (in bits) and IV.
      RC2ParameterSpec rc2Spec = new RC2ParameterSpec(128, iv);
      
      // Generates a Cipher object that implements the specified transformation.
      // *** Very time expensive call ***
      // for increase in speed, create object early
      Cipher cipher = Cipher.getInstance("RC2/CBC/PKCS5Padding");
      
      //
      long CipherTime = System.currentTimeMillis();
      long Test1Time = (CipherTime - StartTime);
      System.out.println("Cost of cypher is " + Test1Time + " ms");
      
      // Initializes this cipher with a key and a set of algorithm parameters.
      // *** time expensive call *** 
      cipher.init(Cipher.ENCRYPT_MODE, key, rc2Spec, null);
      
      long initTime = System.currentTimeMillis();
      long Test2Time = (initTime - CipherTime);
      System.out.println("Cost of init is " + Test2Time + " ms");
			
         // Get encrypted array of bytes.
         toEncrypt = instr.getBytes();
			
         // Encrypts  byte data in a single-part operation, or finishes a 
         // multiple-part operation
         encrypted = cipher.doFinal(toEncrypt);
			
         // Get the Key size in bits
         int kbit = rc2Spec.getEffectiveKeyBits();
         System.out.println("Effective key size is " + kbit + " bits.\n");
			
         // get the cipher algorithm
         String alg = cipher.getAlgorithm();
         System.out.println("Algorithm " + alg);
			
         // get the Key
         System.out.println("Input Key : " + new String(key.getEncoded()));
			
         // get the IV
         System.out.println("Input IV : " + new String(cipher.getIV()) + "\n");
			
         // Base 64 Encode the encrypted string
         b64es = new BASE64Encoder().encodeBuffer(encrypted);
         System.out.println("Base64 encrypted output : " + b64es);
			
         // decrypt
         // Convert base64 encrypted string to encrypted byte array 
         es = new BASE64Decoder().decodeBuffer(b64es);

         // Initializes this cipher with a key and a set of algorithm parameters.
         // ** dcipher.init(Cipher.DECRYPT_MODE, key, rc2Spec, null);
         cipher.init(Cipher.DECRYPT_MODE, key, rc2Spec, null);
			
         //decrypt byte array
         // ** fromEncrypt = dcipher.doFinal(es);
         fromEncrypt = cipher.doFinal(es);
			
         // convert byte array into string
         decstr = new String(fromEncrypt);
         System.out.println("Input string : " + instr);
         System.out.println("Output string : " + decstr);
			
         // System.out.println("Output string : " + new String(fromEncrypt));
         long EndTime = System.currentTimeMillis();
         long TotalTime = (EndTime - StartTime);
         System.out.println("Cost of encrypting 8 characters is " + TotalTime + " ms");
    } catch (Exception e) {
      System.out.println("Error : " + e);
    }
  }
}
