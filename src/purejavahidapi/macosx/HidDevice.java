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
package purejavahidapi.macosx;

import java.io.UnsupportedEncodingException;
import java.nio.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.plaf.ColorChooserUI;

import static purejavahidapi.macosx.CoreFoundationLibrary.*;
import static purejavahidapi.macosx.IOHIDManagerLibrary.*;

import com.sun.jna.*;

import purejavahidapi.*;
import purejavahidapi.macosx.CoreFoundationLibrary.CFMutableDictionaryRef;
import purejavahidapi.macosx.IOHIDManagerLibrary.IOCFPlugInInterface;
import purejavahidapi.macosx.IOHIDManagerLibrary.io_iterator_t;
import purejavahidapi.macosx.IOHIDManagerLibrary.io_object_t;
import purejavahidapi.macosx.IOHIDManagerLibrary.mach_port_t;
import purejavahidapi.macosx.CoreFoundationLibrary.*;
import purejavahidapi.macosx.IOHIDManagerLibrary.*;
import purejavahidapi.shared.Frontend;
import purejavahidapi.shared.SyncPoint;

public class HidDevice implements purejavahidapi.HidDevice {
	private boolean m_Open=true;
	private Frontend m_Frontend;
	private HidDeviceInfo m_HidDeviceInfo;
	private int m_InternalId; // used when passing 'HidDevice' to Mac OS X callbacks
	private IOHIDDeviceRef m_IOHIDDeviceRef;
	private boolean m_Disconnected;
	private CFStringRef m_CFRunLoopMode;
	private CFRunLoopRef m_CFRunLoopRef;
	private CFRunLoopSourceRef m_CFRunLoopSourceRef;
	private Pointer m_InputReportBuffer;
	private byte[] m_InputReportData;
	private int m_MaxInputReportLength;
	private InputReportListener m_InputReportListener;
	private DeviceRemovalListener m_DeviceRemovalListener;
	private Thread m_Thread;
	private SyncPoint m_SyncStart;
	private SyncPoint m_SyncShutdown;
	private boolean m_StopThread;

	// store a reference to the callbacks here so that they are not prematurely garbage collected
	private HidReportCallback m_HidReportCallBack;
	private HidDeviceRemovalCallback m_HidDeviceRemovalCallback;
	private PerformSignalCallback m_PerformSignalCallback;

	Pointer asPointerForPassingToCallback() {
		return new Pointer(m_InternalId);
	}

	HidDevice(IOHIDDeviceRef dev, Frontend frontend) {
		m_Frontend = frontend;
		m_InternalId = MacOsXBackend.addHidDevice(this);
		m_IOHIDDeviceRef = dev;
		m_PerformSignalCallback = new PerformSignalCallback();
		m_HidReportCallBack = new HidReportCallback();
		m_HidDeviceRemovalCallback = new HidDeviceRemovalCallback();
		m_SyncStart = new SyncPoint(2);
		m_SyncShutdown = new SyncPoint(2);
		m_MaxInputReportLength = getIntProperty(dev, CFSTR(kIOHIDMaxInputReportSizeKey));
		m_InputReportBuffer = new Memory(m_MaxInputReportLength);
		m_InputReportData = new byte[m_MaxInputReportLength];

		m_HidDeviceInfo = new HidDeviceInfo(dev);

		String str = String.format("HIDAPI_0x%08x", Pointer.nativeValue(dev.getPointer()));
		m_CFRunLoopMode = CFStringCreateWithCString(null, str, kCFStringEncodingASCII);

		IOHIDDeviceRegisterInputReportCallback(dev, m_InputReportBuffer, m_MaxInputReportLength, m_HidReportCallBack, asPointerForPassingToCallback()); // shoudl pass dev

		IOHIDManagerRegisterDeviceRemovalCallback(MacOsXBackend.m_HidManager, m_HidDeviceRemovalCallback, null);

		m_Thread = new Thread(new Runnable() {
			@Override
			public void run() {

				// Move the device's run loop to this thread. 
				IOHIDDeviceScheduleWithRunLoop(m_IOHIDDeviceRef, CFRunLoopGetCurrent(), m_CFRunLoopMode);

				// RunLoopSource  is used to signal the event loop to stop 
				CFRunLoopSourceContext ctx = new CFRunLoopSourceContext();
				ctx.perform = m_PerformSignalCallback;
				m_CFRunLoopSourceRef = CFRunLoopSourceCreate(kCFAllocatorDefault, 0/* order */, ctx);

				CFRunLoopAddSource(CFRunLoopGetCurrent(), m_CFRunLoopSourceRef, m_CFRunLoopMode);

				m_CFRunLoopRef = CFRunLoopGetCurrent();

				// Notify the main thread that the read thread is up and running
				m_SyncStart.waitAndSync();

				// Run the event loop, CFRunLoopRunInMode(), whick will dispatch HID input reports
				int code;
				while (!m_StopThread && !m_Disconnected) {
					code = CFRunLoopRunInMode(m_CFRunLoopMode, 1000/* sec */, false);
					// Return if the device has been disconnected 
					if (code == kCFRunLoopRunFinished) {
						m_Disconnected = true;
						break;
					}

					if ((code != kCFRunLoopRunTimedOut) && (code != kCFRunLoopRunHandledSource)) {
						m_StopThread = true;
						break;
					}
				}

				// Wait here until close()  makes it past the call to CFRunLoopWakeUp(). 

				m_SyncShutdown.waitAndSync();
			}
		}, m_HidDeviceInfo.getPath());
		m_Thread.start();
		m_SyncStart.waitAndSync();
	}

