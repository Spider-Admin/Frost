package frost.ext;

import java.util.*;

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
