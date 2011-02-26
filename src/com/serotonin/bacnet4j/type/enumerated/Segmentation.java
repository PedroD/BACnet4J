/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.bacnet4j.type.enumerated;

import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.util.queue.ByteQueue;

public class Segmentation extends Enumerated {
    private static final long serialVersionUID = -7301676227213297779L;
    public static final Segmentation segmentedBoth = new Segmentation(0);
    public static final Segmentation segmentedTransmit = new Segmentation(1);
    public static final Segmentation segmentedReceive = new Segmentation(2);
    public static final Segmentation noSegmentation = new Segmentation(3);

    public static final Segmentation[] ALL = { segmentedBoth, segmentedTransmit, segmentedReceive, noSegmentation, };

    public Segmentation(int value) {
        super(value);
    }

    public Segmentation(ByteQueue queue) {
        super(queue);
    }

    public boolean hasTransmitSegmentation() {
        return this.equals(segmentedBoth) || this.equals(segmentedTransmit);
    }

    public boolean hasReceiveSegmentation() {
        return this.equals(segmentedBoth) || this.equals(segmentedReceive);
    }

    @Override
    public String toString() {
        int type = intValue();
        if (type == segmentedBoth.intValue())
            return "both";
        if (type == segmentedTransmit.intValue())
            return "transmit";
        if (type == segmentedReceive.intValue())
            return "receive";
        if (type == noSegmentation.intValue())
            return "none";
        return "Unknown: " + type;
    }
}
