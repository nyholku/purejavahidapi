import java.util.List;

import purejavahidapi.*;

public class Example1 {

	public static void main(String[] args) {
		try {
			List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
			HidDevice dev = null;
			for (HidDeviceInfo info : devList) {
				if (info.getVendorId() == 0x0810 && info.getProductId() == 0x0005) {
					dev = PureJavaHidApi.openDevice(info.getPath());
					break;
				}
			}
			if (dev == null)
				System.err.println("device not found");
			else {
				dev.setInputReportListener(new InputReportListener() {
					@Override
					public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
						System.out.printf("onInputReport: id %d len %d data ", Id, len);
						for (int i = 0; i < len; i++)
							System.out.printf("%02X ", data[i]);
						System.out.println();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
