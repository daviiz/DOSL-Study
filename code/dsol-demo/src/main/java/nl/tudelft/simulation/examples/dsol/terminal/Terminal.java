package nl.tudelft.simulation.examples.dsol.terminal;

import java.util.Properties;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.model.AbstractDSOLModel;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * A simple model of a container terminal. <br>
 * (c) copyright 2018 <a href="https://simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="https://simulation.tudelft.nl">www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/gpl.html">General Public License (GPL) </a>, no warranty <br>
 * @author <a href="http://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Terminal extends AbstractDSOLModel.TimeDouble<DEVSSimulatorInterface.TimeDouble> implements EventListenerInterface
{
    /** The default serial version UID for serializable classes. */
    private static final long serialVersionUID = 1L;

    /** the ship-full event. */
    public static final EventType READY_EVENT = new EventType("READY_EVENT");

    /** QCs. */
    private int numQC = 5;

    /** AGVs. */
    private int numAGV = 25;

    /** replication nr. */
    private final int rep;

    /** debug info or not. */
    public static final boolean DEBUG = false;

    /** the ship for pub/sub when ready. */
    private Ship ship;

    /**
     * constructor for the Container Terminal.
     * @param simulator the simulator
     * @param rep int; the replication number
     */
    public Terminal(final DEVSSimulatorInterface.TimeDouble simulator, final int rep)
    {
        super(simulator);
        this.rep = rep;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        StreamInterface defaultStream = this.simulator.getReplication().getStream("default");

        Properties properties = this.simulator.getReplication().getTreatment().getProperties();
        this.numQC = Integer.parseInt(properties.getProperty("numQC"));
        this.numAGV = Integer.parseInt(properties.getProperty("numAGV"));
        int numCont = 3000;

        QC qc = new QC(this.simulator, "QC", this.numQC, new DistExponential(defaultStream, 60. / 30.));
        AGV agv = new AGV(this.simulator, "AGV", this.numAGV, new DistTriangular(defaultStream, 7, 9, 14));
        this.ship = new Ship(numCont);

        this.ship.addListener(this, Ship.SHIP_FULL_EVENT);

        for (int c = 0; c < numCont; c++)
        {
            new Container(this.simulator, c, qc, agv, this.ship);
        }
    }

    /**
     * @return the ship for pub/sub
     */
    public Ship getShip()
    {
        return this.ship;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event)
    {
        if (event.getType().equals(Ship.SHIP_FULL_EVENT))
        {
            try
            {
                this.simulator.stop();
                double ready = this.simulator.getSimulatorTime() / 60.0;
                double delayHours = Math.max(0.0, Math.ceil(ready) - 20.0);
                if (DEBUG)
                {
                    System.out.println("Delay = " + delayHours);
                }
                double costs =
                        Math.max(20.0, Math.ceil(ready)) * (300.0 * this.numQC + 12.0 * this.numAGV) + 2500.0 * delayHours;
                if (DEBUG)
                {
                    System.out.println("Costs = " + costs);
                }
                int nrCont = (Integer) event.getContent();
                fireEvent(Terminal.READY_EVENT,
                        new Output(this.numQC, this.numAGV, this.rep, delayHours, costs, ready, nrCont));
            }
            catch (SimRuntimeException exception)
            {
                SimLogger.always().error(exception);
            }
        }
    }

    /** */
    public static class Output
    {
        /** QCs. */
        private final int numQC;

        /** AGVs. */
        private final int numAGV;

        /** replication nr. */
        private final int rep;

        /** delay. */
        private final double delayHours;

        /** costs. */
        private final double costs;

        /** time when ready in hrs. */
        private final double ready;

        /** nr containers handled. */
        private final int nrCont;

        /**
         * /**
         * @param numQC int; qc
         * @param numAGV int; agv
         * @param rep int; replication nr
         * @param delayHours double; delay in hours
         * @param costs double; costs in Euros
         * @param ready double; time when ready in hrs
         * @param nrCont int; nr containers handled
         */
        public Output(final int numQC, final int numAGV, final int rep, final double delayHours, final double costs,
                final double ready, final int nrCont)
        {
            super();
            this.numQC = numQC;
            this.numAGV = numAGV;
            this.rep = rep;
            this.delayHours = delayHours;
            this.costs = costs;
            this.ready = ready;
            this.nrCont = nrCont;
        }

        /**
         * @return numQC
         */
        public final int getNumQC()
        {
            return this.numQC;
        }

        /**
         * @return numAGV
         */
        public final int getNumAGV()
        {
            return this.numAGV;
        }

        /**
         * @return rep
         */
        public final int getRep()
        {
            return this.rep;
        }

        /**
         * @return delayHours
         */
        public final double getDelayHours()
        {
            return this.delayHours;
        }

        /**
         * @return costs
         */
        public final double getCosts()
        {
            return this.costs;
        }

        /**
         * @return ready
         */
        public final double getReady()
        {
            return this.ready;
        }

        /**
         * @return nrCont
         */
        public final int getNrCont()
        {
            return this.nrCont;
        }
    }
}
