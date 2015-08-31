package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.core.network.Network;

/**
 * An event used to signal network elements of updated variables inside the network.
 * @author rubensworks
 */
public class VariableContentsUpdatedEvent extends NetworkEvent {

    public VariableContentsUpdatedEvent(Network network) {
        super(network);
    }

}
