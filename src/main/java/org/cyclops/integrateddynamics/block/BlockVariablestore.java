package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainerGui;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.block.IVariableContainerFacade;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.client.gui.GuiVariablestore;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerVariablestore;
import org.cyclops.integrateddynamics.network.VariablestoreNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

import java.util.Collection;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockVariablestore extends ConfigurableBlockContainerGui implements ICableNetwork<CablePathElement>,
        INetworkElementProvider, IVariableContainerFacade {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockVariablestore _instance = null;

    //@Delegate <- Lombok can't handle delegations with generics, so we'll have to do it manually...
    private CableNetworkComponent<BlockVariablestore> cableNetworkComponent = new CableNetworkComponent<>(this);
    private NetworkElementProviderComponent networkElementProviderComponent = new NetworkElementProviderComponent(this);

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockVariablestore getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockVariablestore(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileVariablestore.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && WrenchHelpers.isWrench(player, pos) && player.isSneaking()) {
            world.destroyBlock(pos, !player.capabilities.isCreativeMode);
            return true;
        }
        return super.onBlockActivated(world, pos, state, player , side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                     int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        cableNetworkComponent.addToNetwork(world, pos);
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        cableNetworkComponent.onPostBlockDestroyed(world, pos);
    }

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement>newHashSet(new VariablestoreNetworkElement(DimPos.of(world, blockPos)));
    }

    @Override
    public IVariableContainer getVariableContainer(World world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, IVariableContainer.class);
    }

    /* --------------- Delegate to ICableNetwork<CablePathElement> --------------- */

    @Override
    public void initNetwork(World world, BlockPos pos) {
        cableNetworkComponent.initNetwork(world, pos);
    }

    @Override
    public boolean canConnect(World world, BlockPos selfPosition, ICable connector, EnumFacing side) {
        return cableNetworkComponent.canConnect(world, selfPosition, connector, side);
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        cableNetworkComponent.updateConnections(world, pos);
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        return cableNetworkComponent.isConnected(world, pos, side);
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.disconnect(world, pos, side);
    }

    @Override
    public void reconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.reconnect(world, pos, side);
    }

    @Override
    public void resetCurrentNetwork(World world, BlockPos pos) {
        cableNetworkComponent.resetCurrentNetwork(world, pos);
    }

    @Override
    public void setNetwork(INetwork network, World world, BlockPos pos) {
        cableNetworkComponent.setNetwork(network, world, pos);
    }

    @Override
    public INetwork getNetwork(World world, BlockPos pos) {
        return cableNetworkComponent.getNetwork(world, pos);
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return cableNetworkComponent.createPathElement(world, blockPos);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerVariablestore.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiVariablestore.class;
    }
}