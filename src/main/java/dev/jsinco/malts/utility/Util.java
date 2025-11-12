package dev.jsinco.malts.utility;

import com.google.gson.Gson;
import dev.jsinco.malts.Malts;
import dev.jsinco.malts.utility.interfaces.EditMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class Util {

    public static final Gson GSON = new Gson();


    public static int getInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static <E extends Enum<E>> E getEnum(String value, Class<E> enumClass) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static NamespacedKey namespacedKey(String key) {
        return new NamespacedKey(Malts.getInstance(), key);
    }

    public static ItemStack editMeta(ItemStack itemStack, EditMeta editMeta) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        editMeta.edit(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static <P, C> void setPersistentKey(ItemStack item, String key, PersistentDataType<P, C> type, C value) {
        editMeta(item, meta -> {
            meta.getPersistentDataContainer().set(namespacedKey(key), type, value);
        });
    }

    public static <P, C> ItemStack setPersistentKey(ItemStack item, NamespacedKey key, PersistentDataType<P, C> type, C value) {
        editMeta(item, meta -> {
            meta.getPersistentDataContainer().set(key, type, value);
        });
        return item;
    }

    public static boolean hasPersistentKey(ItemStack item, NamespacedKey key) {
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key);
    }

    public static <P, C> C getPersistentKey(ItemStack item, NamespacedKey key, PersistentDataType<P, C> type) {
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key)) {
            return item.getItemMeta().getPersistentDataContainer().get(key, type);
        }
        return null;
    }


    public static String formatEnumerator(Enum<?> e) {
        return formatEnumerator(e.name());
    }
    public static String formatEnumerator(String s) {
        String name = s.toLowerCase().replace("_", " ");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == ' ' && i + 1 < name.length()) {
                name = name.substring(0, i + 1)
                        + Character.toUpperCase(name.charAt(i + 1))
                        + name.substring(i + 2);
            }
        }
        return name;
    }

    public static int getMaterialAmount(Inventory inv, Material material) {
        int amount = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null) {
                continue;
            }
            if (item.getType() == material && !item.hasItemMeta()) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public static int getAmountInvCanHold(Inventory inv, Material material) {
        int amount = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null) {
                amount += material.getMaxStackSize();
            } else if (item.getType() == material && !item.hasItemMeta()) {
                amount += item.getMaxStackSize() - item.getAmount();
            }
        }
        return amount;
    }

    public static List<String> tryGetNextNumberArg(String arg) {
        int num = Util.getInteger(arg, -1);
        return IntStream.range(0, 10)
                .mapToObj(i -> num < 0 ? String.valueOf(i) : num + "" + i)
                .toList();
    }

    @SafeVarargs
    public static String replace(String string, Couple<String, Object>... pairs) {
        if (string == null) {
            return null;
        }
        String newString = string;
        for (Couple<String, Object> pair : pairs) {
            if (pair.b() instanceof Integer integer) {
                newString = newString.replace(pair.a(), String.format("%,d", integer));
            } else if(pair.b() instanceof Double doubleValue) {
                newString = newString.replace(pair.a(), String.format("%,.2f", doubleValue));
            } else {
                newString = newString.replace(pair.a(), String.valueOf(pair.b()));
            }
        }
        return newString;
    }

    @SafeVarargs
    public static Component replaceComponents(Component passed, Couple<String, Object>... pairs) {
        if (passed == null) {
            return null;
        }

        // I hate this but whatever

        Component newComponent = passed;
        for (Couple<String, Object> pair : pairs) {
            if (pair.b() instanceof Component component) {
                newComponent = newComponent.replaceText(builder -> builder
                        .match(Pattern.quote(pair.a()))
                        .replacement(component));
            } else if (pair.b() instanceof Collection<?> collection) {
                List<Component> components = collection.stream()
                        .map(obj -> {
                            if (obj instanceof Component comp) {
                                return comp;
                            } else {
                                return Component.text(String.valueOf(obj));
                            }
                        })
                        .toList();

                Component joined = Component.join(JoinConfiguration.newlines(), components);

                newComponent = newComponent.replaceText(builder -> builder
                        .match(Pattern.quote(pair.a()))
                        .replacement(joined));
            } else {
                newComponent = newComponent.replaceText(builder -> builder
                        .match(Pattern.quote(pair.a()))
                        .replacement(String.valueOf(pair.b())));
            }


        }
        return newComponent;
    }

    public static List<String> replaceAll(List<String> list, String charArray, String charArrayReplacement) {
        return list.stream().map(s -> s.replace(charArray, charArrayReplacement)).toList();
    }

    public static List<String> replaceStringWithList(List<String> list, String charArray, List<String> charArrayReplacement) {
        List<String> newList = new ArrayList<>(list);
        for (int i = 0; i < newList.size(); i++) {
            String s = newList.get(i);
            if (s.contains(charArray)) {
                newList.remove(i);
                newList.addAll(i, charArrayReplacement);
            }
        }
        return newList;
    }

    public static List<String> replaceAll(List<String> list, Couple<String, Object>... pairs) {
        List<String> newList = new ArrayList<>();
        for (String string : list) {
            String newString = replace(string, pairs);
            newList.add(newString);
        }
        return newList;
    }

    public static <T> List<T> plusList(List<T> list, T... items) {
        List<T> newList = new ArrayList<>(list);
        newList.addAll(List.of(items));
        return newList;
    }
    public static <T> List<T> plusFirstIndex(List<T> list, T... items) {
        List<T> newList = new ArrayList<>(items.length + list.size());
        newList.addAll(List.of(items));
        newList.addAll(list);
        return newList;
    }
}
