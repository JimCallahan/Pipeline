// $Id: TimeService.java,v 1.5 2007/06/21 03:36:16 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   S E R V I C E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A very simple NTP client which bypasses the local system to determine the current time.
 */ 
class TimeService
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current time according to the given NTP server.
   * 
   * @param hostname [<B>in<B>]
   *   The hostname of the NTP server.
   * 
   * @throws IOException 
   *   If unable to successfully query the NTP server.
   */ 
  public static long
  getTime
  (
   String hostname
  ) 
    throws IOException
  {
    try {
      DatagramSocket socket = new DatagramSocket();
      InetAddress address = InetAddress.getByName(hostname);
      
      byte[] buf = new byte[48];
      buf[0] = (3 << 3 | 3);
      
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
      socket.send(packet);
      
      socket.setSoTimeout(5000);
      socket.receive(packet);
    
      return decode(packet.getData(), 40);
    }
    catch(Exception ex) {
      throw new IOException("Unable to get the time from (" + hostname + "):\n" + 
			    "  " + ex.getMessage() + "\n");
    }      
  }
    

  /**
   * Get the current time from one of a trusted list of NTP time servers.
   *
   * @throws IOException 
   *   If unable to successfully query any of the NTP servers.
   */ 
  public static long
  getTime() 
    throws IOException
  {
    StringBuilder buf = new StringBuilder();

    int wk;
    for(wk=0; wk<sNtpServers.length; wk++) {
      try {
	return getTime(sNtpServers[wk]);
      }
      catch(IOException ex) {
	buf.append(ex.getMessage());
      }
    }

    throw new IOException(buf.toString());
  }
  
  /**
   * Wheterh the current time is within the given interval. <P> 
   * 
   * If the time reported by a time server is outside the interval, check the next time
   * server until successful or all time servers have been exhasted.
   * 
   * @param start
   *   The timestamp of the start of the time interval.
   * 
   * @param end
   *   The timestamp of the end of the time interval.
   * 
   * @throws IOException 
   *   If unable to successfully query any of the NTP servers.
   */
  public static boolean
  isValid
  (
   long start, 
   long end
  ) 
    throws IOException
  {
    StringBuilder buf = new StringBuilder();

    int checked = 0;
    int wk;
    for(wk=0; wk<sNtpServers.length; wk++) {
      try {
	long stamp = getTime(sNtpServers[wk]);
	if((stamp > start) && (stamp < end)) 
	  return true;
	checked++;
      }
      catch(IOException ex) {
	buf.append(ex.getMessage());
      }
    }

    if(checked == 0) 
      throw new IOException(buf.toString());

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Convert from a UTC timestamp to a 64-bit unsigned NPT timestamp format.
   */
  private static void 
  encode
  (
   byte[] buf, 
   int idx, 
   long stamp
  ) 
  {
    double dstamp = ((double) (stamp + 2208988800L)) / 1000.0;

    int wk;
    for(wk=0; wk<8; wk++) {
      double base = Math.pow(2, (3-wk)*8);
      buf[idx+wk] = (byte) (dstamp / base);
      dstamp = dstamp - (double) (ubyteToShort(buf[idx+wk]) * base);
    }
		
    buf[idx+7] = (byte) (Math.random()*255.0);
  }

  /** 
   * Convert from a 64-bit unsigned NPT timestamp format to a UTC timestamp.
   */
  private static long
  decode
  (
   byte[] buf, 
   int idx
  ) 
  {
    double dstamp = 0.0;
    
    int wk;
    for(wk=0; wk<8; wk++) 
      dstamp += ubyteToShort(buf[idx+wk]) * Math.pow(2, (3-wk)*8);
    
    return (long) ((dstamp - 2208988800.0) * 1000.0);
  }

  /**
   * Convert from a unsigned byte to a short.
   */
  private static short 
  ubyteToShort
  ( 
   byte b
  ) 
  {
    if((b & 0x80)==0x80) 
      return (short) (128 + (b & 0x7f));
    return (short) b;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The IP addresses of some trusted (Stratum One) NTP servers.
   */
  private static final String[] sNtpServers = {
    "204.152.184.72",          // clock.isc.org
    "128.9.176.30",            // timekeeper.isi.edu
    "216.218.254.202",         // time.heypete.com

    "76.169.239.34",           // t2.timegps.net
    "76.168.30.201",           // t4.timegps.net
    "76.169.237.141",          // t3.timegps.net
    
    "129.6.15.28",             // time-a.nist.gov
    "129.6.15.29",             // time-b.nist.gov
    
    "131.188.3.221",           // ntp1.fau.de
    "131.188.3.223",           // ntp3.fau.de
    
    "193.79.237.14",           // ntp1.nl.net
    "193.79.237.30",           // ntp2.nl.net
    
    "133.243.238.243",         // ntp.nict.jp
    
    "128.250.36.3",            // ntp1.cs.mu.OZ.AU
    "128.250.36.2",            // ntp0.cs.mu.OZ.AU
  };

}
