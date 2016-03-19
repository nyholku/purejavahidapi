<sup><sub>*"I'm inspired,intrigued, amazed and annoyed by how difficult, complex, rare and long time coming simple cross platform USB IO is!"*</sub></sup>


# Pure Java HID-API

### Summary

PureJavaHidApi is a crossplatform  Application Programming Interface (API) for accessing USB HID devices from Java, so it is a library aimed at programmers, not end users.


PureJavaHidApi is written 100% in Java so it is easy for Java programmers to develop and debug and it requires no native libraries when deployed. 

Native access to the underlaying operating system's USB device interface is provided by the wonderful JNA library which takes away all the pain of compiling and deploying native code.

### Project Status

In someways this is early days but the code is actual daily production use in the <a href="http://www.sparetimelabs.com/eazycnc/welcome/welcome.php" target ="eazycnc"> EazyCNC Project </a> so there is some credibility.

The basic input/output report functionality has been tested on all supported platforms and works but some of the less common features (like feature reports) have not been tested because of lack of suitable test hardware.

At this stage some API breakage as the library matures towards simplicity maybe expected.

### Supported Platforms

* Windows
* Mac OS X 
* Linux

### Basic Functionality

PureJavaHidApi provides the capability to enumerate (find) and open attached USB HID devices and send and receive reports i.e. chucks of bytes.

### Planned Functionality

* Ability to read and parse the report descriptors <sup>(1)</sup>

* Persintent and sensical device path names for all platforms would be great!

<sup>(1)</sup> 
<sub> there is a semi-decent parser in <a href="https://github.com/nyholku/purejavahidapi/tree/master/src/purejavahidapi/hidparser" target="hidparser"> purejavahidapi.hidparser </a> but the ability to read raw descriptor still eludes me.
</sub>

### Documentation

The definitive PureJavaHidApi reference is the <a href="http://nyholku.github.io/purejavahidapi/javadoc/index.html" target="javadoc" > JavaDoc </a>.

### Why HID?

Why would you like to use PureJavaHidApi to access HID devices?

The answer is that you usually don't!

Most HID devices (like Mouse, Keyboard etc) have specific APIs provided by the operating system and you should use those to access them.

However some device which are not really Human Interface Devices in the original intent of the USB HID standard nevertheless represent themselves as HID devices. Devices like ThinkGeek  USB Rocket Launcher or  Oregon Scientific WMR100 weather stations to name two. 

It is specifically for accessing these types of devices that PureJavaHidApi is aimed for.

Now why do they represent represent themselves as HID devices?

For one overwhelming advantage over other class of USB devices: all major operating systems have built in HID drivers which means that no driver installation is required. Hard as it for a programmer to understand driver installation is a major hurdle and can make the difference between making the sale or not.

If you are considering developing a USB device, then incarnating it as a HID device is an option worth considering.

So what is the catch?

HID devices are limited to transferring one 64 byte packet once each 1 msec or 64000 bytes/sec each way. If you need more than that you have take an other route, I suggest you head over to <a href="http://libusb.info"> libusb project </a>.

### Why yet another HID library?

PureJavaHidApi is by no means the only game in town, for example there is <a href="https://github.com/gary-rowe/hid4java" target = "hid4java" > hid4java </a> which incidentally uses JNA just like PureJavaHidApi with the difference that it builds on the  C-library <a href="https://github.com/signal11/hidapi" target="hidapi"> HIDAPI </a>, fortunately it handles the native library distribution and deployment for you, which is great as it can be a chore.

### Code Example

To list all available HID devices use code like:

```java
import purejavahidapi.*;
...
List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
for (HidDeviceInfo info : devList) {
	System.out.printf("VID = 0x%04X PID = 0x%04X Manufacturer = %s Product = %s Path = %s\n", //
		info.getVendorId(), //
		info.getProductId(), //
		info.getManufacturerString(), //
		info.getProductString(), //
		info.getPath());
		}

```

To find a generic USB Gamepad:

```java
import purejavahidapi.*;

List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
HidDeviceInfo devInfo = null;
for (HidDeviceInfo info : devList) {
	if (info.getVendorId() == (short)0x0810 && info.getProductId() == (short)0x0005) {
		devInfo = info;
		break;
		}
	}

```
... and then open and attach an input report listener to it:

```java
HidDevice dev=PureJavaHidApi.openDevice(devInfo.getPath());
dev.setInputReportListener(new InputReportListener() {
	@Override
	public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
		System.out.printf("onInputReport: id %d len %d data ", Id, len);
		for (int i = 0; i < len; i++)
			System.out.printf("%02X ", data[i]);
		System.out.println();
		}
	});

```


### Getting Started


All you need to do is to place the `jna.jar` ja `purejahidapi.jar` in your class path and start coding.

You'll probably do this in an IDE but here is how to compile and run one of the examples from the commands line.

Check out the whole project from github and and go to the examples directory and execute this:


```
java -cp ../bin/purejavahidapi.jar:../lib/jna-4.0.0.jar Example1
```

or this if you are on Windows:

```
java -cp ..\bin\purejavahidapi.jar;..\lib\jna-4.0.0.jar Example1
```


### License 

PureJavaHidApi is BSD licensed but please note it depends on JNA which is LGPL/ASL dual licensed.


### Acknowledgment 

While PureJavaHidApi is totally independent developement from the great <a href="https://github.com/signal11/hidapi" target="hidapi"> HIDAPI </a>  by <a href="http://www.signal11.us" target="signal11"> SIGNAL11 </a>  a lot of the technical and intricate knowledge needed to access HID devices were cherry picked ripe from that project which I gratefully acknowledge.







 