	@Override
	public void setInputReportListener(InputReportListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_InputReportListener = listener;
	}

	@Override
	public void setDeviceRemovalListener(DeviceRemovalListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_DeviceRemovalListener = listener;
	}

	static void processPendingEvents() {
		int res;
		do {
			res = CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.001, false);
		} while (res != kCFRunLoopRunFinished && res != kCFRunLoopRunTimedOut);
	}

	static int getIntProperty(IOHIDDeviceRef device, CFStringRef key) {
		int[] value = { 0 };

		CFTypeRef ref = IOHIDDeviceGetProperty(device, key);
		if (ref != null) {
			if (CFGetTypeID(ref.getPointer()) == CFNumberGetTypeID()) {
				CFNumberGetValue(new CFNumber(ref.getPointer()), kCFNumberSInt32Type, value);
				return value[0];
			}
		}
		return 0;
	}

	static int getIntProperty(IOHIDElementRef element, CFStringRef key) {
		int[] value = { 0 };

		CFTypeRef ref = IOHIDElementGetProperty(element, key);
		if (ref != null) {
			if (CFGetTypeID(ref.getPointer()) == CFNumberGetTypeID()) {
				CFNumberGetValue(new CFNumber(ref.getPointer()), kCFNumberSInt32Type, value);
				return value[0];
			}
		}
		return 0;
	}

	static String getStringProperty(IOHIDDeviceRef device, CFStringRef prop) {
		try {
			CFTypeRef t = IOHIDDeviceGetProperty(device, prop);
			CFStringRef str = null;
			if (t != null)
				str = new CFStringRef(t.getPointer());
			if (str != null) {
				long str_len = CFStringGetLength(str);
				CFRange range = new CFRange(0, str_len);
				long[] used_buf_len = { 0 };
				//long chars_copied = CFStringGetBytes(str, range, kCFStringEncodingUTF32LE, (byte) '?', false, buf, 255, used_buf_len);
				CFStringGetBytes(str, range, kCFStringEncodingUTF8, (byte) '?', false, null, 0, used_buf_len);
				byte[] buf = new byte[(int) used_buf_len[0]];
				CFStringGetBytes(str, range, kCFStringEncodingUTF8, (byte) '?', false, buf, buf.length, used_buf_len);
				return new String(buf, "utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	static String createPathForDevide(IOHIDDeviceRef dev) {
		short vid, pid;
		String transport = getStringProperty(dev, CFSTR(kIOHIDTransportKey));
		if (transport == null)
			return null;
		vid = (short) getIntProperty(dev, CFSTR(kIOHIDVendorIDKey));
		pid = (short) getIntProperty(dev, CFSTR(kIOHIDProductIDKey));

		return String.format("%s_%04x_%04x_0x%08x", transport, vid, pid, Pointer.nativeValue(dev.getPointer()));
	}

	static class HidDeviceRemovalCallback implements IOHIDDeviceCallback {
		@Override
		public void hid_device_removal_callback(Pointer context, int result, Pointer sender, IOHIDDeviceRef dev_ref) {
			HidDevice dev = MacOsXBackend.getHidDevice(context);
			if (dev != null && dev.m_DeviceRemovalListener != null)
				dev.m_DeviceRemovalListener.onDeviceRemoval(dev);
		}
	}

	static class HidReportCallback implements IOHIDReportCallback {
		public void callback(Pointer context, int result, Pointer sender, int reportType, int reportID, Pointer report, NativeLong report_length) {
			HidDevice dev = MacOsXBackend.getHidDevice(context);
			if (dev != null && dev.m_InputReportListener != null) {
				int length = report_length.intValue();
				report.read(0, dev.m_InputReportData, 0, length);
				dev.m_InputReportListener.onInputReport(dev, (byte) reportID, dev.m_InputReportData, length);
			}
		}
	}

	static private class PerformSignalCallback implements CFRunLoopPerformCallBack {
		@Override
		public void callback(Pointer context) {
			HidDevice dev = MacOsXBackend.getHidDevice(context);
			CFRunLoopStop(dev.m_CFRunLoopRef);
		}

	}

	public int getFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		int[] len = { length };
		int res;

		res = IOHIDDeviceGetReport(m_IOHIDDeviceRef, kIOHIDReportTypeFeature, 0xFF & data[0], ByteBuffer.wrap(data), len);
		if (res == kIOReturnSuccess)
			return len[0];
		else
			return -1;
	}

	private int setReport(int type, byte reporID, byte[] data, int length) {
		ByteBuffer data_to_send;

		int length_to_send;
		int res;

		data_to_send = ByteBuffer.wrap(data);
		length_to_send = length;

		// On Mac OS X the IOHIDDeviceSetReport() always takes pure data and explicit report number (which maybe 0 if numbers are not used)
		res = IOHIDDeviceSetReport(m_IOHIDDeviceRef, type, 0xff & reporID, data_to_send, length_to_send);

		if (res == kIOReturnSuccess) {
			return length;
		} else
			return -1;
	}

	public int setOutputReport(byte reportID, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		return setReport(kIOHIDReportTypeOutput, reportID, data, length);
	}

	public int setFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		return setReport(kIOHIDReportTypeFeature, (byte) 0, data, length);
	}

	public void close() {
		if (!m_Open)
			throw new IllegalStateException("device not open");

		/* Disconnect the report callback before close. */
		if (!m_Disconnected) {
			// according to the following link unregistering callbacks is not safe ???
			// https://github.com/signal11/hidapi/issues/116			
			IOHIDDeviceRegisterInputReportCallback(m_IOHIDDeviceRef, m_InputReportBuffer, m_MaxInputReportLength, null, asPointerForPassingToCallback());
			IOHIDManagerRegisterDeviceRemovalCallback(MacOsXBackend.m_HidManager, null, asPointerForPassingToCallback());
			IOHIDDeviceUnscheduleFromRunLoop(m_IOHIDDeviceRef, m_CFRunLoopRef, m_CFRunLoopMode);
			IOHIDDeviceScheduleWithRunLoop(m_IOHIDDeviceRef, CFRunLoopGetMain(), kCFRunLoopDefaultMode);
		}

		/* Cause read_thread() to stop. */
		m_StopThread = true;

		/* Wake up the run thread's event loop so that the thread can exit. */
		CFRunLoopSourceSignal(m_CFRunLoopSourceRef);
		CFRunLoopWakeUp(m_CFRunLoopRef);

		// Notify the read thread that it can shut down now. 
		m_SyncShutdown.waitAndSync();

		// Wait for the tread to close down
		try {
			m_Thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		/*
		 * Close the OS handle to the device, but only if it's not been
		 * unplugged. If it's been unplugged, then calling IOHIDDeviceClose()
		 * will crash.
		 */
		if (!m_Disconnected) {
			IOHIDDeviceClose(m_IOHIDDeviceRef, kIOHIDOptionsTypeNone);
		}

		CFRelease(m_IOHIDDeviceRef);

		MacOsXBackend.removeHidDevice(m_InternalId);
		m_Frontend.closeDevice(this);
		m_Open = false;
	}

	@Override
	public HidDeviceInfo getHidDeviceInfo() {
		return m_HidDeviceInfo;
	}

}
