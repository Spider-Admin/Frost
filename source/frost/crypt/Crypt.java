/*
  Crypt.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package frost.crypt;
import java.io.File;

/**
 * facade for verifying/signing messages in frost
 */
 public interface Crypt {
 
 	/**
	 * [0] private, [1] public
	 */
 	public String[] generateKeys(); 
	
	/**
	 * generates a detached signature on a String.
	 * @param message the string to be signed
	 * @param key the private key
	 * @return a detached signature not ascii armored
	 */
	public String detachedSign(String message,String key);
	
	public String detachedSign(byte [] message,String key);
	
	/**
	 *  Verifies a String with a detached signature
	 * @param message the message to be verified
	 * @param key the key used for verification
	 * @param sig the binary signature
	 * @return whether the verification was successful
	 */
    public boolean detachedVerify(String message, String key, String sig);
    public boolean detachedVerify(byte [] plaintext, String key, String sig);

	/**
	 * checksum of a string
	 */
	public String digest(String what);
	public String digest(File which);
	
    public byte[] encrypt(byte[] what, String publicKey);
	public byte [] decrypt(byte [] what, String privateKey);
	
	public String encode64(String what);
	
	public String decode64(String what);
 }
