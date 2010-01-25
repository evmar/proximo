// Copyright (c) 2010 Evan Martin.  All rights reserved.
// Use of this source code is governed by a BSD-style license that can
// be found in the LICENSE file.

package org.neugierig.proximo;

import org.neugierig.proximo.IUpdateMonitor;

// The bindable interface exposed by UpdateService.
// Allows an object to register interest in receiving updates on
// particular stop.
interface IUpdateService {
  void monitorStop(in String routeId, in String stopId,
                   IUpdateMonitor callback);
}
