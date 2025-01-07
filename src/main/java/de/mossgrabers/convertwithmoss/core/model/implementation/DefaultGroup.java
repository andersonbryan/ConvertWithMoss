// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.core.model.implementation;

import java.util.ArrayList;
import java.util.List;

import de.mossgrabers.convertwithmoss.core.model.IGroup;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;
import de.mossgrabers.convertwithmoss.core.model.enumeration.TriggerType;


/**
 * Default implementation for a group.
 *
 * @author Jürgen Moßgraber
 */
public class DefaultGroup implements IGroup
{
    private List<ISampleZone> samples     = new ArrayList<> ();
    private String            name;
    protected TriggerType     triggerType = TriggerType.ATTACK;


    /**
     * Constructor.
     */
    public DefaultGroup ()
    {
        // Intentionally empty
    }


    /**
     * Constructor.
     *
     * @param name The group's name
     */
    public DefaultGroup (final String name)
    {
        this.name = name;
    }


    /**
     * Constructor.
     *
     * @param samples The group's samples
     */
    public DefaultGroup (final List<ISampleZone> samples)
    {
        this.samples = samples;
    }


    /** {@inheritDoc} */
    @Override
    public String getName ()
    {
        return this.name;
    }


    /** {@inheritDoc} */
    @Override
    public void setName (final String name)
    {
        this.name = name;
    }


    /** {@inheritDoc} */
    @Override
    public List<ISampleZone> getSampleZones ()
    {
        return this.samples;
    }


    /** {@inheritDoc} */
    @Override
    public void setSampleZones (final List<ISampleZone> samples)
    {
        this.samples = samples;
    }


    /** {@inheritDoc} */
    @Override
    public void addSampleZone (final ISampleZone sample)
    {
        this.samples.add (sample);
    }


    /** {@inheritDoc} */
    @Override
    public TriggerType getTrigger ()
    {
        return this.triggerType;
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final TriggerType trigger)
    {
        this.triggerType = trigger;
    }
}
