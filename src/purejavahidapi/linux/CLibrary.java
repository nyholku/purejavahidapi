package purejavahidapi.linux;

import java.util.Arrays;
import java.util.List;

import purejavahidapi.linux.UdevLibrary.hidraw_report_descriptor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public class CLibrary {
	static CLibraryInterface INSTANCE = (CLibraryInterface) Native.load("c", CLibraryInterface.class);

	public static short POLLIN = 0x0001;

	interface CLibraryInterface extends Library {
		int open(String pathname, int flags);

		int close(int fd);

		int read(int fd, byte[] data, NativeLong len);

		int write(int fd, byte[] data, NativeLong len);

		int ioctl(int fd, int cmd, int[] p);

		int ioctl(int fd, int cmd, byte[] p);

		int ioctl(int fd, int cmd, hidraw_report_descriptor p);

		int poll(pollfd[] fds, int nfds, int timeout);

		public int pipe(int[] fds);
	}

	static public class pollfd extends Structure {

		public static class ByReference extends pollfd implements Structure.ByReference {
		}

		public int fd;
		public short events;
		public short revents;

		@Override
		protected List getFieldOrder() {
			return Arrays.asList(//
					"fd",//
					"events",//
					"revents"//
			);
		}

		public pollfd() {
		}

		public pollfd(int fd, short events, short revents) {
			this.fd = fd;
			this.events = events;
			this.revents = revents;
		}
	}

	public static int open(String pathname, int flags) {
		return INSTANCE.open(pathname, flags);
	}

	public static void close(int fd) {
		INSTANCE.close(fd);
	}

	public static int ioctl(int fd, int cmd, int[] p) {
		return INSTANCE.ioctl(fd, cmd, p);
	}

	public static int ioctl(int fd, int cmd, byte[] p) {
		return INSTANCE.ioctl(fd, cmd, p);
	}

	public static int ioctl(int fd, int cmd, hidraw_report_descriptor p) {
		return INSTANCE.ioctl(fd, cmd, p);
	}

	public static int read(int fd, byte[] buffer, int len) {
		return INSTANCE.read(fd, buffer, new NativeLong(len));
	}

	public static int write(int fd, byte[] buffer, int len) {
		return INSTANCE.write(fd, buffer, new NativeLong(len));
	}

	public static int poll(pollfd fds[], int nfds, int timeout) {
		return INSTANCE.poll(fds, nfds, timeout);
	}

	public static int pipe(int[] fds) {
		return INSTANCE.pipe(fds);
	}

}
