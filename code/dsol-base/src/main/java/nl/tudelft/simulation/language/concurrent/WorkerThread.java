package nl.tudelft.simulation.language.concurrent;

import org.djutils.logger.CategoryLogger;

/**
 * The WorkerThread is a working thread. The thread sleeps while not interrupted. If interrupted the job.run operation
 * is invoked.
 * <p>
 * Copyright (c) 2002-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved. See for project information <a href="https://simulation.tudelft.nl/" target="_blank">
 * https://simulation.tudelft.nl</a>. The DSOL project is distributed under a three-clause BSD-style license, which can
 * be found at <a href="https://simulation.tudelft.nl/dsol/3.0/license.html" target="_blank">
 * https://simulation.tudelft.nl/dsol/3.0/license.html</a>.
 * </p>
 * @author <a href="mailto:phmjacobs@hotmail.com">Peter H.M. Jacobs</a>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */

public class WorkerThread extends Thread
{
    /** the job to execute. */
    private Runnable job = null;

    /** finalized. */
    private boolean finalized = false;

    /**
     * constructs a new SimulatorRunThread.
     * @param name String; the name of the thread
     * @param job Runnable; the job to run
     */
    public WorkerThread(final String name, final Runnable job)
    {
        super(name);
        this.job = job;
        this.setDaemon(false);
        this.setPriority(Thread.NORM_PRIORITY);
        this.start();
    }

    /**
     * Clean up the worker thread. synchronized method, otherwise it does not own the Monitor on the wait.
     */
    public final synchronized void cleanUp()
    {
        this.finalized = true;
        if (!this.isInterrupted())
        {
            this.notify(); // in case it is in the 'wait' state
        }
        this.job = null;
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized void run()
    {
        while (!this.finalized) // always until finalized
        {
            try
            {
                this.wait(); // as long as possible
            }
            catch (InterruptedException interruptedException)
            {
                if (!this.finalized)
                {
                    this.interrupt(); // set the status to interrupted
                    try
                    {
                        this.job.run();
                    }
                    catch (Exception exception)
                    {
                        CategoryLogger.always().error(exception);
                    }
                    Thread.interrupted();
                }
            }
        }
    }
}
