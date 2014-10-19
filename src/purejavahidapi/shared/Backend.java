package purejavahidapi.shared;

import java.io.IOException;
import java.util.List;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;

public interface Backend {
	void init();

	void cleanup();

	List<HidDeviceInfo> enumerateDevices(short vendorId, short productId);

	HidDevice openDevice(String path, Frontend frontEnd) throws IOException;
}