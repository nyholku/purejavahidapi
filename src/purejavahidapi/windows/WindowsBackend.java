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

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import purejavahidapi.shared.Backend;
import purejavahidapi.shared.Frontend;
import purejavahidapi.windows.HidLibrary.*;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;

import static purejavahidapi.windows.SetUpApiLibrary.*;
import static purejavahidapi.windows.Kernel32Library.*;
import static purejavahidapi.windows.HidLibrary.*;

public class WindowsBackend implements Backend {
	private Frontend m_Frontend;

	@Override
	public void init() {

	}

	@Override
	public void cleanup() {

	}

	static Kernel32Library.HANDLE open_device(String path, boolean enumerate) {
		Kernel32Library.HANDLE handle;
		int desired_access = (enumerate) ? 0 : (GENERIC_WRITE | GENERIC_READ);
		int share_mode = (enumerate) ? FILE_SHARE_READ | FILE_SHARE_WRITE : FILE_SHARE_READ;

		handle = CreateFileA(path, desired_access, share_mode, null, OPEN_EXISTING, FILE_FLAG_OVERLAPPED,// FILE_ATTRIBUTE_NORMAL,
				null);

		return handle;
	}

	void visit(String tab, HIDP_LINK_COLLECTION_NODE[] nodes, int n, HIDP_PREPARSED_DATA ppd) {
		System.out.println(tab + "node " + n + " no of children " + nodes[n].NumberOfChildren + " first child " + nodes[n].FirstChild + " next sibling " + nodes[n].NextSibling + " parent " + nodes[n].Parent);
	}

	void traverse(String tab, HIDP_LINK_COLLECTION_NODE[] nodes, int n, HIDP_PREPARSED_DATA ppd) {
		do {
			visit(tab, nodes, n, ppd);

			short[] plen = { 0 };
			boolean res = HidP_GetSpecificValueCaps(0, (short) 0, (short) n, (short) 0, null, plen, ppd);
			System.out.println(res + " " + plen[0]);
			int len = plen[0];
			if (len > 0) {
				HIDP_VALUE_CAPS[] valueCaps = new HIDP_VALUE_CAPS[len];
				// for (int i=0; i<len; i++)
				// valueCaps[i]=new HIDP_VALUE_CAPS();
				System.out.println();
				res = HidP_GetSpecificValueCaps(0, (short) 0, (short) n, (short) 0, valueCaps, plen, ppd);
				Pointer p = valueCaps[0].getPointer();
				for (int j = 0; j < valueCaps[0].size(); j++)
					System.out.printf("%02X %d\n", p.getByte(j), p.getByte(j));
				for (int j = 0; j < len; j++)
					System.out.println(" usage page " + valueCaps[j].UsagePage + " usage " + valueCaps[j].u.NotRange.Usage + " min " + valueCaps[j].LogicalMin.intValue() + " max " + valueCaps[j].LogicalMax.intValue());
			}

			if (nodes[n].FirstChild != 0)
				traverse(tab + "   ", nodes, nodes[n].FirstChild, ppd);
		} while (0 != (n = nodes[n].NextSibling));
	}

