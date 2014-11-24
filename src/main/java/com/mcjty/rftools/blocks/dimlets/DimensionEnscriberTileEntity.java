package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class DimensionEnscriberTileEntity extends GenericTileEntity implements ISidedInventory {

    public static final String CMD_STORE = "store";
    public static final String CMD_EXTRACT = "extract";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionEnscriberContainer.factory, DimensionEnscriberContainer.SIZE_DIMLETS+1);

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimensionEnscriberContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimensionEnscriberContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimensionEnscriberContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public String getInventoryName() {
        return "Enscriber Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    private void storeDimlets() {
        Map<DimletType, List<Integer>> dimletsByType = getDimletTypeListMap();
        ItemStack realizedTab = createRealizedTab(dimletsByType);
        inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB] = realizedTab;

        markDirty();
    }

    /**
     * Create a realized dimension tab by taking a map of ids per type and storing
     * that in the NBT of the realized dimension tab.
     * @param dimletsByType
     * @return
     */
    private ItemStack createRealizedTab(Map<DimletType, List<Integer>> dimletsByType) {
        ItemStack realizedTab = new ItemStack(ModItems.realizedDimensionTab, 1, 0);
        NBTTagCompound tagCompound = new NBTTagCompound();

        for (Map.Entry<DimletType,List<Integer>> me : dimletsByType.entrySet()) {
            Collections.sort(me.getValue());

            int[] arr = new int[me.getValue().size()];
            for (int i = 0 ; i < me.getValue().size() ; i++) {
                arr[i] = me.getValue().get(i);
            }
            NBTTagIntArray tagList = new NBTTagIntArray(arr);

            tagCompound.setTag(me.getKey().getName(), tagList);
        }

        realizedTab.setTagCompound(tagCompound);
        return realizedTab;
    }

    /**
     * Convert the dimlets in the inventory to a map of id's per type.
     * @return
     */
    private Map<DimletType, List<Integer>> getDimletTypeListMap() {
        Map<DimletType,List<Integer>> dimletsByType = new HashMap<DimletType, List<Integer>>();
        for (DimletType type : DimletType.values()) {
            dimletsByType.put(type, new ArrayList<Integer>());
        }

        for (int i = 0 ; i < DimensionEnscriberContainer.SIZE_DIMLETS ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i + DimensionEnscriberContainer.SLOT_DIMLETS];
            if (stack != null && stack.stackSize > 0) {
                int dimletId = stack.getItemDamage();
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(dimletId);
                dimletsByType.get(entry.getType()).add(dimletId);
            }
            inventoryHelper.getStacks()[i + DimensionEnscriberContainer.SLOT_DIMLETS] = null;
        }
        return dimletsByType;
    }

    private void extractDimlets() {
        ItemStack realizedTab = inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB];
        NBTTagCompound tagCompound = realizedTab.getTagCompound();
        if (tagCompound != null) {
            int idx = DimensionEnscriberContainer.SLOT_DIMLETS;
            for (DimletType type : DimletType.values()) {
                if (tagCompound.hasKey(type.getName())) {
                    NBTTagIntArray tagIntArray = (NBTTagIntArray) tagCompound.getTag(type.getName());
                    if (tagIntArray != null) {
                        int[] dimlets = tagIntArray.func_150302_c();
                        for (int id : dimlets) {
                            inventoryHelper.getStacks()[idx++] = new ItemStack(ModItems.knownDimlet, 1, id);
                        }
                    }
                }
            }
        }

        inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB] = new ItemStack(ModItems.emptyDimensionTab);
        markDirty();
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_STORE.equals(command)) {
            storeDimlets();
            return true;
        } else if (CMD_EXTRACT.equals(command)) {
            extractDimlets();
            return true;
        }
        return false;
    }
}
