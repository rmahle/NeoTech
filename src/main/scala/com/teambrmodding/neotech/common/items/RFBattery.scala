package com.teambrmodding.neotech.common.items

import java.util

import cofh.api.energy.IEnergyContainerItem
import com.teambrmodding.neotech.NeoTech
import com.teambrmodding.neotech.lib.Reference
import com.teambrmodding.neotech.managers.ItemManager
import com.teambrmodding.neotech.tools.upgradeitems.BaseUpgradeItem
import com.teambrmodding.neotech.utils.ClientUtils
import com.teambr.bookshelf.common.items.traits.ItemBattery
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable.ArrayBuffer

/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Dyonovan
  * @since 2/17/2016
  */
class RFBattery(name: String, tier: Int) extends BaseUpgradeItem("battery", 1) with ItemBattery {

    setMaxStackSize(1)
    setCreativeTab(NeoTech.tabNeoTech)
    setMaxStackSize(maxStackSize)
    setUnlocalizedName(Reference.MOD_ID + ":" + name)
    
    override def getTexturesToStitch: ArrayBuffer[String] = ArrayBuffer("neotech:items/basicRFBattery",
        "neotech:items/advancedRFBattery", "neotech:items/eliteRFBattery")

    override def getTextures(stack : ItemStack): java.util.List[String] = {
        val list = new util.ArrayList[String]()
        stack.getItem match {
            case item : ItemManager.basicRFBattery.type => list.add("neotech:items/basicRFBattery")
            case item : ItemManager.advancedRFBattery.type => list.add("neotech:items/advancedRFBattery")
            case item : ItemManager.eliteRFBattery.type => list.add("neotech:items/eliteRFBattery")
            case _ => list.add("neotech:items/basicRFBattery")
        }
        list
    }

    override def onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean): Unit = {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected)
        entityIn match {
            case player: EntityPlayer if getEnergyStored(stack) > 0 =>
                for (x <- 0 until player.inventory.getSizeInventory) {
                    if (player.inventory.getStackInSlot(x) != null &&
                            player.inventory.getStackInSlot(x).getItem.isInstanceOf[IEnergyContainerItem] &&
                            !player.inventory.getStackInSlot(x).getItem.isInstanceOf[RFBattery]) {
                        val energyContainerItem = player.inventory.getStackInSlot(x).getItem.asInstanceOf[IEnergyContainerItem]
                        val amount =
                            extractEnergy(stack,
                                energyContainerItem.receiveEnergy(player.inventory.getStackInSlot(x), stack.getTagCompound.getInteger("MaxExtract"), false),
                                simulate = false)
                        if (amount > 0) {
                            extractEnergy(stack,
                                energyContainerItem.receiveEnergy(player.inventory.getStackInSlot(x), amount, false),
                                simulate = false)
                        }
                    }
                }
            case _ =>
        }
    }

    override def setDefaultTags(stack: ItemStack): Unit = {
        var tier = 1
        stack.getItem match {
            case ItemManager.basicRFBattery => tier = 1
            case ItemManager.advancedRFBattery => tier = 2
            case ItemManager.eliteRFBattery => tier = 3
        }
        val tagCompound = new NBTTagCompound
        val energy = getTierPower(tier)
        tagCompound.setInteger("EnergyCapacity", energy._1)
        tagCompound.setInteger("MaxExtract", energy._2)
        tagCompound.setInteger("MaxReceive", energy._2)
        tagCompound.setInteger("Tier", tier)
        stack.setTagCompound(tagCompound)

    }

    /**
      * Defines amount of power each tier holds
      *
      * @param t Battery Tier
      * @return Touple2(capacity, maxReceive)
      */
    def getTierPower(t: Int): (Int, Int) = {
        t match {
            case 1 => (25000, 200)
            case 2 => (100000, 1000)
            case 3 => (1000000, 10000)
            case _ => (0, 0)
        }
    }

    @SideOnly(Side.CLIENT)
    override def addInformation(stack: ItemStack, player: EntityPlayer, list: java.util.List[String], boolean: Boolean): Unit = {
        val amount = getTierPower(tier)
        list.add(ClientUtils.formatNumber(getEnergyStored(stack)) + " / " + ClientUtils.formatNumber(amount._1) + " RF")
    }

    /**
      * Can this upgrade item allow more to be applied to the item
      *
      * @param stack The stack we want to apply to, get count from there
      * @param count The stack size of the input
      * @return True if there is space for the entire count
      */
    override def canAcceptLevel(stack: ItemStack, count: Int, name: String): Boolean = true

    /**
      * Use this to put information onto the stack, called when put onto the stack
      *
      * @param stack The stack to put onto
      * @return The tag passed
      */
    override def writeInfoToNBT(stack: ItemStack, tag: NBTTagCompound, writingStack: ItemStack): Unit = {
        stack.getTagCompound.setInteger("EnergyCapacity", writingStack.getTagCompound.getInteger("EnergyCapacity"))
        stack.getTagCompound.setInteger("MaxReceive", writingStack.getTagCompound.getInteger("MaxReceive"))
        stack.getTagCompound.setInteger("MaxExtract", writingStack.getTagCompound.getInteger("MaxExtract"))
    }
}