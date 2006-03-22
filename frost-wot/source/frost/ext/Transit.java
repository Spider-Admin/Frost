/*
Transit.java / Frost
Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of
the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.ext;

import java.util.Vector;

/**
 * Transports any kind of data
 * @author Jan-Thomas Czornack
 * @version 010729
 */
class Transit {

    String string;
    Vector vector;
    Transit transit;


    public void setString(String string) {
    this.string = string;
    }

    public String getString() {
    return string;
    }


    public void setVector(Vector vector) {
    this.vector = vector;
    }

    public Vector getVector() {
    return vector;
    }


    public void setTransit(Transit transit) {
    this.transit = transit;
    }

    public Transit getTransit() {
    return transit;
    }


    /**Constructor*/
    public Transit() {

    }

}
