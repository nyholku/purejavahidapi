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
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import purejavahidapi.linux.UdevLibrary.hidraw_report_descriptor;
import purejavahidapi.shared.Backend;
import purejavahidapi.shared.Frontend;

import com.sun.jna.Pointer;

import static purejavahidapi.linux.UdevLibrary.*;

public class LinuxBackend implements Backend {
	@Override
	public void init() {
	}

	@Override
	public void cleanup() {

	}

	@Override
	public List<purejavahidapi.HidDeviceInfo> enumerateDevices() {

		List<purejavahidapi.HidDeviceInfo> list = new LinkedList<purejavahidapi.HidDeviceInfo>();

		udev_enumerate enumerate;
		udev_list_entry devices;
		udev_list_entry dev_list_entry;
		udev udev;

		udev = udev_new();

		enumerate = udev_enumerate_new(udev);
		udev_enumerate_add_match_subsystem(enumerate, "hidraw");
		udev_enumerate_scan_devices(enumerate);
		devices = udev_enumerate_get_list_entry(enumerate);

		// FOR EACH
		loop: for (dev_list_entry = devices; dev_list_entry != null; dev_list_entry = udev_list_entry_get_next(dev_list_entry)) {
			String str;
			udev_device usb_dev; // The device's USB udev node.
			udev_device intf_dev; // The device's interface (in the USB sense).

			String sysfs_path = udev_list_entry_get_name(dev_list_entry);
			udev_device raw_dev = udev_device_new_from_syspath(udev, sysfs_path);
			String dev_path = udev_device_get_devnode(raw_dev);
			udev_device hid_dev = udev_device_get_parent_with_subsystem_devtype(raw_dev, "hid", null);

			if (hid_dev == null)
				continue loop;

			try {
				Properties p = new Properties();
				p.load(new StringReader(udev_device_get_sysattr_value(hid_dev, "uevent")));

				String[] hidId = ((String) p.get("HID_ID")).split(":");
				short bus = (short) Long.parseLong(hidId[0], 16);
				short vid = (short) Long.parseLong(hidId[1], 16);
				short pid = (short) Long.parseLong(hidId[2], 16);

				if (bus != BUS_USB && bus != BUS_BLUETOOTH)
					continue loop;

				HidDeviceInfo info = new HidDeviceInfo(sysfs_path);
				list.add(info);

			} catch (Exception e) {
				e.printStackTrace();
			}

			udev_device_unref(raw_dev);
			//* hid_dev, usb_dev and intf_dev can't be) unref()d. I'm not sure why.

		} 
		/* Free the enumerator and udev objects. */
		udev_enumerate_unref(enumerate);
		udev_unref(udev);

		return list;
	}

	@Override
	public purejavahidapi.HidDevice openDevice(String path,Frontend frontend) throws IOException {
		return new HidDevice(path,frontend);

	}

}
