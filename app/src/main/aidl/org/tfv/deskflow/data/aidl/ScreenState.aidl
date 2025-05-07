package org.tfv.deskflow.data.aidl;
import org.tfv.deskflow.data.aidl.ServerState;

parcelable ScreenState {

	String name;
	ServerState server;

	boolean isActive;
	int width;
	int height;
}