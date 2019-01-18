package nl.tudelft.simulation.examples.dsol.mm1queue;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.flow.Delay;
import nl.tudelft.simulation.dsol.formalisms.flow.Generator;
import nl.tudelft.simulation.dsol.formalisms.flow.StationInterface;
import nl.tudelft.simulation.dsol.model.AbstractDSOLModel;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterDouble;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterInteger;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simtime.dist.DistContinuousSimTime;
import nl.tudelft.simulation.dsol.simtime.dist.DistContinuousSimulationTime;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.statistics.SimCounter;
import nl.tudelft.simulation.dsol.statistics.SimPersistent;
import nl.tudelft.simulation.dsol.swing.charts.boxAndWhisker.BoxAndWhiskerChart;
import nl.tudelft.simulation.dsol.swing.charts.histogram.Histogram;
import nl.tudelft.simulation.dsol.swing.charts.xy.XYChart;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.statistics.Tally;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * The M/M/1 example as published in Simulation Modeling and Analysis by A.M. Law &amp; W.D. Kelton section 1.4 and 2.4. .
 * <p>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://simulation.tudelft.nl/" target="_blank"> https://simulation.tudelft.nl</a>. The DSOL
 * project is distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://simulation.tudelft.nl/dsol/3.0/license.html" target="_blank">
 * https://simulation.tudelft.nl/dsol/3.0/license.html</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/webstaf/peterja/index.htm">Peter Jacobs </a>
 */
public class MM1QueueModel extends AbstractDSOLModel.TimeDouble<DEVSSimulator.TimeDouble>
{
    /** The default serial version UID for serializable classes. */
    private static final long serialVersionUID = 1L;

    /** the chart for the service time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected XYChart serviceTimeChart;

    /** the Box-and-Whisher chart for the service time. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected BoxAndWhiskerChart serviceTimeBWChart;

    /**
     * constructor for the MM1Queue.
     * @param simulator the simulator
     * @throws InputParameterException on parameter error
     */
    public MM1QueueModel(final DEVSSimulator.TimeDouble simulator) throws InputParameterException
    {
        super(simulator);
        InputParameterMap generatorMap = new InputParameterMap("generator", "Generator", "Generator", 1.0);
        generatorMap.add(new InputParameterDouble("intervalTime", "Average interval time", "Average interval time", 1.0, 1.0));
        generatorMap.add(new InputParameterDouble("startTime", "Generator start time", "Generator start time", 0.0, 2.0));
        generatorMap.add(new InputParameterInteger("batchSize", "Batch size", "batch size", 1, 3.0));
        this.inputParameterMap.add(generatorMap);
        InputParameterMap resourceMap = new InputParameterMap("resource", "Resource", "Resource", 2.0);
        resourceMap.add(new InputParameterDouble("capacity", "Resource capacity", "Resource capacity", 1.0, 1.0));
        resourceMap.add(new InputParameterDouble("serviceTime", "Average service time", "Average service time", 0.8, 2.0));
        this.inputParameterMap.add(resourceMap);

    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        StreamInterface defaultStream = this.simulator.getReplication().getStream("default");

        try
        {
            InputParameterMap parameters = this.simulator.getReplication().getTreatment().getInputParameterMap();

            // The Generator
            Generator.TimeDouble generator = new Generator.TimeDouble(this.simulator, Customer.class, null);
            DistContinuousSimulationTime.TimeDouble intervalTime = new DistContinuousSimulationTime.TimeDouble(
                    new DistExponential(defaultStream, (Double) parameters.get("generator.intervalTime").getCalculatedValue()));
            generator.setInterval(intervalTime);

            DistContinuousSimTime.TimeDouble startTime = new DistContinuousSimTime.TimeDouble(
                    new DistConstant(defaultStream, (Double) parameters.get("generator.startTime").getCalculatedValue()));
            generator.setStartTime(startTime);

            DistDiscrete batchSize = new DistDiscreteConstant(defaultStream,
                    (Integer) parameters.get("generator.batchSize").getCalculatedValue());
            generator.setBatchSize(batchSize);

            // The queue, the resource and the release
            double capacity = (Double) parameters.get("resource.capacity").getCalculatedValue();
            Resource<Double, Double, SimTimeDouble> resource = new Resource<>(this.simulator, capacity);

            // created a resource
            StationInterface.TimeDouble queue = new Seize(this.simulator, resource);
            StationInterface.TimeDouble release = new Release(this.simulator, resource, capacity);

            // The server
            DistContinuousSimulationTime.TimeDouble serviceTime = new DistContinuousSimulationTime.TimeDouble(
                    new DistExponential(defaultStream, (Double) parameters.get("resource.serviceTime").getCalculatedValue()));
            StationInterface.TimeDouble server = new Delay.TimeDouble(this.simulator, serviceTime);

            // The flow
            generator.setDestination(queue);
            queue.setDestination(server);
            server.setDestination(release);

            // Statistics

            new SimCounter<Double, Double, SimTimeDouble>("counting the generator", this.simulator, generator,
                    Generator.CREATE_EVENT);
            SimPersistent.TimeDouble persistent = new SimPersistent.TimeDouble("persistent on service time", this.simulator,
                    release, Release.SERVICE_TIME_EVENT);

            Histogram histogram = new Histogram(this.simulator, "histogram on service time", new double[] {0, 10}, 30);
            histogram.add("histogram on service time", persistent, Tally.SAMPLE_MEAN_EVENT);

            this.serviceTimeChart = new XYChart(this.simulator, "XY chart of service time",
                    new double[] {0, this.simulator.getReplication().getTreatment().getRunLength()}, new double[] {-2, 30});
            this.serviceTimeChart.add(persistent);

            this.serviceTimeBWChart = new BoxAndWhiskerChart(this.simulator, "BoxAndWhisker on serviceTime");
            this.serviceTimeBWChart.add(persistent);
        }
        catch (Exception exception)
        {
            throw new SimRuntimeException(exception);
        }
    }

}
