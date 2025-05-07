package org.tfv.deskflow.data.aidl;

parcelable Result {
    boolean ok;

    @nullable
    String message;

    int code;

    @nullable
    String detail;
}