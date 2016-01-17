package com.dyonovan.neotech.pipes.tiles.item

import java.util

import com.dyonovan.neotech.collections.ISidedWrapper
import com.dyonovan.neotech.pipes.entities.{ResourceEntity, ItemResourceEntity}
import com.dyonovan.neotech.pipes.types.{SimplePipe, SinkPipe}
import com.teambr.bookshelf.common.tiles.traits.{UpdatingTile, Syncable, Inventory}
import com.teambr.bookshelf.util.InventoryUtils
import net.minecraft.inventory.{IInventory, ISidedInventory}
import net.minecraft.item.ItemStack
import net.minecraft.util.{BlockPos, EnumFacing}

/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Paul Davis pauljoda
  * @since August 16, 2015
  */
class ItemSinkPipe extends SinkPipe[ItemStack, ItemResourceEntity] with UpdatingTile {
    val waitingQueue  = new util.ArrayList[ItemResourceEntity]()

    override def canConnect(facing: EnumFacing): Boolean =
        getWorld.getTileEntity(getPos.offset(facing)).isInstanceOf[SimplePipe] || getWorld.getTileEntity(pos.offset(facing)).isInstanceOf[IInventory]

    /**
      * Used to check if this pipe can accept a resource
      *
      * You should not actually change anything, all simulation
      * @param resourceEntity
      * @return
      */
    override def willAcceptResource(resourceEntity: ResourceEntity[_]): Boolean = {
        if(resourceEntity == null || !resourceEntity.isInstanceOf[ItemResourceEntity] || resourceEntity.resource == null )
            return false

        val resource = resourceEntity.asInstanceOf[ItemResourceEntity]

        val tempInventory = new Inventory() {
            override var inventoryName: String = "TEMPINV"
            override def hasCustomName(): Boolean = false
            override def initialSize: Int = 1
        }

        tempInventory.setInventorySlotContents(0, resource.resource.copy())

        //Try and insert the stack
        for(dir <- EnumFacing.values()) {
            worldObj.getTileEntity(pos.offset(dir)) match {
                case otherInv : IInventory =>
                    if(InventoryUtils.moveItemInto(tempInventory, 0, addAllWaiting(otherInv, dir), -1, 64, dir, doMove = false, canStack = true) > 0) {
                        waitingQueue.add(resource)
                        return true
                    }
                case _ =>
            }
        }
        false
    }

    def addAllWaiting(otherInv : IInventory, dir : EnumFacing): IInventory = {
        if(waitingQueue.isEmpty)
            otherInv
        else {
            val iterator = waitingQueue.iterator() //Remove deads
            while (iterator.hasNext) {
                if (iterator.next().isDead)
                    iterator.remove()
            }
            var tempInventory : Inventory = null
            otherInv match {
                case sidedInv: ISidedInventory =>
                    tempInventory = new ISidedWrapper(sidedInv) {
                        override var inventoryName: String = "TEMPINV"

                        override def hasCustomName(): Boolean = false

                        override def initialSize: Int = otherInv.getSizeInventory
                    }
                case _ =>
                    tempInventory = new Inventory() {
                        override var inventoryName: String = "TEMPINV"

                        override def hasCustomName(): Boolean = false

                        override def initialSize: Int = otherInv.getSizeInventory

                    }
                    tempInventory.copyFrom(otherInv)
            }

            for(x <- 0 until waitingQueue.size) {

                val tempInventoryUs = new Inventory() {
                    override var inventoryName: String = "TEMPINV"
                    override def hasCustomName(): Boolean = false
                    override def initialSize: Int = 1
                }

                val resource = waitingQueue.get(x)

                tempInventoryUs.setInventorySlotContents(0, resource.resource.copy())
                InventoryUtils.moveItemInto(tempInventoryUs, 0, tempInventory, -1, 64, dir, doMove = true, canStack = true)
            }
            tempInventory
        }
    }

    override def tryInsertResource(resource : ItemResourceEntity) : Unit = {
        val tempInventory = new Inventory() {
            override var inventoryName: String = "TEMPINV"
            override def hasCustomName(): Boolean = false
            override def initialSize: Int = 1
        }

        tempInventory.setInventorySlotContents(0, resource.resource)

        //Try and insert the stack
        for(dir <- EnumFacing.values()) {
            worldObj.getTileEntity(pos.offset(dir)) match {
                case otherInv : IInventory =>
                    if(InventoryUtils.moveItemInto(tempInventory, 0, otherInv, -1, 64, dir, doMove = true, canStack = true) > 0) {
                        resource.resource = tempInventory.getStackInSlot(0)
                        if(resource.resource == null || resource.resource.stackSize <= 0) {
                            resource.isDead = true
                            resource.resource = null
                            waitingQueue.remove(resource)
                        }
                    }
                case _ =>
            }
        }

        //If we couldn't fill, move back to source
        if(!resource.isDead) {
            val tempLocation = new BlockPos(resource.from)
            resource.from = new BlockPos(pos)
            resource.destination = new BlockPos(tempLocation)
            resource.findPathToDestination()
        }
    }

    var coolDown = 0
    override def onServerTick: Unit = {
        coolDown -= 1
        if(coolDown < 0) {
            coolDown = 200
            if (!waitingQueue.isEmpty) {
                val iterator = waitingQueue.iterator()
                while (iterator.hasNext) {
                    if (iterator.next().isDead)
                        iterator.remove()
                }
            }
        }
    }
}
