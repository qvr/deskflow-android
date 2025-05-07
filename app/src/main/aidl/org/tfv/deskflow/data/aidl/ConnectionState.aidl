package org.tfv.deskflow.data.aidl;
import org.tfv.deskflow.data.aidl.ScreenState;

parcelable ConnectionState {
	boolean isEnabled;
	boolean isConnected;
	boolean ackReceived;
    long connectionTimestamp;
    ScreenState screen;

    boolean isCaptureKeyModeEnabled;

}
