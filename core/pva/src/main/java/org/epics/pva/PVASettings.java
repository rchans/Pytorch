/*******************************************************************************
 * Copyright (c) 2019-2021 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.epics.pva;

import java.util.logging.Level;
import java.util.logging.Logger;

/** PVA Settings
 *
 *  <p>Settings are first read from Java properties.
 *  If not defined, environment variables are checked.
 *  Falling back to defaults defined in here.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class PVASettings
{
    /** Common logger */
    public static final Logger logger = Logger.getLogger(PVASettings.class.getPackage().getName());

    /** Address list.
     *
     *  <p>May contain space-separated host names or IP addresses.
     *  Each may be followed by ":port", otherwise defaulting to EPICS_PVA_BROADCAST_PORT.
     *  When empty, local subnet is used.
     */
    public static String EPICS_PVA_ADDR_LIST = "";

    /** Add local broadcast addresses to addr list? */
    public static boolean EPICS_PVA_AUTO_ADDR_LIST = true;

    /** PVA client port for sending name searches and receiving beacons */
    public static int EPICS_PVA_BROADCAST_PORT = 5076;

    /** First PVA port used by server */
    public static int EPICS_PVA_SERVER_PORT = 5075;

    /** Local addresses to which server will listen.
     *
     *  First must be an IPv4 and IPv6 address that enables
     *  support for that protocol family.
     *  There can be at most one address for each protocol family.
     *
     *  <pre>
     *  Options for IPv4
     *  0.0.0.0   - Listen to unicasts or broadcasts on any interface
     *  127.0.0.1 - Listen on a specific address
     *
     *  Options for IPv6
     *  The square brackets are optional unless a port is provided
     *  [::]       - Listen on any interface
     *  ::         - Listen on any interface
     *  ::1        - Listen on localhost
     *  [::1]:9876 - Listen on localhost but use non-default port. Require
     *
     *  [fe80:8263:4a27:9ef1%en0] - Listen on a specific address
     *
     *  Next, multicast groups may be added.
     *  Each multicast group must include an interface.
     *  224.0.1.1,1@127.0.0.1     - Listen to local IPv4 multicasts
     *  [ff02::42:1],1@::1        - Listen to local IPv6 multicasts
     *  [ff02::42:1],1@en1        - Listen to IPv6 multicasts on network interface en1
     *  </pre>
     */
    public static String EPICS_PVAS_INTF_ADDR_LIST = "0.0.0.0 [::] 224.0.1.1,1@127.0.0.1 [ff02::42:1],1@::1";

    /** PVA server port for name searches and beacons */
    public static int EPICS_PVAS_BROADCAST_PORT = EPICS_PVA_BROADCAST_PORT;

    /** Multicast address used for the local re-send of IPv4 unicasts */
    public static String EPICS_PVA_MULTICAST_GROUP = "224.0.0.128";

    /** TCP buffer size for sending data
     *
     *  <p>Messages are constructed within this buffer,
     *  so it needs to be pre-configured to hold the maximum
     *  package size.
     */
    // double[8 million] plus some protocol overhead
    public static int EPICS_PVA_SEND_BUFFER_SIZE = 8001000;

    /** Initial TCP buffer size for receiving data
     *
     *  <p>Buffer grows when larger packages are received.
     */
    public static final int EPICS_PVA_RECEIVE_BUFFER_SIZE = 16 * 1024;

    /** UDP maximum send message size (for sending search requests).
     *
     *  <p>MAX_UDP: 1500 (max of ethernet and 802.{2,3} MTU) - 20/40(IPv4/IPv6) - 8(UDP) - some reserve (e.g. IPSEC)
     * (the MTU of Ethernet is currently independent of its speed variant)
     */
    public static final int MAX_UDP_UNFRAGMENTED_SEND = 1440;

    /** UDP maximum receive message size.
     *
     *  <p>MAX_UDP: 65535 (max UDP packet size) - 20/40(IPv4/IPv6) - 8(UDP)
     */
    public static final int MAX_UDP_PACKET = 65487;

    /** (Initial) TCP buffer size */
    public static final int TCP_BUFFER_SIZE = 1024 * 16;

    /** Connection timeout [seconds]
     *
     * <p>When approaching this time without having received a new value,
     * an 'Echo' request is sent. If still no reply, the channel is disconnected.
     */
    public static int EPICS_PVA_CONN_TMO = 30;

    /** Maximum number of array elements shown when printing data */
    public static int EPICS_PVA_MAX_ARRAY_FORMATTING = 256;

    static
    {
        EPICS_PVA_ADDR_LIST = get("EPICS_PVA_ADDR_LIST", EPICS_PVA_ADDR_LIST);
        EPICS_PVA_AUTO_ADDR_LIST = get("EPICS_PVA_AUTO_ADDR_LIST", EPICS_PVA_AUTO_ADDR_LIST);
        EPICS_PVA_SERVER_PORT = get("EPICS_PVA_SERVER_PORT", EPICS_PVA_SERVER_PORT);
        EPICS_PVAS_INTF_ADDR_LIST = get("EPICS_PVAS_INTF_ADDR_LIST", EPICS_PVAS_INTF_ADDR_LIST).trim();
        EPICS_PVA_BROADCAST_PORT = get("EPICS_PVA_BROADCAST_PORT", EPICS_PVA_BROADCAST_PORT);
        EPICS_PVAS_BROADCAST_PORT = get("EPICS_PVAS_BROADCAST_PORT", EPICS_PVAS_BROADCAST_PORT);
        EPICS_PVA_CONN_TMO = get("EPICS_PVA_CONN_TMO", EPICS_PVA_CONN_TMO);
        EPICS_PVA_MAX_ARRAY_FORMATTING = get("EPICS_PVA_MAX_ARRAY_FORMATTING", EPICS_PVA_MAX_ARRAY_FORMATTING);
        EPICS_PVA_SEND_BUFFER_SIZE = get("EPICS_PVA_SEND_BUFFER_SIZE", EPICS_PVA_SEND_BUFFER_SIZE);
    }

    /** Get setting from property, environment or default
     *  @param name Name of setting
     *  @param default_value Default value
     *  @return Effective value
     */
    public static String get(final String name, final String default_value)
    {
        String value = System.getProperty(name);
        if (value != null)
        {
            logger.log(Level.CONFIG, name + " = " + value + " (from property)");
            return value;
        }
        value = System.getenv(name);
        if (value != null)
        {
            logger.log(Level.CONFIG, name + " = " + value + " (from environment)");
            return value;
        }
        logger.log(Level.CONFIG, name + " = " + default_value + " (default)");
        return default_value;
    }

    /** Get setting from property, environment or default
     *  @param name Name of setting
     *  @param default_value Default value
     *  @return Effective value
     */
    public static boolean get(final String name, final boolean default_value)
    {
        String text = get(name, default_value ? "YES" : "NO").toLowerCase();
        return Boolean.parseBoolean(text)  ||  "yes".equals(text);
    }

    /** Get setting from property, environment or default
     *  @param name Name of setting
     *  @param default_value Default value
     *  @return Effective value
     */
    public static int get(final String name, final int default_value)
    {
        return Integer.parseInt(get(name, Integer.toString(default_value)));
    }
}
