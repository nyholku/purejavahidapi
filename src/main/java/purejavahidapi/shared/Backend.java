package purejavahidapi.shared;

import java.io.IOException;
import java.util.List;

import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;

public interface Backend {
	void init();

	void cleanup();

	List<HidDeviceInfo> enumerateDevices();

	/**
	 * Scans in blocking mode. 
	 */
	HidDevice openDevice(String path, Frontend frontEnd) throws IOException;

	/**
	 * Scans in non-blocking mode with specified scan interval. 
	 * Only implemented for Windows backend! 
	 */
	default HidDevice openDevice(String path, Frontend frontEnd, long scanIntervalMs) throws IOException{
		throw new UnsupportedOperationException("This backend does not support non-blocking scanning");
	}
}