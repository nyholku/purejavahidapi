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

import static com.sun.jna.platform.win32.WinBase.INVALID_HANDLE_VALUE;
import static purejavahidapi.windows.HidLibrary.HidD_FreePreparsedData;
import static purejavahidapi.windows.HidLibrary.HidD_GetAttributes;
import static purejavahidapi.windows.HidLibrary.HidD_GetPreparsedData;
import static purejavahidapi.windows.HidLibrary.HidD_SetFeature;
import static purejavahidapi.windows.HidLibrary.HidD_SetOutputReport;
import static purejavahidapi.windows.HidLibrary.HidP_GetCaps;
import static purejavahidapi.windows.Kernel32Library.*;
import static purejavahidapi.windows.SetupApiLibrary.HIDP_STATUS_SUCCESS;
//import static purejavahidapi.windows.WinDef.INVALID_HANDLE_VALUE;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import purejavahidapi.shared.SyncPoint;
import purejavahidapi.windows.HidLibrary.HIDD_ATTRIBUTES;
import purejavahidapi.windows.HidLibrary.HIDP_CAPS;
import purejavahidapi.windows.HidLibrary.HIDP_PREPARSED_DATA;
//import purejavahidapi.windows.WinDef.HANDLE;
//import purejavahidapi.windows.WinDef.OVERLAPPED;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class HidDevice extends purejavahidapi.HidDevice {
	private WindowsBackend m_Backend;
	private HANDLE m_Handle;
	private int m_OutputReportLength;
	private Memory m_OutputReportMemory;
	private OVERLAPPED m_OutputReportOverlapped;
	private int[] m_OutputReportBytesWritten;
	private int m_InputReportLength;
	private Thread m_Thread;
	private SyncPoint m_SyncStart;
	private SyncPoint m_SyncShutdown;
	private boolean m_StopThread;
	private boolean m_ForceControlOutput;

	/* package */ HidDevice(purejavahidapi.HidDeviceInfo deviceInfo, WindowsBackend backend) {
		HANDLE handle = WindowsBackend.openDeviceHandle(deviceInfo.getPath(), false);

		if (handle == INVALID_HANDLE_VALUE)
			return;

		m_Backend = backend;
		m_Handle = handle;
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
		m_OutputReportLength = caps.OutputReportByteLength;
		if (m_OutputReportLength > 0)
			m_OutputReportMemory = new Memory(m_OutputReportLength);
		m_OutputReportOverlapped = new OVERLAPPED();
		m_OutputReportBytesWritten = new int[] {
				0
		};

		m_InputReportLength = caps.InputReportByteLength;
		HidD_FreePreparsedData(ppd[0]);

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
		if (m_InputReportLength > 0) {
			m_Thread.start();
			m_SyncStart.waitAndSync();
		}

		m_ForceControlOutput = System.getProperty("purejavahidapi.forceControlOutput") != null;

		// bManualReset parameter has to be set to true to work with WaitForSingleObject
		m_OutputReportOverlapped.hEvent = CreateEvent(null, true, false, null);
	}

	@Override
	synchronized public void close() {
		if (!m_Open)
			throw new IllegalStateException("device not open");

		m_StopThread = true;
		if (m_InputReportLength > 0) {
			CancelIoEx(m_Handle, null);
			m_Thread.interrupt();
			m_SyncShutdown.waitAndSync();
		}
		CloseHandle(m_OutputReportOverlapped.hEvent);
		CloseHandle(m_Handle);
		m_Backend.removeDevice(m_HidDeviceInfo.getDeviceId());
		m_Open = false;
	}

	@Override
	synchronized public int setOutputReport(byte reportID, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (m_OutputReportLength == 0)
			throw new IllegalArgumentException("this device supports no output reports");
		// In Windows writeFile() to HID device data has to be preceded with the report
		// number, regardless
		m_OutputReportMemory.write(0, new byte[] {
				reportID
		}, 0, 1);
		m_OutputReportMemory.write(1, data, 0, length);

		if (!m_ForceControlOutput) {
			ResetEvent(m_OutputReportOverlapped.hEvent);
			m_OutputReportOverlapped.Internal = null;
			m_OutputReportOverlapped.InternalHigh = null;
			m_OutputReportOverlapped.Offset = 0;
			m_OutputReportOverlapped.OffsetHigh = 0;

			// In windows always attempt to write as many bytes as there are in the longest
			// report plus one for the report number (even if zero ie not used)
			if (!WriteFile(m_Handle, m_OutputReportMemory, m_OutputReportLength, null, m_OutputReportOverlapped)) {
				if (GetLastError() != ERROR_IO_PENDING) {
					// WriteFile() failed. Return error.
					// register_error(dev, "WriteFile");
					return -1;
				}
			}

			if (WAIT_OBJECT_0 != WaitForSingleObject(m_OutputReportOverlapped.hEvent, 1000)) {
				return -1;
			}

			// Update structure from native code
			m_OutputReportOverlapped.read();

			if (!GetOverlappedResult(m_Handle, m_OutputReportOverlapped, m_OutputReportBytesWritten, false/* don't need to wait */)) {
				// The Write operation failed.
				// register_error(dev, "WriteFile");
				return -1;
			}

			return m_OutputReportBytesWritten[0] - 1;
		} else {
			if (!HidD_SetOutputReport(m_Handle, m_OutputReportMemory.getByteArray(0, length + 1), length + 1)) {
				// HidD_SetOutputReport() failed. Return error.
				// register_error(dev, "HidD_SetOutputReport");
				return -1;
			}

			return length;
		}
	}


	@Override
	synchronized public int setFeatureReport(byte reportId, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		byte[] buffer = new byte[length + 1];
		buffer[0] = reportId;
		System.arraycopy(data, 0, buffer, 1, length);
		if (!HidD_SetFeature(m_Handle, buffer, length + 1)) {
			// register_error(dev, "HidD_SetFeature");
			return -1;
		}

		return length;
	}

	@Override
	synchronized public int setFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (!HidD_SetFeature(m_Handle, data, length)) {
			// register_error(dev, "HidD_SetFeature");
			return -1;
		}

		return length;
	}

	@Override
	synchronized public int getFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		int[] bytes = {
				0
		};

		OVERLAPPED ol = new OVERLAPPED();
		Pointer buffer = new Memory(data.length);
		if (!DeviceIoControl(m_Handle, IOCTL_HID_GET_FEATURE, buffer, length, buffer, length, bytes, ol)) {
			// System.out.println(GetLastError());
			if (GetLastError() != ERROR_IO_PENDING)
				return -1;
		}

		if (!GetOverlappedResult(m_Handle, ol, bytes, true/* wait */))
			return -1;
		int n = bytes[0] + 1;
		byte[] t = buffer.getByteArray(0, n);
		System.arraycopy(t, 0, data, 0, n);
		return n;
	}

	private void runReadOnBackground() {
		m_SyncStart.waitAndSync();

		int[] numBytesRead = { 0 };
		OVERLAPPED overlapped = new OVERLAPPED();
		overlapped.hEvent = CreateEvent(null, true, true, null);
		Memory readBuffer = new Memory(m_InputReportLength);

		while (!m_StopThread) {
			ResetEvent(overlapped.hEvent);
			numBytesRead[0] = 0;
			overlapped.Internal = null;
			overlapped.InternalHigh = null;
			overlapped.Offset = 0;
			overlapped.OffsetHigh = 0;

			// In Windows ReadFile() from a HID device Windows expects us to
			// attempt to read as much many as there are in the longest report
			// plus one for the report number (even if not used) and the data
			// is always preceded with the report number (even if not used in
			// case of which it is zero)
			if (!ReadFile(m_Handle, readBuffer, m_InputReportLength, numBytesRead, overlapped)) {
				if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
					break; // early exit if the device disappears
				if (GetLastError() != ERROR_IO_PENDING) {
					CancelIo(m_Handle);
					System.err.println("ReadFile failed with GetLastError()==" + GetLastError());
				}

				if (WAIT_OBJECT_0 != WaitForSingleObject(overlapped.hEvent, INFINITE)) {
					System.err.println("WaitForSingleObject failed with GetLastError()==" + GetLastError());
				}

				// Update structure from native code
				overlapped.read();

				if (!GetOverlappedResult(m_Handle, overlapped, numBytesRead, false/* don't need to wait */)) {
					if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
						break; // early exit if the device disappears
					if (m_StopThread && GetLastError() == ERROR_OPERATION_ABORTED)
						break; // on close
					System.err.println("GetOverlappedResult failed with GetLastError()==" + GetLastError());
				}
			}

			processInputReport(readBuffer, numBytesRead[0]);
		}

		CloseHandle(overlapped.hEvent);
		m_SyncShutdown.waitAndSync();
	}

	private void processInputReport(Memory readBuffer, int numBytesRead) {
		if (numBytesRead > 0) {
			byte reportID = readBuffer.getByte(0);
			int len = numBytesRead - 1;
			byte[] inputReport = new byte[len];
			readBuffer.read(1, inputReport, 0, len);
			if (m_InputReportListener != null) {
				m_InputReportListener.onInputReport(this, reportID, inputReport, len);
			}
		}
	}

	private void Log(String sMessage) {
		try {
			String fileName = "C:\\Temp\\bmx.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
			writer.write(sMessage + "\n");
			writer.close();
		} catch (Exception e) {

		}
	}

}
