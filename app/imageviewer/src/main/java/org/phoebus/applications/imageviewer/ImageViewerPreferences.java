/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.phoebus.applications.imageviewer;

import org.phoebus.framework.preferences.AnnotatedPreferences;
import org.phoebus.framework.preferences.Preference;

/**
 * Preference settings for Image Viewer app
 *
 * @author Georg Weiss
 */
@SuppressWarnings("nls")
public class ImageViewerPreferences {
    @Preference
    public static String watermark_text;

    static {
        AnnotatedPreferences.initialize(ImageViewerPreferences.class, "/image_viewer_preferences.properties");
    }
}
