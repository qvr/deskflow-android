package org.tfv.deskflow.data.aidl;

import org.tfv.deskflow.data.aidl.ConnectionState;
import org.tfv.deskflow.data.aidl.ScreenState;
import org.tfv.deskflow.data.aidl.IConnectionServiceCallback;
import org.tfv.deskflow.data.aidl.Result;
interface IConnectionService {

		void registerCallback(IConnectionServiceCallback callback);
		void unregisterCallback(IConnectionServiceCallback callback);

        Result setLogForwardingLevel(in String levelName);

        Result setCaptureKeyMode(in boolean enabled);

        Result setEnabled(in boolean enabled);

        Result updateScreenState(in ScreenState screenState);

        Result setClipboardData(in Bundle bundle);

        Result regenerateClientCertificate();

        ConnectionState getState();
}
