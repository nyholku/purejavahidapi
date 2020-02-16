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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.*;

//Web resources to help implementing the missing functions
//http://www.signal11.us/oss/udev/
//http://www.makelinux.net/ldd3/chp-13-sect-5
//http://lmu.web.psi.ch/docu/manuals/software_manuals/linux_sl/usb_linux_programming_guide.pdf

public class UdevLibrary {

	// OBSOLETE class: hid_device

	static UdevInterface INSTANCE = (UdevInterface) Native.load("udev", UdevInterface.class);

	public final static int BUS_USB = 0x03;
	public final static int BUS_BLUETOOTH = 0x05;
	public final static int O_RDWR = 0x0002;
	public final static int HID_MAX_DESCRIPTOR_SIZE = 4096;

	public final static int _IOC_NRSHIFT = 0;
	public final static int _IOC_NRBITS = 8;
	public final static int _IOC_TYPEBITS = 8;
	public final static int _IOC_SIZEBITS = 14;
	public final static int _IOC_READ = 2;
	public final static int _IOC_TYPECHECK = 4; // #define _IOC_TYPECHECK	(	 	t	)	   (sizeof(t))

	public final static int _IOC_TYPESHIFT = (_IOC_NRSHIFT + _IOC_NRBITS);

	public final static int _IOC_SIZESHIFT = (_IOC_TYPESHIFT + _IOC_TYPEBITS);

	public final static int _IOC_DIRSHIFT = (_IOC_SIZESHIFT + _IOC_SIZEBITS);

	public static final int SIZE_OF_INT = 4;

	public static int _IOC(int dir, int type, int nr, int size) {
		return (((dir) << _IOC_DIRSHIFT) | ((type) << _IOC_TYPESHIFT) | ((nr) << _IOC_NRSHIFT) | ((size) << _IOC_SIZESHIFT));
	}

	public static int _IOR(int type, int nr, int size) {
		return _IOC(_IOC_READ, type, nr, size);
	}

	public static int HIDIOCGRDESC = _IOR('H', 0x02, new hidraw_report_descriptor().size());

	public static int HIDIOCGRDESCSIZE = _IOR('H', 0x01, SIZE_OF_INT);

	public static class hidraw_report_descriptor extends Structure {
		public int size;
		public byte[] value = new byte[HID_MAX_DESCRIPTOR_SIZE];

		protected List getFieldOrder() {
			return Arrays.asList("size", "value");

		}
	}

	public static class udev_monitor extends PointerType {

	}

	public static class device_handle extends PointerType {

	}

	public static class udev extends PointerType {

	}

	public static class udev_device extends PointerType {

	}

	public static class udev_enumerate extends PointerType {

	}

	public static class udev_list_entry extends PointerType {

	}

	interface UdevInterface extends Library {

		udev udev_new();

		udev_enumerate udev_enumerate_new(udev udev);

		void udev_enumerate_add_match_subsystem(udev_enumerate udev_enumerate, String subsystem);

		void udev_enumerate_scan_devices(udev_enumerate udev_enumerate);

		udev_list_entry udev_enumerate_get_list_entry(udev_enumerate udev_enumerate);

		udev_list_entry udev_list_entry_get_next(udev_list_entry list_entry);

		String udev_list_entry_get_name(udev_list_entry list_entry);

		udev_device udev_device_new_from_syspath(udev udev, String sysfs_path);

		udev_monitor udev_monitor_new_from_netlink(udev udev, String name);

		int udev_monitor_filter_add_match_subsystem_devtype(udev_monitor udev_monitor, String subsystem, String devtype);

		int udev_monitor_enable_receiving(udev_monitor udev_monitor);

		int udev_monitor_get_fd(udev_monitor udev_monitor);

		udev_device udev_monitor_receive_device(udev_monitor udev_monitor);

		String udev_device_get_action(udev_device udev_device);

		String udev_device_get_subsystem(udev_device udev_device);

		String udev_device_get_devtype(udev_device udev_device);

		String udev_device_get_syspath(udev_device udev_device);

		String udev_device_get_sysname(udev_device udev_device);

		String udev_device_get_sysnum(udev_device udev_device);

		String udev_device_get_devnode(udev_device device);

		udev_device udev_device_get_parent_with_subsystem_devtype(udev_device udev_device, String subsystem, String devtype);

