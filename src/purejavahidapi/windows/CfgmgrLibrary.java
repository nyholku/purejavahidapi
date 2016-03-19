package purejavahidapi.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

public class CfgmgrLibrary {
	public static int CR_SUCCESS = 0;
	static CfgmgrInterface INSTANCE = (CfgmgrInterface) Native.loadLibrary("CfgMgr32", CfgmgrInterface.class,W32APIOptions.UNICODE_OPTIONS);

	public interface CfgmgrInterface extends Library {
		int CM_Get_Parent(int[] pdnDevInst, int dnDevInst, int ulFlags);

		int CM_Get_Device_ID(int dnDevInst, char[] Buffer, int BufferLen, int ulFlags);

		int CM_Get_Device_ID_Size(int[] pulLen, int dnDevInst, int ulFlags);

	}

	public static int CM_Get_Parent(int[] pdnDevInst, int dnDevInst, int ulFlags) {
		return INSTANCE.CM_Get_Parent(pdnDevInst, dnDevInst, ulFlags);
	}

	public static int CM_Get_Device_ID(int dnDevInst, char[] Buffer, int BufferLen, int ulFlags) {
		return INSTANCE.CM_Get_Device_ID(dnDevInst, Buffer, BufferLen, ulFlags);
	}

	public static int CM_Get_Device_ID_Size(int[] pulLen, int dnDevInst, int ulFlags) {
		return INSTANCE.CM_Get_Device_ID_Size(pulLen, dnDevInst, ulFlags);
	}

}
