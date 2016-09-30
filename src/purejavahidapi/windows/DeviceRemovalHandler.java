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

import static purejavahidapi.windows.SetupApiLibrary.*;

import com.sun.jna.WString;

import purejavahidapi.shared.SyncPoint;
import purejavahidapi.windows.WinDef.*;
import static purejavahidapi.windows.WinDef.*;
import static purejavahidapi.windows.Kernel32Library.*;
import static purejavahidapi.windows.User32Library.*;
import static purejavahidapi.windows.WtsApi32Library.*;
import static purejavahidapi.windows.WindowsBackend.reportLastError;

public class DeviceRemovalHandler implements WindowProc {
	private WindowsBackend m_WindowsBackend;
	private SyncPoint m_StartupSync;

	public DeviceRemovalHandler(WindowsBackend windowsBackend) {
		m_WindowsBackend = windowsBackend;
		m_StartupSync = new SyncPoint(2);
		Runnable threadRunnable = new Runnable() {
			public void run() {
				WString wndClassName = new WString("WindowClass");
				HMODULE hInst = GetModuleHandle(null);
				if (hInst == null)
					reportLastError();

				WNDCLASSEX wndClassEx = new WNDCLASSEX();
				wndClassEx.hInstance = hInst;
				wndClassEx.lpfnWndProc = DeviceRemovalHandler.this;
				wndClassEx.lpszClassName = wndClassName;

				ATOM wndClassRef = RegisterClassEx(wndClassEx);
				if (wndClassRef == null)
					reportLastError();

				HWND hWnd = CreateWindowEx(WS_EX_TOPMOST, "WindowClass", "", 0, 0, 0, 0, 0, null, null, hInst, null);

				if (hWnd == null)
					reportLastError();

				if (!WTSRegisterSessionNotification(hWnd, NOTIFY_FOR_THIS_SESSION))
					reportLastError();

				DEV_BROADCAST_DEVICEINTERFACE notificationFilter = new DEV_BROADCAST_DEVICEINTERFACE();
				notificationFilter.dbcc_size = notificationFilter.size();
				notificationFilter.dbcc_devicetype = DBT_DEVTYP_DEVICEINTERFACE;
				notificationFilter.dbcc_classguid = GUID_DEVINTERFACE_USB_DEVICE;

				HDEVNOTIFY hDevNotify = RegisterDeviceNotification(hWnd, notificationFilter, DEVICE_NOTIFY_WINDOW_HANDLE);
				if (hDevNotify == null)
					reportLastError();

				m_StartupSync.waitAndSync();
				
				MSG msg = new MSG();
				while (GetMessage(msg, hWnd, 0, 0) != 0) {
					TranslateMessage(msg);
					DispatchMessage(msg);
				}

				if (!UnregisterDeviceNotification(hDevNotify))
					reportLastError();

				if (!WTSUnRegisterSessionNotification(hWnd))
					reportLastError();

				if (!UnregisterClass(wndClassRef /* "WindowClass" */, hInst))
					reportLastError();

				if (!DestroyWindow(hWnd))
					reportLastError();
			}
		};
		Thread thread = new Thread(threadRunnable, this.getClass().getSimpleName());
		thread.setDaemon(true);
		thread.start();
		m_StartupSync.waitAndSync();
	}

	public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
		switch (uMsg) {
			case WM_DESTROY: {
				PostQuitMessage(0);
				return new LRESULT(0);
			}
			case WM_DEVICECHANGE: {
				LRESULT lResult = this.onDeviceChange(wParam, lParam);
				return lResult != null ? lResult : DefWindowProc(hwnd, uMsg, wParam, lParam);
			}
			default:
				return DefWindowProc(hwnd, uMsg, wParam, lParam);
		}
	}

	protected LRESULT onDeviceChange(WPARAM wParam, LPARAM lParam) {
		switch (wParam.intValue()) {
			case DBT_DEVICEREMOVECOMPLETE:
				return onDeviceChangeRemoveComplete(lParam);
			default:
				break;
		}
		return null;
	}

	private void handleDeviceRemoval(String name) {
		HDEVINFO deviceInfoHandle = SetupDiCreateDeviceInfoList(null, null);
		if (deviceInfoHandle != INVALID_HANDLE_VALUE) {
			SP_DEVICE_INTERFACE_DATA deviceInterfaceData = new SP_DEVICE_INTERFACE_DATA();
			deviceInterfaceData.cbSize = deviceInterfaceData.size();
			if (SetupDiOpenDeviceInterface(deviceInfoHandle, name, 0, deviceInterfaceData)) {
				SP_DEVINFO_DATA deviceInfoData = new SP_DEVINFO_DATA();
				deviceInfoData.cbSize = deviceInfoData.size();
				if (SetupDiEnumDeviceInfo(deviceInfoHandle, 0, deviceInfoData)) {
					char[] deviceIdChars = new char[255];
					int[] deviceIdLen = { 0 };
					if (SetupDiGetDeviceInstanceId(deviceInfoHandle, deviceInfoData, deviceIdChars, deviceIdChars.length, deviceIdLen)) {
						String deviceId=new String(deviceIdChars, 0, deviceIdLen[0] - 1);
						m_WindowsBackend.deviceRemoved(deviceId);
					}
				}
				if (!SetupDiDeleteDeviceInterfaceData(deviceInfoHandle, deviceInterfaceData))
					reportLastError();
			}
		}
		if (!SetupDiDestroyDeviceInfoList(deviceInfoHandle))
			reportLastError();
	}

	protected LRESULT onDeviceChangeArrivalOrRemoveComplete(LPARAM lParam, String action) {
		DEV_BROADCAST_HDR bhdr = new DEV_BROADCAST_HDR(lParam.longValue());
		switch (bhdr.dbch_devicetype) {
			case DBT_DEVTYP_DEVICEINTERFACE: {
				DEV_BROADCAST_DEVICEINTERFACE bdif = new DEV_BROADCAST_DEVICEINTERFACE(bhdr.getPointer());
				handleDeviceRemoval(bdif.get_dbcc_name());
				break;
			}
			default:
				return null;
		}
		return new LRESULT(1);
	}

	protected LRESULT onDeviceChangeRemoveComplete(LPARAM lParam) {
		return onDeviceChangeArrivalOrRemoveComplete(lParam, "Remove Complete");
	}

}
