package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleToIntFunction;

import org.junit.Test;

import manager.LanguageUtility;
import manager.Utility;
import model.item.Item;
import model.loot.Loot;

public class UtilityTest {

    @Test
    public void randomTierTest() {
        Map<Integer, Integer> tierMap = new HashMap<Integer, Integer>();

        for (int i = 1; i < 6; i++) {
            tierMap.put((i), 0);
        }

        for (int i = 0; i < 10000; i++) {
            int randomTier = Utility.getRandomTier();
            tierMap.put(randomTier, tierMap.get(randomTier) + 1);
        }

        assertTrue("1", tierMap.get(1) > 4500 && tierMap.get(1) < 5500);
        assertTrue("2", tierMap.get(2) > 2000 && tierMap.get(2) < 3000);
        assertTrue("3", tierMap.get(3) > 1000 && tierMap.get(3) < 2000);
        assertTrue("4", tierMap.get(4) > 700 && tierMap.get(4) < 1100);
        assertTrue("5", tierMap.get(5) > 50 && tierMap.get(5) < 150);
    }

    @Test
    public void asRomanNumberTest() {
        assertEquals("V", Utility.asRomanNumber(5));
        assertEquals("DCCLXXXIX", Utility.asRomanNumber(789));
        assertEquals("", Utility.asRomanNumber(0));
        assertEquals("", Utility.asRomanNumber(-50));
    }

    @Test
    public void sellLootTest() {
        float modifier = Utility.getConfig().getFloat("loot.sell.modifier");
        List<Loot> lootList = new ArrayList<Loot>();

        assertEquals(0, Utility.sellLoot(lootList).coinValue);

        // add empty item
        lootList.add(new Loot(new Item()));

        assertEquals(0, Utility.sellLoot(lootList).coinValue);

        // add two more empty items
        lootList.add(new Loot(new Item()));
        lootList.add(new Loot(new Item()));

        assertEquals(0, Utility.sellLoot(lootList).coinValue);

        // add two items with 0.5 * 500 copper each. should be 500 * modifier
        lootList.get(0).getItem().setCurrency(new Currency(500));
        lootList.get(0).getItem().setAmount(0.5f);
        lootList.get(0).setAmount(2);

        assertEquals(500 *modifier, Utility.sellLoot(lootList).coinValue, 0);

        // add one item with -500 copper
        lootList.get(1).getItem().setCurrency(new Currency(-500));

        assertEquals(0, Utility.sellLoot(lootList).coinValue, 0);

        // add 1 currency items with 500 times 1 copper each 
        String currencyString = LanguageUtility.getMessage("currency");
        Item item = new Item();
        item.setSubTyp(currencyString);
        item.setAmount(500);
        item.setCurrency(new Currency(1));
        lootList.add(new Loot(item));

        assertEquals(500, Utility.sellLoot(lootList).coinValue, 0);

        // add 500 currency items with 0 times 1 copper each 
        item = new Item();
        item.setSubTyp(currencyString);
        item.setAmount(0);
        item.setCurrency(new Currency(1));
        lootList.add(new Loot(item, 500));

        assertEquals(500, Utility.sellLoot(lootList).coinValue, 0);
    }

}
