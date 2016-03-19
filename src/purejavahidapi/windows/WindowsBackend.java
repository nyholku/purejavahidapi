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

import java.util.LinkedList;
import java.util.List;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.shared.Backend;
import purejavahidapi.shared.Frontend;
import purejavahidapi.windows.HidLibrary.*;
import purejavahidapi.windows.SetupApiLibrary.HDEVINFO;
import purejavahidapi.windows.SetupApiLibrary.SP_DEVICE_INTERFACE_DATA;
import purejavahidapi.windows.SetupApiLibrary.SP_DEVINFO_DATA;
import purejavahidapi.windows.WinDef.HANDLE;
import static purejavahidapi.windows.CfgmgrLibrary.*;
import static purejavahidapi.windows.WinDef.INVALID_HANDLE_VALUE;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import static purejavahidapi.windows.SetupApiLibrary.*;
import static purejavahidapi.windows.Kernel32Library.*;
import static purejavahidapi.windows.HidLibrary.*;

public class WindowsBackend implements Backend {
	private final static String DEVICE_ID_SEPARATOR = "\u2022"; // Unicode buller
	private Frontend m_Frontend;
	private LinkedList<HidDevice> m_OpenDevices = new LinkedList<HidDevice>();

	@Override
	public void init() {
		new DeviceRemovalHandler(this);
	}

	@Override
	public void cleanup() {

	}

	/* package */void closeDevice(HidDevice device) {
		m_OpenDevices.remove(device);
	}

	/* package */void deviceRemoved(String deviceId) {
		for (HidDevice device : m_OpenDevices) {
			HidDeviceInfo info = device.getHidDeviceInfo();
			String path = info.getPath();
			String id = path.substring(path.indexOf(DEVICE_ID_SEPARATOR) + 1);
			if (deviceId.equals(id)) {
				DeviceRemovalListener listener = device.getDeviceRemovalListener();
				device.close();
				if (listener != null)
					listener.onDeviceRemoval(device);
			}
		}
	}

	/* package */static HANDLE openDeviceHandle(String path, boolean enumerate) {
		path = path.substring(0, path.indexOf(DEVICE_ID_SEPARATOR));

		HANDLE handle;
		int desired_access = (enumerate) ? 0 : (GENERIC_WRITE | GENERIC_READ);
		int share_mode =  FILE_SHARE_READ | FILE_SHARE_WRITE;

		handle = CreateFile(path, desired_access, share_mode, null, OPEN_EXISTING, FILE_FLAG_OVERLAPPED,null);

		return handle;
	}

	static public void reportLastError() {
		int rc = Native.getLastError();
		if (rc != 0)
			System.err.println("GetLastError() == " + rc);
	}

