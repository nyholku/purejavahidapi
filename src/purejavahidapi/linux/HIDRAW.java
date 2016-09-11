/*
 * Copyright (c) 2016, Kustaa Nyholm / SpareTimeLabs
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

import static purejavahidapi.linux.IOCtl.*;

public class HIDRAW {
	 public static int HIDIOCSFEATURE(int len) {
		 return _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x06, len);
	 }
	 public static int HIDIOCGFEATURE(int len)   {
		 return _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len);
	 }

	
	
	/*
	struct hidraw_report_descriptor {
		 22         __u32 size;
		 23         __u8 value[HID_MAX_DESCRIPTOR_SIZE];
		 24 };
		 25 
		 26 struct hidraw_devinfo {
		 27         __u32 bustype;
		 28         __s16 vendor;
		 29         __s16 product;
		 30 };
		 31 
		 32 /* ioctl interface 
		 33 #define HIDIOCGRDESCSIZE        _IOR('H', 0x01, int)
		 34 #define HIDIOCGRDESC            _IOR('H', 0x02, struct hidraw_report_descriptor)
		 35 #define HIDIOCGRAWINFO          _IOR('H', 0x03, struct hidraw_devinfo)
		 36 #define HIDIOCGRAWNAME(len)     _IOC(_IOC_READ, 'H', 0x04, len)
		 37 #define HIDIOCGRAWPHYS(len)     _IOC(_IOC_READ, 'H', 0x05, len)
		 38 /* The first byte of SFEATURE and GFEATURE is the report number 
		 39 #define HIDIOCSFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x06, len)
		 40 #define HIDIOCGFEATURE(len)    _IOC(_IOC_WRITE|_IOC_READ, 'H', 0x07, len)
		 41 
		 42 #define HIDRAW_FIRST_MINOR 0
		 43 #define HIDRAW_MAX_DEVICES 64
		 44 /* number of reports to buffer 
		 45 #define HIDRAW_BUFFER_SIZE 64
	*/	 
}
