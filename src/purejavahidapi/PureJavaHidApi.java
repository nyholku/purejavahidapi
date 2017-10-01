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
package purejavahidapi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import purejavahidapi.shared.Backend;

import com.sun.jna.Platform;

/**
 * PureJavaHidApi class is the entry point to access USB HID devices.
 * <p>
 * Static methods in PureJavaHidApi allow enumeration and opening of HID
 * devices.
 * <p>
 * {@link #enumerateDevices()} method returns a iist of HidDeviceInfo
 * objects from which a device path can be obtained. The path can be passed to
 * the {@link #openDevice(HidDeviceInfo path)} method to obtain a {@link HidDevice} object
 * which can then be used to communicate with the device.
 * <p>
 * See javadoc for above mentioned classes and methods for details.
 * 
 */

public class PureJavaHidApi {
	private static Object m_Mutex = new Object();
	final private static Backend m_Backend;
	private static LinkedList<HidDevice> m_OpenDevices = new LinkedList();

	/**
	 * 
	 * @return PureJavaHidApi library version string
	 */
	public static String getVersion() {
		return "0.0.10";
	}

	/**
	 * Returns a list of available USB HID devices.
	 * <p>
	 * Passing a 0 for the vendorId or productId macthes everything and thus
	 * works as a wild card for matching. Passing 0 for both will return a list
	 * of all USB HID devices.
	 * 
	 * @return List of HidDeviceInfo objects representing the matching devices.
	 */
	public static List<HidDeviceInfo> enumerateDevices() {
		synchronized (m_Mutex) {
			if (m_Backend == null)
				throw new IllegalStateException("Unsupported platform");
			return m_Backend.enumerateDevices();
		}
	}

	/**
	 * Given a device path opens a USB device for communication.
	 * 
	 * @param path
	 *            A path obtained from a HidDeviceInfo object.
	 * @return An instance of HidDevice that can be used to communicate with the
	 *         HID device.
	 * @throws IOException
	 *             if the device cannot be opened
	 * @see HidDeviceInfo#getPath()
	 */
	public static HidDevice openDevice(HidDeviceInfo path) throws IOException {
		synchronized (m_Mutex) {
			if (m_Backend == null)
				throw new IllegalStateException("Unsupported platform");
			HidDevice device = m_Backend.openDevice(path);
			if (device != null)
				m_OpenDevices.add(device);
			return device;
		}
	}

	static {
		if (Platform.isMac()) {
			m_Backend = new purejavahidapi.macosx.MacOsXBackend();
		} else if (Platform.isWindows()) {
			m_Backend = new purejavahidapi.windows.WindowsBackend();
		} else if (Platform.isLinux()) {
			m_Backend = new purejavahidapi.linux.LinuxBackend();
		} else
			m_Backend = null;
		if (m_Backend != null)
			m_Backend.init();
	}
}