	@Override
	public List<purejavahidapi.HidDeviceInfo> enumerateDevices() {

		try {
			boolean res;
			List<purejavahidapi.HidDeviceInfo> list = new LinkedList<purejavahidapi.HidDeviceInfo>();

			GUID InterfaceClassGuid = new GUID(0x4d1e55b2, 0xf16f, 0x11cf, 0x88, 0xcb, 0x00, 0x11, 0x11, 0x00, 0x00, 0x30);

			SP_DEVINFO_DATA devinfo_data = new SP_DEVINFO_DATA();
			SP_DEVICE_INTERFACE_DATA device_interface_data = new SP_DEVICE_INTERFACE_DATA();
			SP_DEVICE_INTERFACE_DETAIL_DATA_A device_interface_detail_data = null;
			HDEVINFO device_info_set = null;
			int deviceIndex = 0;
			int i;

			// Initialize the Windows objects.
			devinfo_data.cbSize = devinfo_data.size();
			device_interface_data.cbSize = device_interface_data.size();

			// Get information for all the devices belonging to the HID class.
			device_info_set = SetupDiGetClassDevs(InterfaceClassGuid, null, null, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);

			// Iterate over each device in the HID class, looking for the right one.

			char[] deviceIdChars = new char[255];
			int[] deviceIdLen = { 0 };
			String deviceId = null;
			SetupDiEnumDeviceInfo(device_info_set, deviceIndex, devinfo_data);
			if (SetupDiGetDeviceInstanceId(device_info_set, devinfo_data, deviceIdChars, deviceIdChars.length, deviceIdLen)) {
				deviceId = new String(deviceIdChars);
			} else
				reportLastError();

			int[] parent = { devinfo_data.DevInst };
			while (CM_Get_Parent(parent, parent[0], 0) == 0) {
				int[] parentIdLen = { 0 };
				if (CM_Get_Device_ID_Size(parentIdLen, parent[0], 0) != CR_SUCCESS)
					reportLastError();
				parentIdLen[0]++;
				char[] parentIdChars = new char[parentIdLen[0]];
				if (CM_Get_Device_ID(parent[0], parentIdChars, parentIdLen[0], 0) != CR_SUCCESS)
					reportLastError();
				String parentId = new String(parentIdChars, 0, parentIdLen[0] - 1);
				if (parentId.startsWith("USB\\")) {
					deviceId = parentId;
					break;
				}
			}

			for (;;) {
				HANDLE devHandle = INVALID_HANDLE_VALUE;
				int[] required_size = { 0 };

				res = SetupDiEnumDeviceInterfaces(device_info_set, null, InterfaceClassGuid, deviceIndex, device_interface_data);

				if (!res)
					break;

				res = SetupDiGetDeviceInterfaceDetail(device_info_set, device_interface_data, null, 0, required_size, null);
				if (!res && GetLastError() != ERROR_INSUFFICIENT_BUFFER)
					throw new RuntimeException("SetupDiGetDeviceIntehrfaceDetailA resulted in error " + GetLastError());

				device_interface_detail_data = new SP_DEVICE_INTERFACE_DETAIL_DATA_A(required_size[0]);

				// get the device path
				res = SetupDiGetDeviceInterfaceDetail(device_info_set, device_interface_data, device_interface_detail_data, required_size[0], null, null);

				if (!res)
					throw new RuntimeException("SetupDiGetDeviceInterfaceDetail resulted in error " + GetLastError());

				// Make sure this device is of Setup Class "HIDClass" and has a driver bound to it.
				for (i = 0;; i++) {
					char[] driverNameChars = new char[256];
					res = SetupDiEnumDeviceInfo(device_info_set, i, devinfo_data);
					if (!res) {
						if (GetLastError() == ERROR_NO_MORE_ITEMS)
							break;
						else
							throw new RuntimeException("SetupDiEnumDeviceInfo resulted in error " + GetLastError());
					}

					res = SetupDiGetDeviceRegistryProperty(device_info_set, devinfo_data, SPDRP_CLASS, null, driverNameChars, driverNameChars.length, null);
					if (!res) {
						if (GetLastError() == ERROR_INVALID_DATA) // Invalid data is legitime from code point of view, maybe the device does not have this property or the device is faulty 
							continue;
						else
							throw new RuntimeException("SetupDiGetDeviceRegistryPropertyA for SPDRP_CLASS resulted in error " + GetLastError());
					}

					int driverNameLen = 0;
					while (driverNameChars[driverNameLen++] != 0)
						;
					String drivername = new String(driverNameChars, 0, driverNameLen - 1);
					if ("HIDClass".equals(drivername)) {
						// if (strcmp(driver_name, "HIDClass") == 0) {
						// See if there's a driver bound.
						res = SetupDiGetDeviceRegistryProperty(device_info_set, devinfo_data, SPDRP_DRIVER, null, driverNameChars, driverNameChars.length, null);
						if (res) // ok, found a driver
							break;
						if (GetLastError() != ERROR_INVALID_DATA) // Invalid data is legitime from code point of view, maybe the device does not have this property or the device is faulty 
							throw new RuntimeException("SetupDiGetDeviceRegistryPropertyA for SPDRP_DRIVER resulted in error " + GetLastError());
					}
				}
				String path = new String(device_interface_detail_data.DevicePath);
				path += DEVICE_ID_SEPARATOR + deviceId;
				devHandle = openDeviceHandle(path, true);
				if (devHandle == INVALID_HANDLE_VALUE)
					break;

				HIDD_ATTRIBUTES attrib = new HIDD_ATTRIBUTES();
				attrib.Size = new NativeLong(attrib.size());
				HidD_GetAttributes(devHandle, attrib);

				list.add(new HidDeviceInfo(path, devHandle, attrib));

				CloseHandle(devHandle);
				deviceIndex++;
			}

			SetupDiDestroyDeviceInfoList(device_info_set);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public purejavahidapi.HidDevice openDevice(String path, Frontend frontend) {
		m_Frontend = frontend;
		HANDLE handle = openDeviceHandle(path, false);

		if (handle == INVALID_HANDLE_VALUE)
			return null;

		HidDevice device = new HidDevice(path, handle, frontend);
		m_OpenDevices.add(device);
		return device;
	}

}
