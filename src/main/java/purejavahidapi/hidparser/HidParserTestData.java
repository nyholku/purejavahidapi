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
package purejavahidapi.hidparser;

public class HidParserTestData {

	static int[] standardMouseDescriptor = { // should be byte[] but initialization is easier when it is int[]
	// This test descriptor from USB HID spec example
			0x05, 0x01, // USAGE_PAGE (Generic Desktop)
			0x09, 0x02, // USAGE (Mouse)
			0xa1, 0x01, // COLLECTION (Application)
			0x09, 0x01, //   USAGE (Pointer)
			0xa1, 0x00, //   COLLECTION (Physical)
			0x05, 0x09, //     USAGE_PAGE (Button)
			0x19, 0x01, //     USAGE_MINIMUM (Button 1)
			0x29, 0x03, //     USAGE_MAXIMUM (Button 3)
			0x15, 0x00, //     LOGICAL_MINIMUM (0)
			0x25, 0x01, //     LOGICAL_MAXIMUM (1)
			0x95, 0x03, //     REPORT_COUNT (3)
			0x75, 0x01, //     REPORT_SIZE (1)
			0x81, 0x02, //     INPUT (Data,Var,Abs)
			0x95, 0x01, //     REPORT_COUNT (1)
			0x75, 0x05, //     REPORT_SIZE (5)
			0x81, 0x03, //     INPUT (Cnst,Var,Abs)
			0x05, 0x01, //     USAGE_PAGE (Generic Desktop)
			0x09, 0x30, //     USAGE (X)
			0x09, 0x31, //     USAGE (Y)
			0x15, 0x81, //     LOGICAL_MINIMUM (-127)
			0x25, 0x7f, //     LOGICAL_MAXIMUM (127)
			0x75, 0x08, //     REPORT_SIZE (8)
			0x95, 0x02, //     REPORT_COUNT (2)
			0x81, 0x06, //     INPUT (Data,Var,Rel)
			0xc0, //   END_COLLECTION
			0xc0, // // END_COLLECTION
	};
	static int[] cheapGamepad = { // should be byte[] but initialization is easier when it is int[]
	// this test data capture from VID = 0x0810 PID = 0x0005 Manufacturer = null Product = USB Gamepad  Path = /sys/devices/pci0000:00/0000:00:1d.0/usb2/2-1/2-1:1.0/0003:0810:0005.0002/hidraw/hidraw0
			0x05, 0x01, //
			0x09, 0x04, //
			0xA1, 0x01, //
			0xA1, 0x02, //
			0x75, 0x08, //
			0x95, 0x05, //
			0x15, 0x00, //
			0x26, 0xFF, //
			0x00, 0x35, // 
			0x00, 0x46, // 
			0xFF, 0x00, // 
			0x09, 0x30, // 
			0x09, 0x30, // 
			0x09, 0x30, // 
			0x09, 0x30, // 
			0x09, 0x31, // 
			0x81, 0x02, //
			0x75, 0x04, //
			0x95, 0x01, //
			0x25, 0x07, //
			0x46, 0x3B, //
			0x01, 0x65, // 
			0x14, 0x09, // 
			0x00, 0x81, // 
			0x42, 0x65, // 
			0x00, 0x75, // 
			0x01, 0x95, // 
			0x0A, 0x25, // 
			0x01, 0x45, // 
			0x01, 0x05, // 
			0x09, 0x19, // 
			0x01, 0x29, // 
			0x0A, 0x81, //
			0x02, 0x06, // 
			0x00, 0xFF, // 
			0x75, 0x01, //
			0x95, 0x0A, // 
			0x25, 0x01, // 
			0x45, 0x01, //
			0x09, 0x01, // 
			0x81, 0x02, // 
			0xC0, 0xA1, //
			0x02, 0x75, // 
			0x08, 0x95, // 
			0x04, 0x46, //
			0xFF, 0x00, // 
			0x26, 0xFF, // 
			0x00, 0x09, // 
			0x02, 0x91, // 
			0x02, //
			0xC0, // 
			0xC0, //
	};
	static int[] opticalMouse = {
			// this test data capture from  VID = 0x0461 PID = 0x4D22 Manufacturer = null Product = USB Optical Mouse Path = /sys/devices/pci0000:00/0000:00:1d.0/usb2/2-1/2-1:1.0/0003:0461:4D22.0003/hidraw/hidraw1
			0x05, 0x01, //
			0x09, 0x02, //
			0xA1, 0x01, //
			0x09, 0x01, //
			0xA1, 0x00, //
			0x05, 0x09, //
			0x19, 0x01, //
			0x29, 0x03, //
			0x15, 0x00, //
			0x25, 0x01, //
			0x75, 0x01, //
			0x95, 0x03, //
			0x81, 0x02, //
			0x75, 0x05, //
			0x95, 0x01, //
			0x81, 0x01, //
			0x05, 0x01, //
			0x09, 0x30, //
			0x09, 0x31, //
			0x09, 0x38, //
			0x15, 0x81, //
			0x25, 0x7F, //
			0x75, 0x08, //
			0x95, 0x03, //
			0x81, 0x06, //
			0xC0, //
			0xC0, //	
	};

}
