package com.dyonovan.neotech.registries

import java.util

import com.dyonovan.neotech.NeoTech
import com.dyonovan.neotech.managers.MetalManager
import com.google.gson.reflect.TypeToken
import com.teambr.bookshelf.helper.LogHelper
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{FluidRegistry, FluidStack}
import net.minecraftforge.oredict.OreDictionary

/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Paul Davis <pauljoda>
  * @since 2/16/2016
  */
class CrucibleRecipeRegistry extends AbstractRecipeHandler[CrucibleRecipe, ItemStack, FluidStack] {

    /**
      * Used to get the base name of the files
      *
      * @return
      */
    override def getBaseName: String = "crucible"

    /**
      * This is the current version of the registry, if you update this it will cause the registry to be redone
      *
      * @return
      */
    override def getVersion: Int = 1

    /**
      * Used to get the default folder location
      *
      * @return
      */
    override def getBaseFolderLocation: String = NeoTech.configFolderLocation

    /**
      * Used to get what type token to read from file (Generics don't handle well)
      *
      * @return
      */
    override def getTypeToken: TypeToken[util.ArrayList[CrucibleRecipe]] =
        new TypeToken[util.ArrayList[CrucibleRecipe]]() {}

    /**
      * Used to generate the default values
      */
    def generateDefaultRecipes(): Unit = {
        LogHelper.info("Json not found. Creating Dynamic Crucible Recipe List...")

        // Metals
        val iterator = MetalManager.metalRegistry.keySet().iterator()
        while(iterator.hasNext) {
            val metal = MetalManager.metalRegistry.get(iterator.next())
            // Crucible Recipes
            if (metal.fluid.isDefined) {
                //Block - 1296mb
                if (metal.block.isDefined)
                    addCrucibleRecipe(null, metal.block.get.getName, new FluidStack(metal.fluid.get, MetalManager.BLOCK_MB))

                //Ore - 432mb
                if (metal.oreBlock.isDefined)
                    addCrucibleRecipe(null, metal.oreBlock.get.getName, new FluidStack(MetalManager.getMetal("dirty" + metal.oreDict).get.fluid.get, MetalManager.ORE_MB))

                //Ingot - 144mb
                if (metal.ingot.isDefined)
                    addCrucibleRecipe(null, metal.ingot.get.getName, new FluidStack(metal.fluid.get, MetalManager.INGOT_MB))

                //Dust - 76mb
                if(metal.dust.isDefined)
                    addCrucibleRecipe(null, metal.dust.get.getName, new FluidStack(metal.fluid.get, MetalManager.DUST_MB))

                //Nugget - 16mb
                if (metal.nugget.isDefined)
                    addCrucibleRecipe(null, metal.nugget.get.getName, new FluidStack(metal.fluid.get, MetalManager.NUGGET_MB))
            }
        }

        // Iron
        addCrucibleRecipe(null, "ingotIron", new FluidStack(MetalManager.getMetal("iron").get.fluid.get, MetalManager.INGOT_MB))
        addCrucibleRecipe(null, "oreIron", new FluidStack(MetalManager.getMetal("dirtyiron").get.fluid.get, MetalManager.ORE_MB))
        addCrucibleRecipe(null, "blockIron", new FluidStack(MetalManager.getMetal("iron").get.fluid.get, MetalManager.BLOCK_MB))

        // Gold
        addCrucibleRecipe(null, "nuggetGold", new FluidStack(MetalManager.getMetal("gold").get.fluid.get, MetalManager.NUGGET_MB))
        addCrucibleRecipe(null, "ingotGold", new FluidStack(MetalManager.getMetal("gold").get.fluid.get, MetalManager.INGOT_MB))
        addCrucibleRecipe(null, "oreGold", new FluidStack(MetalManager.getMetal("dirtygold").get.fluid.get, MetalManager.ORE_MB))
        addCrucibleRecipe(null, "blockGold", new FluidStack(MetalManager.getMetal("gold").get.fluid.get, MetalManager.BLOCK_MB))

        // Ice/Snowball to Water
        addCrucibleRecipe(new ItemStack(Items.snowball), "", new FluidStack(FluidRegistry.WATER, 144))
        addCrucibleRecipe(new ItemStack(Blocks.ice), "", new FluidStack(FluidRegistry.WATER, 1296))
        addCrucibleRecipe(new ItemStack(Blocks.packed_ice), "", new FluidStack(FluidRegistry.WATER, 1296))

        // Stones to lava
        addCrucibleRecipe(null, "cobblestone", new FluidStack(FluidRegistry.LAVA, 20))
        addCrucibleRecipe(null, "stone", new FluidStack(FluidRegistry.LAVA, 40))

        saveToFile()
        LogHelper.info("Finished adding " + recipes.size + " Crucible Recipes")
    }

    /**
      * Adds the recipe
      *
      * @param input If you set null for the itemstack, it will attempt to create one from ore dict
      * @param fluidStack
      */
    def addCrucibleRecipe(input : ItemStack, ore : String, fluidStack: FluidStack) : Unit = {
        var stack : ItemStack = input
        if(input == null && !ore.isEmpty) {
            val stackList = OreDictionary.getOres(ore)
            if(!stackList.isEmpty) {
                stack = stackList.get(0)
            } else {
                LogHelper.severe("Could not add ore dict crucible recipe for " + ore + " as it does not exist in the OreDictionary")
                return
            }
        }
        val recipe = new CrucibleRecipe(getItemStackString(stack), ore, getFluidString(fluidStack))
        addRecipe(recipe)
    }
}


/**
  * Helper class for holding recipes
  *
  */
class CrucibleRecipe(val input : String, val ore : String, val output : String) extends
        AbstractRecipe[ItemStack, FluidStack] {

    /**
      * Used to get the output of this recipe
      *
      * @param itemIn The input object
      * @return The output object
      */
    override def getOutput(itemIn: ItemStack): Option[FluidStack] = {
        if(input == null) //Safety Check
            return None

        if(getItemStackFromString(input) != null &&
                (getItemStackFromString(input).isItemEqual(itemIn) &&
                        getItemStackFromString(input).getItemDamage == itemIn.getItemDamage) || (
                if(ore != null && OreDictionary.getOreIDs(itemIn) != null)
                    OreDictionary.getOreIDs(itemIn).toList.contains(OreDictionary.getOreID(ore))
                else
                    false))
            return Option(getFluidFromString(output))

        None
    }

    /**
      * Is the input valid for an output
      *
      * @param itemIn The input object
      * @return True if there is an output
      */
    override def isValidInput(itemIn: ItemStack): Boolean = {
        if(itemIn == null) //Safety Check
            return false

        val ourInput = getItemStackFromString(input)
        ourInput.isItemEqual(itemIn) && ourInput.getItemDamage == itemIn.getItemDamage && !itemIn.hasTagCompound
    }
}
