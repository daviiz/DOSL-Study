/*
 * ReferenceMapInterface.java
 * 
 * Created on April 17, 2002, 7:23 PM Last edited on October 12, 2002
 */
package nl.javel.gisbeans.map;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.net.URL;

/**
 * This interface defines the image as defined in the mapInterface
 * @author <a href="mailto:paul.jacobs@javel.nl">Paul Jacobs </a>
 * @since JDK 1.0
 */
public interface ReferenceMapInterface extends Serializable
{
    /**
     * Getter for property image.
     * @return URL the value of property image.
     */
    public URL getImage();

    /**
     * Setter for property image.
     * @param image java.net.URL; New value of property image.
     */
    public void setImage(java.net.URL image);

    /**
     * Getter for property extent.
     * @return double[] the value of property extent.
     */
    public double[] getExtent();

    /**
     * Setter for property extent.
     * @param extent double[]; New value of property extent.
     */
    public void setExtent(double[] extent);

    /**
     * Getter for property color.
     * @return Color the value of property color.
     */
    public Color getOutlineColor();

    /**
     * Setter for property color.
     * @param color Color; New value of property color.
     */
    public void setOutlineColor(Color color);

    /**
     * Getter for property size.
     * @return Dimension the value of property size.
     */
    public Dimension getSize();

    /**
     * Setter for property size.
     * @param size Dimension; New value of property size.
     */
    public void setSize(Dimension size);

    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public boolean isStatus();

    /**
     * Setter for property status.
     * @param status boolean; New value of property status.
     */
    public void setStatus(boolean status);
}
