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

import com.sun.jna.Platform;

//inspired,intrigued amazed and annoyed how difficult and complex and non existent simple cross platform USB IO is?!

// TODO error handling (especially background thread)
// TODO hotplug
// TODO windows setReport implementation and testing
// TODO mac setReport  testing
// TODO devicie path
// http://sourceforge.net/mailarchive/message.php?msg_id=29300228
// http://ysflight.in.coocan.jp/programming/macosx/samples/JoystickSample.cpp
// TODO check memory disposal

public class PureJavaHidApi {
	private static Object m_Mutex = new Object();
	private static boolean m_Initialized = false;
	private static boolean m_CleanedUp = true;
	private static PureJavaHidApiBackend m_Backend = null;
	private static LinkedList<HidDevice> m_OpenDevices = new LinkedList();

	public interface Frontend {
		void closeDevice(HidDevice device);
	}

	public interface PureJavaHidApiBackend {
		void init();

		void cleanup();

		List<HidDeviceInfo> enumerateDevices(short vendorId, short productId);

		HidDevice openDevice(String path, Frontend frontEnd) throws IOException;
	}

	public static void init() {
		synchronized (m_Mutex) {
			if (m_Initialized)
				return;
			m_Backend.init();
			m_Initialized = true;
		}
	}

	public static void cleanup() {
		synchronized (m_Mutex) {
			if (m_CleanedUp || !m_Initialized)
				return;
			LinkedList<HidDevice> devices = new LinkedList(m_OpenDevices);
			for (HidDevice device : devices) {
				try {
					device.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			m_Backend.cleanup();
			m_CleanedUp = true;
		}
	}

	public static List<HidDeviceInfo> enumerateDevices(short vendorId, short productId) {
		synchronized (m_Mutex) {
			return m_Backend.enumerateDevices(vendorId, productId);
		}
	}

	public static HidDevice openDevice(String path) throws IOException {
		synchronized (m_Mutex) {
			HidDevice device = m_Backend.openDevice(path, new Frontend() {
				@Override
				public void closeDevice(HidDevice device) {
					m_OpenDevices.remove(device);
				}
			});
			m_OpenDevices.add(device);
			return device;
		}
	}

	static { // INSTANTIATION 
		if (Platform.isMac()) {
			m_Backend = new purejavahidapi.macosx.MacOsXBackend();
		} else if (Platform.isWindows()) {
			m_Backend = new purejavahidapi.windows.WindowsBackend();
		} else if (Platform.isLinux()) {
			m_Backend = new purejavahidapi.linux.LinuxBackend();
		}
	}
}
