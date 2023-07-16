package io.github.rothes.mmoitemstrim;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
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
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;

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
        ArmorMeta trimMeta = (ArmorMeta) itemMeta;
        if (!trimMeta.hasTrim())
            return null;
        ArmorTrim trim = trimMeta.getTrim();

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

        NBTItem nbtItem = new NBTItem(itemStack);
        String thisType = nbtItem.getString("MMOITEMS_ITEM_TYPE");
        String thisId = nbtItem.getString("MMOITEMS_ITEM_ID");
        LiveMMOItem liveMMOItem = MMOItems.plugin.getTypes().has(thisType) && !thisId.isEmpty() ? new LiveMMOItem(itemStack) : null;
        if (nbtItem.hasTag("MMOITEMSTRIM_APPLIED")) {
            String ognType = nbtItem.getString("MMOITEMSTRIM_ORIGINAL_ITEM_TYPE");
            String ognId = nbtItem.getString("MMOITEMSTRIM_ORIGINAL_ITEM_ID");

            if (ognType.isEmpty() && ognId.isEmpty()) {
                // Vanilla item
                for (ItemStat stat : new ArrayList<>(liveMMOItem.getStats())) {
                    switch (stat.getId()) {
                        case "TRIM_MATERIAL":
                        case "TRIM_PATTERN":
                        case "MATERIAL":
                            break;
                        default:
                            liveMMOItem.removeData(stat);
                    }
                }
                for (StatHistory history : liveMMOItem.getStatHistories()) {
                    history.clearExternalData();
                }

                itemStack = liveMMOItem.newBuilder().buildSilently();
                nbtItem = new NBTItem(itemStack);
                nbtItem.removeKey("MMOITEMS_ITEM_TYPE");
                nbtItem.removeKey("MMOITEMS_ITEM_ID");
            } else {
                if (liveMMOItem == null) {
                    // Failed to get current item, stop processing
                    return itemStack;
                }
                MMOItem ognItem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(ognType), ognId);
                if (ognItem != null) {
                    for (ItemStat stat : new ArrayList<>(liveMMOItem.getStats())) {
                        switch (stat.getId()) {
                            case "TRIM_MATERIAL":
                            case "TRIM_PATTERN":
                            case "MATERIAL":
                                break;
                            default:
                                StatData statData = ognItem.getData(stat);
                                if (statData == null) {
                                    liveMMOItem.removeData(stat);
                                } else {
                                    liveMMOItem.setData(stat, statData);
                                }
                        }
                    }
                } else {
                    // Not possible but just removing all
                    for (ItemStat stat : new ArrayList<>(liveMMOItem.getStats())) {
                        switch (stat.getId()) {
                            case "TRIM_MATERIAL":
                            case "TRIM_PATTERN":
                            case "MATERIAL":
                                break;
                            default:
                                liveMMOItem.removeData(stat);
                        }
                    }
                    for (StatHistory history : liveMMOItem.getStatHistories()) {
                        history.clearExternalData();
                    }
                }
                itemStack = liveMMOItem.newBuilder().buildSilently();
                ArmorMeta fixTrim = (ArmorMeta) itemStack.getItemMeta();
                fixTrim.setTrim(trim);
                itemStack.setItemMeta(fixTrim);
                nbtItem = new NBTItem(itemStack);
                nbtItem.setString("MMOITEMS_ITEM_TYPE", ognType);
                nbtItem.setString("MMOITEMS_ITEM_ID", ognId);
            }
            nbtItem.removeKey("MMOITEMSTRIM_APPLIED");
            nbtItem.removeKey("MMOITEMSTRIM_ORIGINAL_ITEM_TYPE");
            nbtItem.removeKey("MMOITEMSTRIM_ORIGINAL_ITEM_ID");
            nbtItem.applyNBT(itemStack);

            thisType = nbtItem.getString("MMOITEMS_ITEM_TYPE");
            thisId = nbtItem.getString("MMOITEMS_ITEM_ID");
            liveMMOItem = MMOItems.plugin.getTypes().has(thisType) && !thisId.isEmpty() ? new LiveMMOItem(itemStack) : null;
            if (liveMMOItem != null) {
//                for (ItemStat stat : new ArrayList<>(liveMMOItem.getStats())) {
//                    switch (stat.getId()) {
//                        case "TRIM_MATERIAL":
//                        case "TRIM_PATTERN":
//                        case "MATERIAL":
//                            break;
//                        default:
//                            if (liveMMOItem.getData(stat) instanceof Mergeable) {
//                                StatHistory from = StatHistory.from(liveMMOItem, stat);
//                                from.clearExternalData();
//                                from.recalculate(liveMMOItem.getUpgradeLevel());
//                            }
//                    }
//                }
                for (StatHistory history : liveMMOItem.getStatHistories()) {
                    history.clearExternalData();
                    history.recalculate(liveMMOItem.getUpgradeLevel());
                    if (!history.getOriginalData().isEmpty()) {
                    }
                }

                itemStack = liveMMOItem.newBuilder().buildSilently();
            }
        }


        TrimPattern pattern = trim.getPattern();
        String id = MMOItemsTrim.configManager.idMap.get(pattern);
        if (id == null) {
            return itemStack;
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
                return itemStack;
            }
        }

        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, id);
        if (mmoItem == null) {
            return itemStack;
        }

        if (liveMMOItem != null) {
//            NBTCompound ogn = nbtItem.addCompound("MMOITEMSTRIM_ORIGINAL");
//            for (String key : nbtItem.getKeys()) {
//                if (!key.startsWith("MMOITEMS_")) {
//                    continue;
//                }
//
//                ogn.set
//            }

//            GemSocketsData data = (GemSocketsData) liveMMOItem.getData(ItemStats.GEM_SOCKETS);
//            String emptySocket = data == null ? null : data.getEmptySocket(liveMMOItem.getNBT().getString(ItemStats.GEM_COLOR.getNBTPath()));
//            GemstoneData gemstoneData = emptySocket == null ? null : new GemstoneData(liveMMOItem, emptySocket);
//            UUID uuid = gemstoneData == null ? null : gemstoneData.getHistoricUUID();

            for (ItemStat<?, ?> stat : liveMMOItem.getStats()) {
                mmoItem.mergeData(stat, liveMMOItem.getData(stat), null);
            }

            itemStack = mmoItem.newBuilder().buildSilently();
            nbtItem = new NBTItem(itemStack);
        } else {
            NBTItem itemNbt = new NBTItem(mmoItem.newBuilder().buildSilently());
            nbtItem.mergeCompound(itemNbt);
        }
        nbtItem.setBoolean("MMOITEMSTRIM_APPLIED", true);
        nbtItem.setString("MMOITEMSTRIM_ORIGINAL_ITEM_TYPE", thisType);
        nbtItem.setString("MMOITEMSTRIM_ORIGINAL_ITEM_ID", thisId);
        nbtItem.applyNBT(itemStack);
        return itemStack;
    }

//    private void removeMmoItemsNbt(NBTCompound nbt) {
//        for (String key : nbt.getKeys()) {
//            if (key.startsWith("MMOITEMS_")) {
//                nbt.removeKey(key);
//            }
//            NBTCompound compound = nbt.getCompound(key);
//            if (compound != null) {
//                removeMmoItemsNbt(compound);
//            }
//        }
//
//    }

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
