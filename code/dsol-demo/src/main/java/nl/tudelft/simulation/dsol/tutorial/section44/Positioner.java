package nl.tudelft.simulation.dsol.tutorial.section44;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.formalisms.dess.DifferentialEquation;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DESSSimulatorInterface;

/**
 * A Positioner.
 * <p>
 * Copyright (c) 2002-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved. See for project information <a href="https://simulation.tudelft.nl/" target="_blank">
 * https://simulation.tudelft.nl</a>. The DSOL project is distributed under a three-clause BSD-style license, which can
 * be found at <a href="https://simulation.tudelft.nl/dsol/3.0/license.html" target="_blank">
 * https://simulation.tudelft.nl/dsol/3.0/license.html</a>.
 * </p>
 * @author <a href="https://www.linkedin.com/in/peterhmjacobs">Peter Jacobs </a>
 */
public class Positioner extends DifferentialEquation<Double, Double, SimTimeDouble>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * constructs a new Positioner.
     * @param simulator DESSSimulatorInterface.TimeDouble; the simulator
     * @throws RemoteException Exception
     */
    public Positioner(final DESSSimulatorInterface.TimeDouble simulator) throws RemoteException
    {
        super(simulator);
        this.initialize(0.0, new double[]{0.0, 0.0});
    }

    /**
     * sets the value.
     * @param value double; the new value
     */
    public void setValue(final double value)
    {
        super.initialize(this.simulator.getSimulatorTime(), new double[]{value, 0.0});
    }

    /** {@inheritDoc} */
    @Override
    public double[] dy(final double x, final double[] y)
    {
        double[] dy = new double[2];
        dy[0] = y[1]; // v(t) = a(t)
        dy[1] = 0.5; // a(t) = constant
        return dy;
    }
}
