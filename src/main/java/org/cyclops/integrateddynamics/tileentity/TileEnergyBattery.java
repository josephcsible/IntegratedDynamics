package org.cyclops.integrateddynamics.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryBase;
import org.cyclops.integrateddynamics.block.BlockEnergyBatteryConfig;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectable;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;

/**
 * A part entity used to store variables.
 * Internally, this also acts as an expression cache
 * @author rubensworks
 */
public class TileEnergyBattery extends TileCableConnectable implements IEnergyStorageCapacity {

    @NBTPersist
    private int energy;
    @NBTPersist(useDefaultValue = false)
    private int capacity = BlockEnergyBatteryConfig.capacity;

    public TileEnergyBattery() {
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(CapabilityEnergy.ENERGY, this);
    }

    protected boolean isCreative() {
        return ((BlockEnergyBatteryBase) getBlock()).isCreative();
    }

    @Override
    public int getEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        if(isCreative()) return Integer.MAX_VALUE;
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void updateBlockState() {
        if(!isCreative()) {
            IBlockState blockState = getWorld().getBlockState(getPos());
            if (blockState.getBlock() == BlockEnergyBattery.getInstance()) {
                int fill = Math.max(0, (int) Math.floor(((float) energy * (BlockEnergyBattery.FILL.getAllowedValues().size() - 1)) / (float) getMaxEnergyStored()));
                if (blockState.getValue(BlockEnergyBattery.FILL) != fill) {
                    getWorld().setBlockState(getPos(), blockState.withProperty(BlockEnergyBattery.FILL, fill));
                    sendUpdate();
                }
            }
        }
    }

    protected void setEnergy(int energy) {
        if(!isCreative()) {
            int lastEnergy = this.energy;
            if (lastEnergy != energy) {
                this.energy = energy;
                updateBlockState();
                markDirty();
            }
        }
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        if(!isCreative()) {
            energy = Math.max(0, Math.min(energy, BlockEnergyBatteryConfig.energyPerTick));
            int stored = getEnergyStored();
            int newEnergy = Math.min(stored + energy, getMaxEnergyStored());
            if(!simulate) {
                setEnergy(newEnergy);
            }
            return newEnergy - stored;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        if(isCreative()) return energy;
        energy = Math.max(0, Math.min(energy, BlockEnergyBatteryConfig.energyPerTick));
        int stored = getEnergyStored();
        int newEnergy = Math.max(stored - energy, 0);
        if(!simulate) {
            setEnergy(newEnergy);
        }
        return stored - newEnergy;
    }

    protected int addEnergy(int energy) {
        int filled = addEnergyFe(energy, false);
        extractEnergy(filled, false);
        return filled;
    }

    protected int addEnergyFe(int energy, boolean simulate) {
        return EnergyHelpers.fillNeigbours(getWorld(), getPos(), energy, simulate);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (!getWorld().isRemote && getEnergyStored() > 0 && getWorld().isBlockPowered(getPos())) {
            addEnergy(Math.min(BlockEnergyBatteryConfig.energyPerTick, getEnergyStored()));
        }
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
