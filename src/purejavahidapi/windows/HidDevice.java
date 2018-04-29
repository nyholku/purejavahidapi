/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * Copyright (c) 2018, Nicholas Saney / Chairosoft
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notices, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notices, this
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
import static purejavahidapi.windows.WinDef.INVALID_HANDLE_VALUE;

import com.sun.jna.*;

import purejavahidapi.dataparser.Capability;
import purejavahidapi.dataparser.HidDataParser;
import purejavahidapi.dataparser.ParsedReportDataItem;
import purejavahidapi.shared.DataDump;
import purejavahidapi.shared.SyncPoint;
import purejavahidapi.windows.HidLibrary.HIDD_ATTRIBUTES;
import purejavahidapi.windows.HidLibrary.HIDP_CAPS;
import purejavahidapi.windows.WinDef.HANDLE;
import purejavahidapi.windows.WinDef.OVERLAPPED;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class HidDevice extends purejavahidapi.HidDevice {
	private WindowsBackend m_Backend;
	private HANDLE m_Handle;
	private int m_OutputReportLength;
	private Memory m_OutputReportMemory;
	private OVERLAPPED m_OutputReportOverlapped;
	private int[] m_OutputReportBytesWritten;
	private int m_InputReportLength;
	private OVERLAPPED m_InputReportOverlapped = new OVERLAPPED();
	private Memory m_InputReportMemory;
	private byte[] m_InputReportBytes;
	private int[] m_InputReportBytesRead = { 0 };
	private Thread m_Thread;
	private SyncPoint m_SyncStart;
	private SyncPoint m_SyncShutdown;
	private boolean m_StopThread;
	private boolean m_ForceControlOutput;
	private HIDP_PREPARSED_DATA m_PreparsedData;

	/* package */HidDevice(purejavahidapi.HidDeviceInfo deviceInfo, WindowsBackend backend) {
		HANDLE handle = backend.openDeviceHandle(deviceInfo.getPath(), false);

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
		m_PreparsedData = ppd[0];
		HIDP_CAPS caps = new HIDP_CAPS();
		int nt_res = HidP_GetCaps(m_PreparsedData, caps);
		if (nt_res != HIDP_STATUS_SUCCESS) {
			CloseHandle(handle);
			return;
		}
		
		if (DUMP_CAPS_ON_OPEN) {
			dumpCapabilities(caps);
		}
		
		m_OutputReportLength = caps.OutputReportByteLength;
		if (m_OutputReportLength > 0)
			m_OutputReportMemory = new Memory(m_OutputReportLength);
		m_OutputReportOverlapped = new OVERLAPPED();
		m_OutputReportBytesWritten = new int[] { 0 };

		m_InputReportLength = caps.InputReportByteLength;
		m_InputReportOverlapped = new OVERLAPPED();
		if (m_InputReportLength > 0) {
			m_InputReportMemory = new Memory(m_InputReportLength);
			m_InputReportBytes = new byte[m_InputReportLength];
		}
		m_InputReportBytesRead = new int[] { 0 };

//		HidD_FreePreparsedData(m_PreparsedData); // moved to close

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
		HidD_FreePreparsedData(m_PreparsedData);
		CloseHandle(m_Handle);
		m_Backend.removeDevice(m_HidDeviceInfo.getDeviceId());
		m_Open = false;
	}

	@Override
	synchronized public int setOutputReport(byte reportID, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (m_OutputReportLength == 0)
			throw new IllegalArgumentException("this device supportst no output reports");
		// In Windows writeFile() to HID device data has to be preceded with the report number, regardless 
		m_OutputReportMemory.write(0, new byte[] { reportID }, 0, 1);
		m_OutputReportMemory.write(1, data, 0, length);

		if (!m_ForceControlOutput) {
			// In windows always attempt to write as many bytes as there are in the longest report plus one for the report number (even if zero ie not used)
			if (!WriteFile(m_Handle, m_OutputReportMemory, m_OutputReportLength, null, m_OutputReportOverlapped)) {
				if (GetLastError() != ERROR_IO_PENDING) {
					// WriteFile() failed. Return error.
					//register_error(dev, "WriteFile");
					return -1;
				}
			}

			if (!GetOverlappedResult(m_Handle, m_OutputReportOverlapped, m_OutputReportBytesWritten, true/* wait */)) {
				// The Write operation failed.
				//register_error(dev, "WriteFile");
				return 0;
			}

			return m_OutputReportBytesWritten[0] - 1;
		} else {
			if (!HidD_SetOutputReport(m_Handle, m_OutputReportMemory.getByteArray(0, length + 1), length + 1)) {
				// HidD_SetOutputReport() failed. Return error.
				//register_error(dev, "HidD_SetOutputReport");
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
			//register_error(dev, "HidD_SetFeature");
			return -1;
		}

		return length;
	}

	@Override
	synchronized public int setFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (!HidD_SetFeature(m_Handle, data, length)) {
			//register_error(dev, "HidD_SetFeature");
			return -1;
		}

		return length;
	}

	@Override
	synchronized public int getFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		if (false) { // can't use this as it will not return the size of the report
			if (!HidD_GetFeature(m_Handle, data, length)) {
				//register_error(dev, "HidD_SetFeature");
				System.out.println(GetLastError());
				return -1;
			}
		} else {
			int[] bytes = { 0 };

			OVERLAPPED ol = new OVERLAPPED();
			Pointer buffer = new Memory(data.length);
			if (!DeviceIoControl(m_Handle, IOCTL_HID_GET_FEATURE, buffer, length, buffer, length, bytes, ol)) {
				//System.out.println(GetLastError());
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
		return -1; // Eclipse says this is unreachable (it is), but won't compile without it ... go figure

	}

	private void runReadOnBackground() {
		m_SyncStart.waitAndSync();
		while (!m_StopThread) {
			m_InputReportBytesRead[0] = 0;
			ResetEvent(m_InputReportOverlapped.hEvent);

			// In Windos ReadFile() from a HID device Windows expects us to attempt to read as much bytes as there are
			// in the longest report plus one for the report number (even if not used) and the data is always
			// preceded with the report number (even if not used in case of which it is zero)
			if (!ReadFile(m_Handle, m_InputReportMemory, m_InputReportLength, null, m_InputReportOverlapped)) {
				if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
					break; // early exit if the device disappears
				if (GetLastError() != ERROR_IO_PENDING) {
					CancelIo(m_Handle);
					System.out.println("ReadFile failed with GetLastError()==" + GetLastError());
				}
			}

			if (!GetOverlappedResult(m_Handle, m_InputReportOverlapped, m_InputReportBytesRead, true/* wait */)) {
				if (GetLastError() == ERROR_DEVICE_NOT_CONNECTED)
					break; // early exit if the device disappears
				
				if(m_StopThread && GetLastError() == ERROR_OPERATION_ABORTED)
					break; // on close
				
				System.out.println("GetOverlappedResult failed with GetLastError()==" + GetLastError());
			}

			if (m_InputReportBytesRead[0] > 0) {
				byte reportID = m_InputReportMemory.getByte(0);
				m_InputReportBytesRead[0]--;
				m_InputReportMemory.read(1, m_InputReportBytes, 0, m_InputReportBytesRead[0]);

				if (m_InputReportListener != null)
					m_InputReportListener.onInputReport(this, reportID, m_InputReportBytes, m_InputReportBytesRead[0]);
			}

		}
		m_SyncShutdown.waitAndSync();
	}
	
	@Override
	public ParsedReportDataItem[] parseReport(Capability.Type capabilityType, byte reportID, byte[] reportData, int reportLength) {
		if (m_HidDeviceInfo == null) { return null; }
		Capability[] deviceCapabilities = m_HidDeviceInfo.getCapabilities();
		if (deviceCapabilities == null) { return null; }
		
		HIDP_REPORT_TYPE reportType = HIDP_REPORT_TYPE.fromCapabilityType(capabilityType);
		byte[] originalReportData = new byte[1 + reportLength];
		originalReportData[0] = reportID;
		System.arraycopy(reportData, 0, originalReportData, 1, reportLength);
		
		Capability[] reportCapabilities = Stream.of(deviceCapabilities)
			.filter(cap -> cap.getType() == capabilityType && cap.getReportId() == reportID)
			.sorted(Comparator.comparingInt(Capability::getDataIndexMin))
			.toArray(Capability[]::new);
		ParsedReportDataItem[] results = new ParsedReportDataItem[reportCapabilities.length];
		for (int i = 0; i < reportCapabilities.length; ++i) {
			Capability cap = reportCapabilities[i];
			ParsedReportDataItem parsedReportDataItem;
			short usagePage = cap.getUsagePage();
			short linkCollection = cap.getLinkCollection();
			if (cap instanceof Capability.ButtonRange) {
				Capability.ButtonRange buttonRange = (Capability.ButtonRange)cap;
				long maxUsageLength = HidP_MaxUsageListLength(reportType, usagePage, m_PreparsedData);
				short[] usageList = new short[(int)maxUsageLength];
				long[] usageLength = { maxUsageLength };
				HIDP_STATUS hidpStatus = HidP_GetButtons(
					reportType,
					usagePage,
					linkCollection,
					usageList,
					usageLength,
					m_PreparsedData,
					originalReportData,
					originalReportData.length
				);
				if (hidpStatus == HIDP_STATUS.SUCCESS) {
					usageList = Arrays.copyOf(usageList, (int)usageLength[0]);
				}
				else {
					usageList = null;
				}
				parsedReportDataItem = new ParsedReportDataItem(buttonRange, usageList);
			}
			else if (cap instanceof Capability.Value) {
				Capability.Value value = (Capability.Value)cap;
				short usage = value.getUsage();
				short usageValueByteLength = value.getReportByteLengthWithPadding();
				int reportCount = value.getReportCount();
				long[] parsedValues = new long[reportCount];
				HIDP_STATUS hidpStatus = null;
				if (reportCount == 1) {
					hidpStatus = HidP_GetUsageValue(
						reportType,
						usagePage,
						linkCollection,
						usage,
						parsedValues,
						m_PreparsedData,
						originalReportData,
						originalReportData.length
					);
				}
				else if (reportCount > 1) {
					byte[] usageValue = new byte[usageValueByteLength];
					hidpStatus = HidP_GetUsageValueArray(
						reportType,
						usagePage,
						linkCollection,
						usage,
						usageValue,
						usageValueByteLength,
						m_PreparsedData,
						originalReportData,
						originalReportData.length
					);
					if (hidpStatus == HIDP_STATUS.SUCCESS) {
						int bitSize = value.getBitSize();
						int reportBitLength = value.getReportBitLength();
						for (int b = 0, v = 0; b < reportBitLength; b += bitSize, ++v) {
							byte[] extractedData = HidDataParser.extractDataAtBitOffset(usageValue, b, bitSize);
							long parsedValue = HidDataParser.bytesToLong(extractedData, 0);
							parsedValues[v] = parsedValue;
						}
					}
				}
				if (hidpStatus != HIDP_STATUS.SUCCESS) {
					parsedValues = null;
				}
				parsedReportDataItem = new ParsedReportDataItem(value, parsedValues);
			}
			else {
				parsedReportDataItem = new ParsedReportDataItem(cap);
			}
			results[i] = parsedReportDataItem;
		}
		return results;
	}
	
	private void dumpCapabilities(HIDP_CAPS caps) {
		PrintStream out = System.out;
		
		out.println("windows/HidDevice: capabilities -------------------------");
		DataDump.dumpJnaStructures(out, caps);
		out.println("--------------------------------------------------------");
		
		short numberLinkCollectionNodes = caps.NumberLinkCollectionNodes;
		out.printf("windows/HidDevice: link collection nodes (%2d) ----------\n", numberLinkCollectionNodes);
		if (numberLinkCollectionNodes > 0) {
			HidLibrary.HIDP_LINK_COLLECTION_NODE[] lcNodes = new HidLibrary.HIDP_LINK_COLLECTION_NODE[numberLinkCollectionNodes];
			int[] lcNodesLength = {numberLinkCollectionNodes};
			HidP_GetLinkCollectionNodes(lcNodes, lcNodesLength, m_PreparsedData);
			DataDump.dumpJnaStructures(out, lcNodes);
		}
		out.println("--------------------------------------------------------");
		
		for (HidLibrary.HIDP_REPORT_TYPE reportType : HidLibrary.HIDP_REPORT_TYPE.values()) {
			short numberButtonCapNodes = reportType.getNumberButtonCaps(caps);
			out.printf("windows/HidDevice: %12s button cap nodes (%2d) --\n", reportType, numberButtonCapNodes);
			if (numberButtonCapNodes > 0) {
				HidLibrary.HIDP_BUTTON_CAPS[] buttonCapNodes = new HidLibrary.HIDP_BUTTON_CAPS[numberButtonCapNodes];
				short[] buttonCapNodesLength = {numberButtonCapNodes};
				HidP_GetButtonCaps(reportType, buttonCapNodes, buttonCapNodesLength, m_PreparsedData);
				DataDump.dumpJnaStructures(out, buttonCapNodes);
			}
			out.println("--------------------------------------------------------");
		}
		
		for (HidLibrary.HIDP_REPORT_TYPE reportType : HidLibrary.HIDP_REPORT_TYPE.values()) {
			short numberValueCapNodes = reportType.getNumberValueCaps(caps);
			out.printf("windows/HidDevice: %12s  value cap nodes (%2d) --\n", reportType, numberValueCapNodes);
			if (numberValueCapNodes > 0) {
				HidLibrary.HIDP_VALUE_CAPS[] valueCapNodes = new HidLibrary.HIDP_VALUE_CAPS[numberValueCapNodes];
				short[] valueCapNodesLength = {numberValueCapNodes};
				HidP_GetValueCaps(reportType, valueCapNodes, valueCapNodesLength, m_PreparsedData);
				DataDump.dumpJnaStructures(out, valueCapNodes);
			}
			out.println("--------------------------------------------------------");
		}
		
	}
}
