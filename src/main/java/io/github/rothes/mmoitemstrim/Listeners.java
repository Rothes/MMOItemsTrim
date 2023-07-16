package io.github.rothes.mmoitemstrim;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
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
import java.util.Optional;

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

        NBTItem nbtItem = new NBTItem(itemStack);
        String thisType = nbtItem.getString("MMOITEMS_ITEM_TYPE");
        String thisId = nbtItem.getString("MMOITEMS_ITEM_ID");
        LiveMMOItem liveMMOItem = MMOItems.plugin.getTypes().has(thisType) && !thisId.isEmpty() ? new LiveMMOItem(itemStack) : null;
        if (nbtItem.hasTag("MMOITEMSTRIM_APPLIED")) {
            if (nbtItem.hasTag("MMOITEMSTRIM_IS_VANILLA_ITEM")) {
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
                    history.recalculate(liveMMOItem.getUpgradeLevel());
                }

                itemStack = liveMMOItem.newBuilder().buildSilently();
                ArmorMeta fixTrim = (ArmorMeta) itemStack.getItemMeta();
                fixTrim.setTrim(trim);
                itemStack.setItemMeta(fixTrim);
                nbtItem = new NBTItem(itemStack);
                nbtItem.removeKey("MMOITEMSTRIM_IS_VANILLA_ITEM");
                nbtItem.removeKey("MMOITEMS_ITEM_TYPE");
                nbtItem.removeKey("MMOITEMS_ITEM_ID");
            } else {
                if (liveMMOItem == null) {
                    // Failed to get current item, stop processing
                    return itemStack;
                }
                MMOItem ognItem = MMOItems.plugin.getMMOItem(liveMMOItem.getType(), liveMMOItem.getId());
                if (ognItem != null) {
                    ArrayList<StatHistory> statHistories = liveMMOItem.getStatHistories();
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
                                    if (statData instanceof Mergeable) {
                                        Optional<StatHistory> find = statHistories.stream().filter(it -> it.getItemStat() == stat).findAny();
                                        if (find.isPresent()) {
                                            find.get().clearExternalData();
                                            find.get().recalculate(liveMMOItem.getUpgradeLevel());
                                        }
                                    }
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
                        history.recalculate(liveMMOItem.getUpgradeLevel());
                    }
                }
                itemStack = liveMMOItem.newBuilder().buildSilently();
                ArmorMeta fixTrim = (ArmorMeta) itemStack.getItemMeta();
                fixTrim.setTrim(trim);
                itemStack.setItemMeta(fixTrim);
                nbtItem = new NBTItem(itemStack);
            }
            nbtItem.removeKey("MMOITEMSTRIM_APPLIED");
            nbtItem.applyNBT(itemStack);

            thisType = nbtItem.getString("MMOITEMS_ITEM_TYPE");
            thisId = nbtItem.getString("MMOITEMS_ITEM_ID");
            liveMMOItem = MMOItems.plugin.getTypes().has(thisType) && !thisId.isEmpty() ? new LiveMMOItem(itemStack) : null;
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
            for (ItemStat<?, ?> stat : mmoItem.getStats()) {
                StatData statData = mmoItem.getData(stat);
                if (statData instanceof Mergeable) {
                    liveMMOItem.mergeData(stat, statData, null);
                } else if (!liveMMOItem.hasData(stat)) {
                    liveMMOItem.setData(stat, statData);
                }
            }

            itemStack = liveMMOItem.newBuilder().buildSilently();
            nbtItem = new NBTItem(itemStack);
        } else {
            NBTItem itemNbt = new NBTItem(mmoItem.newBuilder().buildSilently());
            nbtItem.setBoolean("MMOITEMSTRIM_IS_VANILLA_ITEM", true);
            nbtItem.mergeCompound(itemNbt);
        }
        nbtItem.setBoolean("MMOITEMSTRIM_APPLIED", true);
        nbtItem.applyNBT(itemStack);

        ArmorMeta fixTrim = (ArmorMeta) itemStack.getItemMeta();
        fixTrim.setTrim(trim);
        itemStack.setItemMeta(fixTrim);
        return itemStack;
    }

}
