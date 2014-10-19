PureJavaHidApi is a crossplatform  Application Programmin Interface (API) for accessing USB HID devices from Java, so it is a library aimed at programmers, not end users.


PureJavaHidApi is written 100% in Java so it is easy for Java programmers to develop and debug and it requires no native libraries. 

Native access to the underlaying operating system's USB device interface is provided by the wonderful JNA library which takes away all the pain of compiling and deploying native code.

PureJavaHidApi is BSD licensed but please note it depends on JNA which is LGPL/ASL dual licensed.

The definitive PureJavaHidApi reference is the <a href="nyholku.github.io/purejavahidapi/index.html"> JavaDoc </a>.

Why would you like to use PureJavaHidApi to access HID devices?

The answer is that you usually don't!

Most HID devices (like Mouse, Keyboard etc) have specific APIs provided by the operating system and you should use those to access them.

However some device which are not really Human Interface Devices in the original intent of the USB HID standard nevertheless represent themselves as HID devices. Devices like ThinkGeek  USB Rocket Launcher or  Oregon Scientific WMR100 weather stations to name two. 

It is spefifically for accessing these types of devices that PureJavaHidApi is aimed for.

Now why do they represent represent themselves as HID devices?

For one overwhelming advantage over other class of USB devices: all major operating systems have built in HID drivers which means that no driver installation is required. Hard as it for a programmer to understand driver installation is a major hurdle and can make the difference between making the sale or not.

If you are considering developing a USB device then incarnating it as a HID device is an option worth considering.

So what is the catch?

HID devices are limited to transferring one 64 byte packet once each 1 msec or 64000 bytes/sec each way. If you need more than that you have take an other route, I suggest you head over to <a href="http://libusb.info" libusb project </a>.

PureJavaHidApi is by no means the only game in town, for example there is <a href="https://github.com/gary-rowe/hid4java"> hid4java </a> which incidentally uses JNA just like PureJavaHidApi with the crucial difference that it builds on the nice C-library <a href="https://github.com/signal11/hidapi"> HIDAPI </a> which means that need to solve the distribution and deployment of a native library. 



Acknowledgment 

While PureJavaHidApi is totally independent developement from the great <a href="http://www.signal11.us"> SIGNAL11 </a> HIDAPI a lot of the techical and intricate knowledge need to access HID devices we cherry picked ripe from that project which I gratefully acknowledge.







 
