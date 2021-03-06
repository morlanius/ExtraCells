package extracells.blocks;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Materials;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tileentity.TileEntityMonitorStorageFluid;

public class BlockMonitorStorageFluid extends RotatableColorBlock
{

	@SideOnly(Side.CLIENT)
	Icon frontIcon;
	@SideOnly(Side.CLIENT)
	Icon topIcon;
	@SideOnly(Side.CLIENT)
	Icon sideIcon;
	@SideOnly(Side.CLIENT)
	Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	public Icon baseLayer;
	@SideOnly(Side.CLIENT)
	public Icon[] colorLayers;

	public BlockMonitorStorageFluid(int id)
	{
		super(id, Material.rock);
		setCreativeTab(extracells.Extracells.ModTab);
		setUnlocalizedName("block.fluid.monitor.storage");
		setHardness(2.0F);
		setResistance(10.0F);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		super.onBlockActivated(world, x, y, z, player, side, offsetX, offsetY, offsetZ);
		if (!world.isRemote)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityMonitorStorageFluid)
			{
				TileEntityMonitorStorageFluid monitorTE = (TileEntityMonitorStorageFluid) te;
				ItemStack currItem = player.getCurrentEquippedItem();
				if (currItem != null)
				{
					if (!monitorTE.isMatrixed() && currItem.isItemEqual(Materials.matConversionMatrix))
					{
						monitorTE.setMatrixed();
						currItem.stackSize -= 1;
						if (currItem.stackSize <= 0)
							currItem = null;
						return true;
					}
					if (!monitorTE.isLocked())
					{
						if (currItem.getItem() instanceof IFluidContainerItem)
						{
							FluidStack fluid = ((IFluidContainerItem) currItem.getItem()).getFluid(currItem);
							monitorTE.setFluid(fluid != null ? fluid.getFluid() : null);
						} else if (FluidContainerRegistry.isFilledContainer(currItem))
						{
							FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(currItem);
							monitorTE.setFluid(fluid != null ? fluid.getFluid() : null);
						} else if (FluidContainerRegistry.isEmptyContainer(currItem))
						{
							monitorTE.setFluid(null);
						}
					} else
					{
						if (monitorTE.isMatrixed())
						{
							ItemStack toAdd = monitorTE.fillContainer(currItem.copy());
							if (toAdd != null)
							{
								ForgeDirection orientation = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
								dropBlockAsItem_do(world, x + orientation.offsetX, y + orientation.offsetY, z + orientation.offsetZ, toAdd);
								currItem.stackSize -= 1;
								if (currItem.stackSize <= 0)
									currItem = null;
							}
						}
					}
				} else if (player.isSneaking())
				{
					if (!monitorTE.isLocked())
					{
						monitorTE.setLocked(true);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.Locked")));
					} else
					{
						monitorTE.setLocked(false);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.Unlocked")));
					}
				} else if (!player.isSneaking() && !monitorTE.isLocked())
				{
					monitorTE.setFluid(null);
				}
			}
		}
		return true;
	}

	@Override
	public int getRenderType()
	{
		return Extracells.renderID;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMonitorStorageFluid();
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		frontIcon = iconregister.registerIcon("extracells:fluid.monitor.hotbar");
		sideIcon = iconregister.registerIcon("extracells:machine.side");
		bottomIcon = iconregister.registerIcon("extracells:machine.bottom");
		topIcon = iconregister.registerIcon("extracells:machine.top");
		baseLayer = iconregister.registerIcon("extracells:fluid.monitor.layerbase");
		colorLayers = new Icon[]
		{ iconregister.registerIcon("extracells:fluid.monitor.layer3"), iconregister.registerIcon("extracells:fluid.monitor.layer2"), iconregister.registerIcon("extracells:fluid.monitor.layer1") };
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return side == 3 ? frontIcon : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity tileentity = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);

		if (tileentity != null)
		{
			return side == metadata ? baseLayer : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
		}
		return null;
	}

	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> items = super.getBlockDropped(world, x, y, z, metadata, fortune);
		TileEntity blockTE = world.getBlockTileEntity(x, y, z);
		if (blockTE instanceof TileEntityMonitorStorageFluid)
		{
			if (((TileEntityMonitorStorageFluid) blockTE).isMatrixed())
			{
				items.add(Materials.matConversionMatrix.copy());
			}
		}
		return items;
	}
}
