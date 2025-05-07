#!/usr/bin/env bash

adb shell settings put secure enabled_accessibility_services \
  "org.tfv.deskflow/org.tfv.deskflow.services.GlobalInputService"
