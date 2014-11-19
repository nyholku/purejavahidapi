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
package purejavahidapi.linux;

import java.io.IOException;
import java.util.List;

import com.sun.jna.Native;

import purejavahidapi.*;
import purejavahidapi.shared.Frontend;
import purejavahidapi.shared.SyncPoint;
import static purejavahidapi.linux.UdevLibrary.*;

public class HidDevice implements purejavahidapi.HidDevice {
	private boolean m_Open = true;
	private int m_DeviceHandle;

	private InputReportListener m_InputReportListener;

	private DeviceRemovalListener m_DeviceRemovalListener;

	private HidDeviceInfo m_HidDeviceInfo;
	private Frontend m_Frontend;

	private boolean m_UsesNumberedReports;
	private Thread m_Thread;
	private SyncPoint m_SyncStart;
	private SyncPoint m_SyncShutdown;
	private boolean m_StopThread;
	private byte[] m_InputReportBytes;
	private byte[] m_OutputReportBytes;

	/* package */HidDevice(String path, Frontend frontend) throws IOException {
		m_Frontend = frontend;
		udev udev = udev_new();
		udev_device raw_dev = udev_device_new_from_syspath(udev, path);
		String dev_path = udev_device_get_devnode(raw_dev);
		udev_unref(udev);

		m_HidDeviceInfo = new HidDeviceInfo(path);
		// OPEN HERE //
		m_DeviceHandle = open(dev_path, O_RDWR);

		// If we have a good handle, return it.
		if (m_DeviceHandle <= 0)
			throw new IOException("open() failed, errno " + Native.getLastError());

		// Get the report descriptor 
		int[] desc_size = { 0 };
		int res;
		hidraw_report_descriptor rpt_desc = new hidraw_report_descriptor();
		// Get Report Descriptor Size 

		res = ioctl(m_DeviceHandle, HIDIOCGRDESCSIZE, desc_size);
		if (res < 0) // FIXME ERROR HANDLING
			throw new IOException("ioctl(...HIDIOCGRDESCSIZE..) failed"); // perror("HIDIOCGRDESCSIZE");

		// Get Report Descriptor 
		rpt_desc.size = desc_size[0];
		res = ioctl(m_DeviceHandle, HIDIOCGRDESC, rpt_desc);
		if (res < 0)
			throw new IOException("ioctl(...HIDIOCGRDESC..) failed");
		// Determine if this device uses numbered reports. 
		m_UsesNumberedReports = uses_numbered_reports(rpt_desc.value, rpt_desc.size);

		//---------------

		// Magic here, assume that no HID device ever uses reports longer than 4kB 
		m_InputReportBytes = new byte[4096 + 1];
		m_OutputReportBytes = new byte[4096 + 1];

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
		m_Thread.start();
		m_SyncStart.waitAndSync();
	}

	private void runReadOnBackground() {
		m_SyncStart.waitAndSync();
		while (!m_StopThread) {
			// In Linux read() from a HID device we always try to read at least as many bytes as there can be in a report
			// the kernel will return with the actual number of bytes in the report (plus one if numbered reports are used)
			// and the data will be preceded with the report number if and only if numbered reports are used, which is
			// kind of stupid because then we need to know this and to know that it is necessary to (be able to!) read
			// the HID descriptor AND parse it. I like the Mac OS and Windows ways better, what a mess the world is!
			int bytes_read = read(m_DeviceHandle, m_InputReportBytes, m_InputReportBytes.length);
			if (m_InputReportListener != null) {
				byte reportID = 0;
				if (m_UsesNumberedReports) {
					reportID = m_InputReportBytes[0];
					bytes_read--;
					System.arraycopy(m_InputReportBytes, 1, m_InputReportBytes, 0, bytes_read);
				}
				m_InputReportListener.onInputReport(this, reportID, m_InputReportBytes, bytes_read);
			}

		}
		m_SyncShutdown.waitAndSync();
	}

	@Override
	synchronized public void close() {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_StopThread=true;
		UdevLibrary.close(m_DeviceHandle);
		m_Thread.interrupt();
		m_SyncShutdown.waitAndSync();
		m_Frontend.closeDevice(this);
		m_Open=false;
	}

	@Override
	synchronized public int setOutputReport(byte reportID, byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		// In Linux write() to HID device data is preceded with the report number only if numbered reports are used
		//
		//   "The first byte of the buffer passed to write() should be set to the report
		//   number.  If the device does not use numbered reports, the first byte should
		//   be set to 0. The report data itself should begin at the second byte."
		//
		//   References:
		//   - https://www.kernel.org/doc/Documentation/hid/hidraw.txt
		//   - http://www.usb.org/developers/hidpage/HID1_11.pdf
		if (m_UsesNumberedReports)
			m_OutputReportBytes[0] = reportID;
		else
			m_OutputReportBytes[0] = 0;
		System.arraycopy(data, 0, m_OutputReportBytes, 1, length);
		int len = write(m_DeviceHandle, m_OutputReportBytes, length + 1);
		if (len < 0)
			return len;
		return len - 1;
	}

	@Override
	synchronized public int setFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		return -1;
	}

	@Override
	synchronized public void setInputReportListener(InputReportListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_InputReportListener = listener;
	}

	@Override
	synchronized public void setDeviceRemovalListener(DeviceRemovalListener listener) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		m_DeviceRemovalListener = listener;
	}

	@Override
	synchronized public int getFeatureReport(byte[] data, int length) {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		return -1;
	}

	@Override
	synchronized public HidDeviceInfo getHidDeviceInfo() {
		if (!m_Open)
			throw new IllegalStateException("device not open");
		return m_HidDeviceInfo;
	}

	private static boolean uses_numbered_reports(byte[] report_descriptor, int size)

	{

		for (int i = 0; i < size; i++) {
			System.out.printf("0x%02X, ", report_descriptor[i]);
			if ((i & 15) == 15)
				System.out.println();
		}
		int i = 0;

		int size_code;
		int data_len, key_size;

		while (i < size) {
			int key = report_descriptor[i];

			// Check for the Report ID key 
			if (key == 0x85) {// Report ID
				// This device has a Report ID, which means it uses numbered reports. 
				return true;
			}

			System.out.printf("key: %02x\n", 0xff & key);

			if ((key & 0xf0) == 0xf0) {
				/* This is a Long Item. The next byte contains the
				   length of the data section (value) for this key.
				   See the HID specification, version 1.11, section
				   6.2.2.3, titled "Long Items." */
				if (i + 1 < size)
					data_len = report_descriptor[i + 1];
				else
					data_len = 0; // malformed report 
				key_size = 3;
			} else {
				/* This is a Short Item. The bottom two bits of the
				   key contain the size code for the data section
				   (value) for this key.  Refer to the HID
				   specification, version 1.11, section 6.2.2.2,
				   titled "Short Items." */
				size_code = key & 0x3;
				switch (size_code) {
					case 0:
					case 1:
					case 2:
						data_len = size_code;
						break;
					case 3:
						data_len = 4;
						break;
					default:
						// Can't ever happen since size_code is & 0x3 
						data_len = 0;
						break;
				}
				;
				key_size = 1;
			}

			// Skip over this key and it's associated data 
			i += data_len + key_size;
		}

		/* Didn't find a Report ID key. Device doesn't use numbered reports. 
		*/
		return false;
	}

}
