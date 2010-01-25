// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import org.neugierig.proximo.ProximoBus;

// A interface for callbacks from from the UpdateService.
// This function is called repeatedly as predictions are updated.
interface IUpdateMonitor {
  void onNewPredictions(in ProximoBus.Prediction[] predictions);
}

