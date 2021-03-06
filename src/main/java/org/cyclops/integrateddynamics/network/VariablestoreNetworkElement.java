package org.cyclops.integrateddynamics.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

/**
 * Network element for variable stores.
 * @author rubensworks
 */
public class VariablestoreNetworkElement extends TileNetworkElement<TileVariablestore> {

    public VariablestoreNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        return NetworkHelpers.getPartNetwork(network).addVariableContainer(getPos());
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        NetworkHelpers.getPartNetwork(network).removeVariableContainer(getPos());
    }

    @Override
    public void setPriority(INetwork network, int priority) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getConsumptionRate() {
        return 4;
    }

    @Override
    protected Class<TileVariablestore> getTileClass() {
        return TileVariablestore.class;
    }
}
