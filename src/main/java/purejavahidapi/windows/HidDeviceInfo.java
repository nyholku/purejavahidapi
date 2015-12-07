/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
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

import static purejavahidapi.windows.HidLibrary.*;
import static purejavahidapi.windows.SetUpApiLibrary.HIDP_STATUS_SUCCESS;

import java.nio.ByteBuffer;

import purejavahidapi.windows.HidLibrary.HIDD_ATTRIBUTES;
import purejavahidapi.windows.HidLibrary.HIDP_CAPS;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class HidDeviceInfo implements purejavahidapi.HidDeviceInfo {
	private String m_Path;
	private short m_VendorId;
	private short m_ProductId;
	private short m_ReleaseNumber;
	private short m_UsagePage;
	private String m_SerialNumberString;
	private String m_ManufactureString;
	private String m_ProductString;

	@Override
	public String getPath() {
		return m_Path;
	}

	@Override
	public short getVendorId() {
		return m_VendorId;
	}

	@Override
	public short getProductId() {
		return m_ProductId;
	}

	@Override
	public short getReleaseNumber() {
		return m_ReleaseNumber;
	}

	@Override
	public String getManufacturerString() {
		return m_ManufactureString;
	}

	@Override
	public String getProductString() {
		return m_ProductString;
	}

	@Override
	public String getSerialNumberString() {
		return m_SerialNumberString;
	}

	@Override
	public short getUsagePage() {
		return m_UsagePage;
	}

	public HidDeviceInfo(String path,Kernel32Library.HANDLE handle, HIDD_ATTRIBUTES attrib) {
		try {
			m_Path = path;
			m_VendorId = attrib.VendorID;
			m_ProductId = attrib.ProductID;
			m_ReleaseNumber = attrib.VersionNumber;

			HIDP_CAPS caps = new HIDP_CAPS();

			// Get the Usage Page and Usage for this device.
			HIDP_PREPARSED_DATA[] ppd = new HIDP_PREPARSED_DATA[1];
			if (HidD_GetPreparsedData(handle, ppd)) {
				if (HidP_GetCaps(ppd[0], caps) == HIDP_STATUS_SUCCESS) {
					m_UsagePage = caps.UsagePage;
				}

				HidD_FreePreparsedData(ppd[0]);
			}

			byte[] wstr = new byte[256];
			int sizeofWstr = wstr.length;
			ByteBuffer b=ByteBuffer.wrap(wstr);

			if (HidD_GetSerialNumberString(handle, wstr, sizeofWstr))
				m_SerialNumberString = Native.toString(wstr, "utf-16le");
			if (HidD_GetManufacturerString(handle, wstr, sizeofWstr))
				m_ManufactureString =  Native.toString(wstr, "utf-16le");
			if (HidD_GetProductString(handle, wstr, sizeofWstr))
				m_ProductString =  Native.toString(wstr, "utf-16le");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}