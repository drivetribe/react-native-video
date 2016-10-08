/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brentvatne.exoplayer;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

class DataSourceUtil {

    private DataSourceUtil() {
    }

    private static String userAgent = null;

    private static String getUserAgent(Context context) {
        if (userAgent == null) {
            Util.getUserAgent(context.getApplicationContext(), "ReactNativeVideo");
        }
        return userAgent;
    }

    static DataSource.Factory buildDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter) {
        Context appContext = context.getApplicationContext();
        return new DefaultDataSourceFactory(appContext, bandwidthMeter,
                buildHttpDataSourceFactory(appContext, bandwidthMeter));
    }

    static HttpDataSource.Factory buildHttpDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(getUserAgent(context), bandwidthMeter);
    }

}
