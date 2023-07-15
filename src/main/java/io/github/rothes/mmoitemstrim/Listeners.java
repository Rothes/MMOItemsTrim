package io.github.rothes.mmoitemstrim;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class Listeners implements Listener {

    @EventHandler
    public void onTrim(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv instanceof SmithingInventory) {
            SmithingInventory inventory = (SmithingInventory) inv;
            ItemStack result = inventory.getResult();
            if (result == null) {
                return;
            }
            ItemStack converted = convertMmoItem(result);
            if (converted == null) {
                return;
            }
            inventory.setResult(converted);
        }
    }

    private ItemStack convertMmoItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof ArmorMeta)) {
            return null;
        }
        ArmorMeta trim = (ArmorMeta) itemMeta;
        if (!trim.hasTrim())
            return null;
        TrimPattern pattern = trim.getTrim().getPattern();
        String id = MMOItemsTrim.configManager.idMap.get(pattern);
        if (id == null) {
            return null;
        }

        Type type = MMOItems.plugin.getTypes().get(MMOItemsTrim.configManager.typeMap.get(pattern));
        if (type == null) {
            if (Tag.ITEMS_SWORDS.isTagged(itemStack.getType())) {
                type = Type.SWORD;
            } else if (Tag.ITEMS_AXES.isTagged(itemStack.getType())) {
                type = Type.TOOL;
            } else if (Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(itemStack.getType())) {
                type = Type.ARMOR;
            } else if (itemStack.getType() == Material.BOW) {
                type = Type.BOW;
            } else if (itemStack.getType() == Material.CROSSBOW) {
                type = Type.CROSSBOW;
            } else {
                return null;
            }
        }

//        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, id);
//        NBTItem itemNbt = mmoItem.newBuilder().buildNBT();
//        NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(itemStack);
//        List<ItemTag> tags = new ArrayList<>();
//        for (String tag : itemNbt.getTags()) {
//            tags.add(new ItemTag(tag, itemNbt.get(tag)));
//        }
//
//        nbtItem = nbtItem.addTag(tags);
//        System.out.println("TAGS: " + nbtItem.getTags());
////        nbtItem = nbtItem.addTag(new ItemTag("MMOITEMS_ITEM_TYPE", type.getName()), new ItemTag("MMOITEMS_ITEM_ID", id));
//        return nbtItem.toItem();

        ItemStack mmoItem = MMOItems.plugin.getItem(type, id);
        NBTItem itemNbt = new NBTItem(mmoItem);
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.mergeCompound(itemNbt);
        return nbtItem.getItem();
    }

}