		String udev_device_get_sysattr_value(udev_device udev_device, String sysattr);

		void udev_device_unref(udev_device device);

		void udev_enumerate_unref(udev_enumerate enumerate);

		void udev_unref(udev dev);

		//int usb_control_msg(udev_device dev, int pipe, byte request, byte requesttype, short value, short index, Pointer data, short size, int timeout);

	}

	public static udev udev_new() {
		return INSTANCE.udev_new();
	}

	public static udev_enumerate udev_enumerate_new(udev udev) {
		return INSTANCE.udev_enumerate_new(udev);
	}

	public static void udev_enumerate_add_match_subsystem(udev_enumerate udev_enumerate, String subsystem) {
		INSTANCE.udev_enumerate_add_match_subsystem(udev_enumerate, subsystem);
	}

	public static void udev_enumerate_scan_devices(udev_enumerate udev_enumerate) {
		INSTANCE.udev_enumerate_scan_devices(udev_enumerate);
	}

	public static udev_list_entry udev_enumerate_get_list_entry(udev_enumerate udev_enumerate) {
		return INSTANCE.udev_enumerate_get_list_entry(udev_enumerate);
	}

	public static udev_list_entry udev_list_entry_get_next(udev_list_entry list_entry) {
		return INSTANCE.udev_list_entry_get_next(list_entry);
	}

	public static String udev_list_entry_get_name(udev_list_entry list_entry) {
		return INSTANCE.udev_list_entry_get_name(list_entry);
	}

	public static udev_device udev_device_new_from_syspath(udev udev, String sysfs_path) {
		return INSTANCE.udev_device_new_from_syspath(udev, sysfs_path);
	}

	public static udev_monitor udev_monitor_new_from_netlink(udev udev, String name) {
		return INSTANCE.udev_monitor_new_from_netlink(udev, name);
	}

	public static int udev_monitor_filter_add_match_subsystem_devtype(udev_monitor udev_monitor, String subsystem, String devtype) {
		return INSTANCE.udev_monitor_filter_add_match_subsystem_devtype(udev_monitor, subsystem, devtype);
	}

	public static int udev_monitor_enable_receiving(udev_monitor udev_monitor) {
		return INSTANCE.udev_monitor_enable_receiving(udev_monitor);
	}

	public static int udev_monitor_get_fd(udev_monitor udev_monitor) {
		return INSTANCE.udev_monitor_get_fd(udev_monitor);
	}

	public static udev_device udev_monitor_receive_device(udev_monitor udev_monitor) {
		return INSTANCE.udev_monitor_receive_device(udev_monitor);
	}

	public static String udev_device_get_action(udev_device udev_device) {
		return INSTANCE.udev_device_get_action(udev_device);
	}

	public static String udev_device_get_subsystem(udev_device udev_device) {
		return INSTANCE.udev_device_get_subsystem(udev_device);
	}

	public static String udev_device_get_devtype(udev_device udev_device) {
		return INSTANCE.udev_device_get_devtype(udev_device);
	}

	public static String udev_device_get_syspath(udev_device udev_device) {
		return INSTANCE.udev_device_get_syspath(udev_device);
	}

	public static String udev_device_get_sysname(udev_device udev_device) {
		return INSTANCE.udev_device_get_sysname(udev_device);
	}

	public static String udev_device_get_sysnum(udev_device udev_device) {
		return INSTANCE.udev_device_get_sysnum(udev_device);
	}

	public static String udev_device_get_devnode(udev_device device) {
		return INSTANCE.udev_device_get_devnode(device);
	}

	public static udev_device udev_device_get_parent_with_subsystem_devtype(udev_device udev_device, String subsystem, String devtype) {
		return INSTANCE.udev_device_get_parent_with_subsystem_devtype(udev_device, subsystem, devtype);
	}

	public static String udev_device_get_sysattr_value(udev_device udev_device, String sysattr) {
		return INSTANCE.udev_device_get_sysattr_value(udev_device, sysattr);
	}

	public static void udev_device_unref(udev_device device) {
		INSTANCE.udev_device_unref(device);
	}

	public static void udev_enumerate_unref(udev_enumerate enumerate) {
		INSTANCE.udev_enumerate_unref(enumerate);
	}

	public static void udev_unref(udev dev) {
		INSTANCE.udev_unref(dev);
	}

}