	@Override
	public List<purejavahidapi.HidDeviceInfo> enumerateDevices() {

		try {
			boolean res;
			List<purejavahidapi.HidDeviceInfo> list = new LinkedList<purejavahidapi.HidDeviceInfo>();

			// Windows objects for interacting with the driver.
			GUID InterfaceClassGuid = new GUID(0x4d1e55b2, 0xf16f, 0x11cf, 0x88, 0xcb, 0x00, 0x11, 0x11, 0x00, 0x00, 0x30);

			SP_DEVINFO_DATA devinfo_data = new SP_DEVINFO_DATA();
			SP_DEVICE_INTERFACE_DATA device_interface_data = new SP_DEVICE_INTERFACE_DATA();
			SP_DEVICE_INTERFACE_DETAIL_DATA_A device_interface_detail_data = null;
			HDEVINFO device_info_set = null;
			int device_index = 0;
			int i;

			// Initialize the Windows objects.
			devinfo_data.cbSize = devinfo_data.size();
			device_interface_data.cbSize = device_interface_data.size();

			// Get information for all the devices belonging to the HID class.
			device_info_set = SetupDiGetClassDevsA(InterfaceClassGuid, null, null, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);

			// Iterate over each device in the HID class, looking for the right
			// one.

			for (;;) {
				Kernel32Library.HANDLE devHandle = INVALID_HANDLE_VALUE;
				int[] required_size = { 0 };

				res = SetupDiEnumDeviceInterfaces(device_info_set, null, InterfaceClassGuid, device_index, device_interface_data);

				if (!res) {
					// A return of FALSE from this function means that
					// there are no more devices.
					break;
				}

				// Call with 0-sized detail size, and let the function
				// tell us how long the detail struct needs to be. The
				// size is put in &required_size.
				res = SetupDiGetDeviceInterfaceDetailA(device_info_set, device_interface_data, null, 0, required_size, null);
				if (!res && GetLastError() != ERROR_INSUFFICIENT_BUFFER) {
					// This is not supposed to happen ever but it would be good to know if it does
					throw new RuntimeException("SetupDiGetDeviceIntehrfaceDetailA resulted in error " + GetLastError());
				}

				// Allocate a long enough structure for
				// device_interface_detail_data.
				device_interface_detail_data = new SP_DEVICE_INTERFACE_DETAIL_DATA_A(required_size[0]);

				// Get the detailed data for this device. The detail data gives
				// us
				// the device path for this device, which is then passed into
				// CreateFile() to get a handle to the device.
				res = SetupDiGetDeviceInterfaceDetailA(device_info_set, device_interface_data, device_interface_detail_data, required_size[0], null, null);

				if (!res) { // This is not supposed to happen ever but it would be good to know if it does
					throw new RuntimeException("SetupDiGetDeviceIntehrfaceDetailA resulted in error " + GetLastError());
				}

				// Make sure this device is of Setup Class "HIDClass" and has a
				// driver bound to it.
				for (i = 0;; i++) {
					byte[] driver_name = new byte[256];

					// Populate devinfo_data. This function will return failure
					// when there are no more interfaces left.
					res = SetupDiEnumDeviceInfo(device_info_set, i, devinfo_data);
					if (!res) {
						if (GetLastError() == ERROR_NO_MORE_ITEMS)
							continue;
						else
							throw new RuntimeException("SetupDiEnumDeviceInfo resulted in error " + GetLastError());
					}

					res = SetupDiGetDeviceRegistryPropertyA(device_info_set, devinfo_data, SPDRP_CLASS, null, driver_name, driver_name.length, null);
					if (!res) {
						if (GetLastError() == ERROR_INVALID_DATA) // Invalid data is legitime from code point of view, maybe the device does not have this property or the device is faulty 
							continue;
						else
							throw new RuntimeException("SetupDiGetDeviceRegistryPropertyA for SPDRP_CLASS resulted in error " + GetLastError());
					}

					//if (strcmp(driver_name, "HIDClass") == 0) {
					if (true) {
						// if (strcmp(driver_name, "HIDClass") == 0) {
						// See if there's a driver bound.
						res = SetupDiGetDeviceRegistryPropertyA(device_info_set, devinfo_data, SPDRP_DRIVER, null, driver_name, driver_name.length, null);
						if (res) // ok, found a driver
							break;
						if (GetLastError() != ERROR_INVALID_DATA) // Invalid data is legitime from code point of view, maybe the device does not have this property or the device is faulty 
							throw new RuntimeException("SetupDiGetDeviceRegistryPropertyA for SPDRP_DRIVER resulted in error " + GetLastError());
					}
				}
				String path = new String(device_interface_detail_data.DevicePath, "ascii");
				devHandle = open_device(path, true);
				// Check validity of write_handle.
				if (devHandle == INVALID_HANDLE_VALUE) {
					break;
				}

				HIDD_ATTRIBUTES attrib = new HIDD_ATTRIBUTES();
				attrib.Size = new NativeLong(attrib.size());
				HidD_GetAttributes(devHandle, attrib);

				list.add(new HidDeviceInfo(path, devHandle, attrib));


				CloseHandle(devHandle);
				device_index++;
			}

			// Close the device information handle.
			SetupDiDestroyDeviceInfoList(device_info_set);
			return list;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public purejavahidapi.HidDevice openDevice(String path, Frontend frontend) {
		m_Frontend = frontend;
		HANDLE handle = open_device(path, false);

		// Check validity of write_handle.
		if (handle == INVALID_HANDLE_VALUE) {
			// Unable to open the device.
			// register_error(dev, "CreateFile");
			return null;
		}

		return new HidDevice(path, handle, frontend);
	}

}
