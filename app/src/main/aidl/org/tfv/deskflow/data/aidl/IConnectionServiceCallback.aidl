package org.tfv.deskflow.data.aidl;
import org.tfv.deskflow.data.aidl.ConnectionState;

oneway interface IConnectionServiceCallback {

		void onStateChanged(in ConnectionState state);

		void onMessage(in Bundle bundle);
}

