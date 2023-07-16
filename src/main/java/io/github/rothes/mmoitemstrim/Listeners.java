package io.github.rothes.mmoitemstrim;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
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

import java.util.UUID;

public class Listeners implements Listener {

    @EventHandler
    public void onTrim(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv instanceof SmithingInventory) {
            SmithingInventory inventory = (SmithingInventory) inv;
            ItemStack result = inventory.getResult();
            if (result == null || result.getType() == Material.AIR) {
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

//        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, id);
//        NBTItem itemNbt = new NBTItem(mmoItem);
//        NBTItem nbtItem = new NBTItem(itemStack);
//        nbtItem.mergeCompound(itemNbt);


        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, id);
        if (mmoItem == null) {
            return itemStack;
        }

        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasTag("MMOITEMSTRIM_APPLIED")) {
            return itemStack;
        }

        String thisType = nbtItem.getString("MMOITEMS_ITEM_TYPE");
        String thisId = nbtItem.getString("MMOITEMS_ITEM_ID");
        if (MMOItems.plugin.getTypes().has(thisType) && thisId != null) {
            LiveMMOItem liveMMOItem = new LiveMMOItem(itemStack);
            GemSocketsData data = (GemSocketsData) liveMMOItem.getData(ItemStats.GEM_SOCKETS);
            String emptySocket = data == null ? null : data.getEmptySocket(liveMMOItem.getNBT().getString(ItemStats.GEM_COLOR.getNBTPath()));
            GemstoneData gemstoneData = emptySocket == null ? null : new GemstoneData(liveMMOItem, emptySocket);
            UUID uuid = gemstoneData == null ? null : gemstoneData.getHistoricUUID();

            for (ItemStat<?, ?> stat : liveMMOItem.getStats()) {
                mmoItem.mergeData(stat, liveMMOItem.getData(stat), uuid);
            }

            itemStack = mmoItem.newBuilder().build();
            nbtItem = new NBTItem(itemStack);
        } else {
            NBTItem itemNbt = new NBTItem(mmoItem.newBuilder().build());
            nbtItem.mergeCompound(itemNbt);
        }
        nbtItem.setBoolean("MMOITEMSTRIM_APPLIED", true);
        nbtItem.applyNBT(itemStack);
        return itemStack;
    }

//    private void merge(ItemStack itemStack, MMOItem mmoitem) {
//        NBTItem miNbt = new NBTItem(mmoitem.newBuilder().build());
//        NBTItem nbtItem = new NBTItem(itemStack);
//        merge(nbtItem, miNbt);
//        nbtItem.applyNBT(itemStack);
//    }

//    private void merge(NBTCompound target, NBTCompound merge) {
//        for (String key : merge.getKeys()) {
//            NBTType keyType = merge.getType(key);
//            switch (keyType) {
//                case NBTTagEnd -> {}
//                case NBTTagByte -> {
//                    Byte get = merge.getByte(key);
//                    Byte t = target.getByte(key);
//                    if (t != null) {
//                        target.setByte(key, (byte) (t + get));
//                    } else {
//                        target.setByte(key, get);
//                    }
//                }
//                case NBTTagShort -> {
//                    Short get = merge.getShort(key);
//                    Short t = target.getShort(key);
//                    if (t != null) {
//                        target.setShort(key, (short) (t + get));
//                    } else {
//                        target.setShort(key, get);
//                    }
//                }
//                case NBTTagInt -> {
//                    Integer get = merge.getInteger(key);
//                    Integer t = target.getInteger(key);
//                    if (t != null) {
//                        target.setInteger(key, t + get);
//                    } else {
//                        target.setInteger(key, get);
//                    }
//                }
//                case NBTTagLong -> {
//                    Long get = merge.getLong(key);
//                    Long t = target.getLong(key);
//                    if (t != null) {
//                        target.setLong(key, t + get);
//                    } else {
//                        target.setLong(key, get);
//                    }
//                }
//                case NBTTagFloat -> {
//                    Float get = merge.getFloat(key);
//                    Float t = target.getFloat(key);
//                    if (t != null) {
//                        target.setFloat(key, t + get);
//                    } else {
//                        target.setFloat(key, get);
//                    }
//                }
//                case NBTTagDouble -> {
//                    Double get = merge.getDouble(key);
//                    Double t = target.getDouble(key);
//                    if (t != null) {
//                        target.setDouble(key, t + get);
//                    } else {
//                        target.setDouble(key, get);
//                    }
//                }
//                case NBTTagList -> target.getCompoundList(key).addAll(merge.getCompoundList(key));
//                case NBTTagString -> target.setString(key, merge.getString(key));
//                case NBTTagIntArray -> target.setIntArray(key, merge.getIntArray(key));
//                case NBTTagByteArray -> target.setByteArray(key, merge.getByteArray(key));
//                case NBTTagCompound -> {
//                    if (target.hasTag(key)) {
//                        merge(target.getCompound(key), merge.getCompound(key));
//                    } else {
//                        target.addCompound(key).mergeCompound(merge.getCompound(key));
//                    }
//                }
//                default -> throw new AssertionError();
//            }
//        }
//    }

}
