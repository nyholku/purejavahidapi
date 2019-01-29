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

import static purejavahidapi.windows.HidLibrary.HidD_FreePreparsedData;
import static purejavahidapi.windows.HidLibrary.HidD_GetManufacturerString;
import static purejavahidapi.windows.HidLibrary.HidD_GetPreparsedData;
import static purejavahidapi.windows.HidLibrary.HidD_GetProductString;
import static purejavahidapi.windows.HidLibrary.HidD_GetSerialNumberString;
import static purejavahidapi.windows.HidLibrary.HidP_GetCaps;
import static purejavahidapi.windows.SetupApiLibrary.HIDP_STATUS_SUCCESS;

import com.sun.jna.Memory;

import purejavahidapi.windows.HidLibrary.HIDD_ATTRIBUTES;
import purejavahidapi.windows.HidLibrary.HIDP_CAPS;
import purejavahidapi.windows.HidLibrary.HIDP_PREPARSED_DATA;
import purejavahidapi.windows.WinDef.HANDLE;

/* package*/class HidDeviceInfo extends purejavahidapi.HidDeviceInfo {

	public HidDeviceInfo(String path, String deviceId, HANDLE handle, HIDD_ATTRIBUTES attrib) {
		try {
			m_Path = path;
			m_DeviceId = deviceId;
			m_VendorId = attrib.VendorID;
			m_ProductId = attrib.ProductID;

			HIDP_CAPS caps = new HIDP_CAPS();

			// Get the Usage Page and Usage for this device.
			HIDP_PREPARSED_DATA[] ppd = new HIDP_PREPARSED_DATA[1];
			if (HidD_GetPreparsedData(handle, ppd)) {
				if (HidP_GetCaps(ppd[0], caps) == HIDP_STATUS_SUCCESS) {
					m_UsagePage = caps.UsagePage;
				}

				HidD_FreePreparsedData(ppd[0]);
			}

			Memory wstr = new Memory(256);
			int sizeofWstr = (int)(wstr.size());

			if (HidD_GetSerialNumberString(handle, wstr, sizeofWstr))
				m_SerialNumberString = wstr.getWideString(0);
			if (HidD_GetManufacturerString(handle, wstr, sizeofWstr))
				m_ManufactureString = wstr.getWideString(0);
			if (HidD_GetProductString(handle, wstr, sizeofWstr))
				m_ProductString = wstr.getWideString(0);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}