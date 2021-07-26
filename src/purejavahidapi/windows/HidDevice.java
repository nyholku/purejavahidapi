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
import static purejavahidapi.windows.Kernel32Library.*;
import static purejavahidapi.windows.SetupApiLibrary.*;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import purejavahidapi.shared.SyncPoint;
import purejavahidapi.windows.HidLibrary.HIDD_ATTRIBUTES;
import purejavahidapi.windows.HidLibrary.HIDP_CAPS;

public class HidDevice extends purejavahidapi.HidDevice {
	private int INPUT = 0;
	private int OUTPUT = 1;
	private int FEATURE = 2;
	private static final HANDLE INVALID_HANDLE_VALUE = null;
	private WindowsBackend m_Backend;
	private HANDLE[] m_Handles = new HANDLE[3];
	private int[] m_ReportLength = new int[3];
	private Memory[] m_Buffer = new Memory[3];
	private OVERLAPPED[] m_Overlapped = new OVERLAPPED[3];
	private int[][] m_Transfrd = new int[3][1];
	private byte[] m_InputReportBytes;
	private Thread m_Thread;
	private SyncPoint m_SyncStart;
	private SyncPoint m_SyncShutdown;
	private boolean m_StopThread;

	/*
	 * 			
	
	 */

	/* package */ HidDevice(purejavahidapi.HidDeviceInfo deviceInfo, WindowsBackend backend) {
		String[] paths = deviceInfo.getPath().split(HidDeviceInfo.SEPARATOR);

		for (int i = 0; i < paths.length; i++) {

			HANDLE handle = backend.openDeviceHandle(paths[i], false);

			if (handle == INVALID_HANDLE_VALUE)
				return;

			m_Backend = backend;
			HIDD_ATTRIBUTES attrib = new HIDD_ATTRIBUTES();
			attrib.Size = new NativeLong(attrib.size());
			HidD_GetAttributes(handle, attrib);
			m_HidDeviceInfo = (HidDeviceInfo) deviceInfo;
			boolean res;
			HIDP_PREPARSED_DATA[] ppd = new HIDP_PREPARSED_DATA[1];
			res = HidD_GetPreparsedData(handle, ppd);
			if (!res) {
				CloseHandle(handle);
				return;
			}
			HIDP_CAPS caps = new HIDP_CAPS();
			int nt_res = HidP_GetCaps(ppd[0], caps);
			if (nt_res != HIDP_STATUS_SUCCESS) {
				CloseHandle(handle);
				return;
			}
			if (caps.InputReportByteLength > 0) {
				m_ReportLength[INPUT] = caps.InputReportByteLength;
				m_Buffer[INPUT] = new Memory(m_ReportLength[INPUT] + 1);
				m_InputReportBytes = new byte[m_ReportLength[INPUT] + 1];
				m_Handles[INPUT] = handle;
			}
			if (caps.OutputReportByteLength > 0) {
				m_ReportLength[OUTPUT] = caps.OutputReportByteLength;
				m_Buffer[OUTPUT] = new Memory(m_ReportLength[OUTPUT] + 1);
				m_Handles[OUTPUT] = handle;
			}
			if (caps.FeatureReportByteLength > 0) {
				m_ReportLength[FEATURE] = caps.FeatureReportByteLength;
				m_Buffer[FEATURE] = new Memory(m_ReportLength[FEATURE] + 1);
				m_Handles[FEATURE] = handle;
			}
			HidD_FreePreparsedData(ppd[0]);
		}

		for (int i = 0; i < 3; i++) {
			m_Overlapped[i] = new OVERLAPPED();
			m_Overlapped[i].setAutoRead(false);
			m_Overlapped[i].setAutoWrite(false);
			m_Overlapped[i].hEvent = CreateEvent(null, false, false, null);
			m_Overlapped[i].writeField("hEvent");
		}

		m_SyncStart = new SyncPoint(2);
		m_SyncShutdown = new SyncPoint(2);

		m_Thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runReadOnBackground();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, m_HidDeviceInfo.getPath());
		m_Backend.addDevice(m_HidDeviceInfo.getDeviceId(), this);
		m_Open = true;
		if (m_ReportLength[INPUT] > 0) {
			m_Thread.start();
			m_SyncStart.waitAndSync();
		}

	}

	@Override
	synchronized public void close() {
		if (!m_Open)
			throw new IllegalStateException("device not open");

		m_StopThread = true;
		if (m_ReportLength[INPUT] > 0) {
			CancelIoEx(m_Handles[INPUT], null);
			m_Thread.interrupt();
			m_SyncShutdown.waitAndSync();
		}
		for (int i = 0; i < 3; i++)
			CloseHandle(m_Handles[i]);
		m_Backend.removeDevice(m_HidDeviceInfo.getDeviceId());
		m_Open = false;
	}

	@Override
	synchronized public int setOutputReport(byte reportId, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (m_ReportLength[OUTPUT] <= 0)
			throw new IllegalArgumentException("this device supportst no output reports");
		// In Windows writeFile() to HID device data has to be preceded with the report
		// number, regardless
		m_Buffer[OUTPUT].write(0, new byte[] { reportId }, 0, 1);
		m_Buffer[OUTPUT].write(1, data, 0, length);
		// In windows always attempt to write as many bytes as there are in the longest
		// report plus one for the report number (even if zero ie not used)
		if (!WriteFile(m_Handles[OUTPUT], m_Buffer[OUTPUT], m_ReportLength[OUTPUT], m_Transfrd[OUTPUT], m_Overlapped[OUTPUT])) {
			if (GetLastError() != ERROR_IO_PENDING) {
				// WriteFile() failed. Return error.
				// register_error(dev, "WriteFile");
				System.out.println("write WriteFile failed " + GetLastError());
				return -1;
			}

			if (!GetOverlappedResult(m_Handles[OUTPUT], m_Overlapped[OUTPUT], m_Transfrd[OUTPUT], true/* wait */)) {
				System.out.println("write GetOverlappedResult failed " + GetLastError());
				// The Write operation failed.
				// register_error(dev, "WriteFile");
				return 0;
			}
		}

		return m_Transfrd[OUTPUT][0] - 1;
	}

	@Override
	@Deprecated
	synchronized public int setFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (!HidD_SetFeature(m_Handles[FEATURE], data, length)) {
			// register_error(dev, "HidD_SetFeature");
			return -1;
		}

		return length;
	}

	@Override
	@Deprecated
	synchronized public int getFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (false) { // can't use this as it will not return the size of the report
			if (!HidD_GetFeature(m_Handles[FEATURE], data, length)) {
				// register_error(dev, "HidD_SetFeature");
				System.out.println(GetLastError());
				return -1;
			}
		} else {
			if (!DeviceIoControl(m_Handles[FEATURE], IOCTL_HID_GET_FEATURE, m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Transfrd[FEATURE], m_Overlapped[FEATURE])) {
				// System.out.println(GetLastError());
				if (GetLastError() != ERROR_IO_PENDING)
					return -1;
			}

			if (!GetOverlappedResult(m_Handles[FEATURE], m_Overlapped[FEATURE], m_Transfrd[FEATURE], true/* wait */))
				return -1;
			m_Buffer[FEATURE].read((long) 1, data, 0, m_Transfrd[FEATURE][0]);
			return m_Transfrd[FEATURE][0];
		}
		return -1; // Eclipse says this is unreachable (it is), but won't compile without it ... go
					// figure

	}

	synchronized public int getFeatureReport(int reportId, byte[] data, int length) {
		return getFeatureReport((byte) reportId, data, length);
	}

	synchronized public int getFeatureReport(byte reportId, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (false) { // can't use this as it will not return the size of the report
			if (!HidD_GetFeature(m_Handles[FEATURE], data, length)) {
				// register_error(dev, "HidD_SetFeature");
				return -1;
			}
		} else {
			m_Buffer[FEATURE].write(0, new byte[] { reportId }, 0, 1);
			if (!DeviceIoControl(m_Handles[FEATURE], IOCTL_HID_GET_FEATURE, m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Transfrd[FEATURE], m_Overlapped[FEATURE])) {
				// System.out.println(GetLastError());
				if (GetLastError() != ERROR_IO_PENDING)
					return -1;
			}

			if (!GetOverlappedResult(m_Handles[FEATURE], m_Overlapped[FEATURE], m_Transfrd[FEATURE], true/* wait */))
				return -1;
			m_Buffer[FEATURE].read((long) 1, data, 0, m_Transfrd[FEATURE][0]);
			return m_Transfrd[FEATURE][0];
		}
		return -1; // Eclipse says this is unreachable (it is), but won't compile without it ... go
					// figure

	}

	private void runReadOnBackground() {
		m_SyncStart.waitAndSync();
		while (!m_StopThread) {
			m_Transfrd[INPUT][0] = 0;
			// KOE ResetEvent(m_ReportOverlapped.hEvent);

			// In Windos ReadFile() from a HID device Windows expects us to attempt to read
			// as much bytes as there are
			// in the longest report plus one for the report number (even if not used) and
			// the data is always
			// preceded with the report number (even if not used in case of which it is
			// zero)
			if (!ReadFile(m_Handles[INPUT], m_Buffer[INPUT], m_ReportLength[INPUT], m_Transfrd[INPUT], m_Overlapped[INPUT])) {
				if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
					break; // early exit if the device disappears
				if (GetLastError() != ERROR_IO_PENDING) {
					System.out.println("ReadFile failed with GetLastError()==" + GetLastError());
					CancelIo(m_Handles[INPUT]);
					break;
				}

				if (!GetOverlappedResult(m_Handles[INPUT], m_Overlapped[INPUT], m_Transfrd[INPUT], true/* wait */)) {
					if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
						break; // early exit if the device disappears
					System.out.println("GetOverlappedResult failed with GetLastError()==" + GetLastError());
				}
			}

			if (m_Transfrd[INPUT][0] > 0) {
				byte reportId = m_Buffer[INPUT].getByte(0);
				m_Transfrd[INPUT][0]--;

				m_Buffer[INPUT].read(1, m_InputReportBytes, 0, m_Transfrd[INPUT][0]);

				if (m_InputReportListener != null)
					m_InputReportListener.onInputReport(this, reportId, m_InputReportBytes, m_Transfrd[INPUT][0]);
			}

		}
		m_SyncShutdown.waitAndSync();
	}

	@Override
	public int setFeatureReport(byte reportId, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (false) {
			byte[] buf = new byte[length + 1];
			buf[0] = reportId;
			System.arraycopy(data, 0, buf, 1, length);
			if (!HidD_SetFeature(m_Handles[FEATURE], buf, length)) {
				return -1;
			}
			return length;
		}

		m_Buffer[FEATURE].write(0, new byte[] {reportId}, 0, 1);
		m_Buffer[FEATURE].write(1, data, 0, length);

		if (!DeviceIoControl(m_Handles[FEATURE], IOCTL_HID_SET_FEATURE, m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Buffer[FEATURE], m_ReportLength[FEATURE], m_Transfrd[FEATURE], m_Overlapped[FEATURE])) {
			System.out.println(GetLastError());
			if (GetLastError() != ERROR_IO_PENDING)
				return -1;
		}

		if (!GetOverlappedResult(m_Handles[FEATURE], m_Overlapped[FEATURE], m_Transfrd[FEATURE], true/* wait */))
			return -1;
		return m_Transfrd[FEATURE][0];
	}
}