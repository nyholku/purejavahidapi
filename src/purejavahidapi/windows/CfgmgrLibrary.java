/*
 * Copyright (c) 2016, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *  
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its 
 * contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package purejavahidapi.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

public class CfgmgrLibrary {
	public static int CR_SUCCESS = 0;
	static CfgmgrInterface INSTANCE = (CfgmgrInterface) Native.loadLibrary("CfgMgr32", CfgmgrInterface.class,W32APIOptions.UNICODE_OPTIONS);

	public interface CfgmgrInterface extends Library {
		int CM_Get_Parent(int[] pdnDevInst, int dnDevInst, int ulFlags);

		int CM_Get_Device_ID(int dnDevInst, char[] Buffer, int BufferLen, int ulFlags);

		int CM_Get_Device_ID_Size(int[] pulLen, int dnDevInst, int ulFlags);

	}

	public static int CM_Get_Parent(int[] pdnDevInst, int dnDevInst, int ulFlags) {
		return INSTANCE.CM_Get_Parent(pdnDevInst, dnDevInst, ulFlags);
	}

	public static int CM_Get_Device_ID(int dnDevInst, char[] Buffer, int BufferLen, int ulFlags) {
		return INSTANCE.CM_Get_Device_ID(dnDevInst, Buffer, BufferLen, ulFlags);
	}

	public static int CM_Get_Device_ID_Size(int[] pulLen, int dnDevInst, int ulFlags) {
		return INSTANCE.CM_Get_Device_ID_Size(pulLen, dnDevInst, ulFlags);
	}

}
