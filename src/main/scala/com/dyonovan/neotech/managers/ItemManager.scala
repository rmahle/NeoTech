package com.dyonovan.neotech.managers

import com.dyonovan.neotech.common.items.BaseItem
import net.minecraft.init.Items
import net.minecraft.item.{ItemStack, Item}
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

/**
 * This file was created for NeoTech
 *
 * NeoTech is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * @author Dyonovan
 * @since August 12, 2015
 */
object ItemManager {

    //Dusts
    val dustGold = new BaseItem("dustGold", 64)
    val dustIron = new BaseItem("dustIron", 64)
    val dustCopper = new BaseItem("dustCopper", 64)
    val dustTin = new BaseItem("dustTin", 64)

    //Ingots
    val ingotCopper = new BaseItem("ingotCopper", 64)
    val ingotTin = new BaseItem("ingotTin", 64)

    def preInit(): Unit = {
        registerItem(dustGold, "dustGold", "dustGold")
        registerItem(dustIron, "dustIron", "dustIron")
        registerItem(dustCopper, "dustCopper", "dustCopper")
        registerItem(dustTin, "dustTin", "dustTin")

        registerItem(ingotCopper, "ingotCopper", "ingotCopper")
        registerItem(ingotTin, "ingotTin", "ingotTin")

        //Dust Smelting Recipes
        GameRegistry.addSmelting(dustGold, new ItemStack(Items.gold_ingot), 2.0F)
        GameRegistry.addSmelting(dustIron, new ItemStack(Items.iron_ingot), 1.0F)
        GameRegistry.addSmelting(dustCopper, new ItemStack(this.ingotCopper), 1.0F)
        GameRegistry.addSmelting(dustTin, new ItemStack(this.ingotTin), 2.0F)
    }
    /**
     * Helper method to register items
     * @param item The item to register
     * @param name The name of the item
     * @param oreDict The ore dict tag
     */
    private def registerItem(item: Item, name: String, oreDict: String) {
        GameRegistry.registerItem(item, name)
        if (oreDict != null) OreDictionary.registerOre(oreDict, item)
    }

    private def registerItem(item: Item, name: String) {
        registerItem(item, name, null)
    }
}
