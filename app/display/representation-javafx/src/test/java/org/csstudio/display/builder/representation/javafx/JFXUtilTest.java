/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.csstudio.display.builder.model.properties.HorizontalAlignment;
import org.csstudio.display.builder.model.properties.VerticalAlignment;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.junit.Test;

/** JUnit test of JFXUtil
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXUtilTest
{
    @Test
    public void testRGB()
    {
        assertThat(JFXUtil.webRGB(new WidgetColor(15, 255, 0)), equalTo("#0FFF00"));
        assertThat(JFXUtil.webRGB(new WidgetColor(0, 16, 255)), equalTo("#0010FF"));
    }

    @Test
    public void tetCssFont() {

        // Given a font and prefix
        String prefix = "foobar";
        Font font = Font.font("serif", FontWeight.BOLD, FontPosture.ITALIC,37);

        // When converted
        String actual = JFXUtil.cssFont(prefix, font);

        // Then it matches as expected, note the system lookup finds Serif instead of serif
        // And the individual pieces are broken out instead of on one line
        String expected = "foobar-size: 37px;foobar-family: \"Serif\";foobar-weight: bold;foobar-style: italic;";
        assertEquals(expected, actual);

    }

    @Test
    public void testCssFontShorthand() {

        // Given a font and prefix
        String prefix = "foobar";
        Font font = Font.font("serif", FontWeight.BOLD, FontPosture.ITALIC,37);

        // When converted
        String actual = JFXUtil.cssFontShorthand(prefix, font);

        // Then it matches as expected, note the system lookup finds Serif instead of serif
        String expected = "foobar: bold italic 37px 'Serif';";
        assertEquals(expected, actual);

    }

    @Test
    public void testCssFontShorthandNonexistentFont() {

        // Given a font and prefix
        String prefix = "foobar";
        Font font = Font.font("My Fancy Font That Doesnt Exist", FontWeight.BOLD, FontPosture.ITALIC,37);

        // When converted
        String actual = JFXUtil.cssFontShorthand(prefix, font);

        // Then it matches as expected
        String expected = "foobar: bold italic 37px 'System';";
        assertEquals(expected, actual);

    }

    @Test
    public void testPositionFromHorizAndVertComponents() {
        assertEquals(Pos.TOP_LEFT, JFXUtil.computePos(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        assertEquals(Pos.TOP_CENTER, JFXUtil.computePos(HorizontalAlignment.CENTER, VerticalAlignment.TOP));
        assertEquals(Pos.TOP_RIGHT, JFXUtil.computePos(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));

        assertEquals(Pos.CENTER_LEFT, JFXUtil.computePos(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        assertEquals(Pos.CENTER, JFXUtil.computePos(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE));
        assertEquals(Pos.CENTER_RIGHT, JFXUtil.computePos(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE));

        assertEquals(Pos.BOTTOM_LEFT, JFXUtil.computePos(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM));
        assertEquals(Pos.BOTTOM_CENTER, JFXUtil.computePos(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM));
        assertEquals(Pos.BOTTOM_RIGHT, JFXUtil.computePos(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM));
    }

}