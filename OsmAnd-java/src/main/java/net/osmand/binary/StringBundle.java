package net.osmand.binary;

import net.osmand.util.Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class StringBundle {

	private Map<String, Item> map = new LinkedHashMap<>();

	public enum ItemType {
		STRING,
		LIST,
		MAP
	}

	public StringBundle() {
	}

	protected StringBundle(Map<String, Item> map) {
		this.map = map;
	}

	public StringBundle newInstance() {
		return new StringBundle();
	}

	public static abstract class Item<T> {

		private String name;
		private ItemType type;
		private T value;

		private Item(String name, ItemType type, T value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public ItemType getType() {
			return type;
		}

		public T getValue() {
			return value;
		}
	}

	public static class StringItem extends Item<String> {

		private StringItem(String name, String value) {
			super(name, ItemType.STRING, value);
		}

		private StringItem(String name, int value) {
			super(name, ItemType.STRING, String.valueOf(value));
		}

		private StringItem(String name, long value) {
			super(name, ItemType.STRING, String.valueOf(value));
		}

		private StringItem(String name, float value) {
			super(name, ItemType.STRING, String.valueOf(value));
		}

		private StringItem(String name, boolean value) {
			super(name, ItemType.STRING, String.valueOf(value));
		}

		private int asInt(int defaultValue) {
			try {
				return Integer.parseInt(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private long asLong(long defaultValue) {
			try {
				return Long.parseLong(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private float asFloat(float defaultValue) {
			try {
				return Float.parseFloat(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private boolean asBoolean(boolean defaultValue) {
			try {
				return Boolean.parseBoolean(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private int[] asIntArray(int[] defaultValue) {
			try {
				return stringToIntArray(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

		private int[][] asIntIntArray(int[][] defaultValue) {
			try {
				return stringToIntIntArray(getValue());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	public static class StringListItem extends Item<List<Item>> {

		private StringListItem(String name, List<Item> list) {
			super(name, ItemType.LIST, list);
		}
	}

	public static class StringMapItem extends Item<Map<String, Item>> {

		private StringMapItem(String name, Map<String, Item> map) {
			super(name, ItemType.MAP, map);
		}
	}

	public static class StringBundleItem extends StringMapItem {

		private StringBundleItem(String name, StringBundle bundle) {
			super(name, bundle.map);
		}
	}

	public Map<String, Item> getMap() {
		return Collections.unmodifiableMap(map);
	}

	public Item getItem(String key) {
		return map.get(key);
	}

	public void putInt(String key, int value) {
		map.put(key, new StringItem(key, value));
	}

	public int getInt(String key, int defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asInt(defaultValue) : defaultValue;
	}

	public void putLong(String key, long value) {
		map.put(key, new StringItem(key, value));
	}

	public long getLong(String key, long defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asLong(defaultValue) : defaultValue;
	}

	public void putFloat(String key, float value) {
		map.put(key, new StringItem(key, value));
	}

	public float getFloat(String key, float defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asFloat(defaultValue) : defaultValue;
	}

	public void putBoolean(String key, boolean value) {
		map.put(key, new StringItem(key, value));
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asBoolean(defaultValue) : defaultValue;
	}

	public void putString(String key, String value) {
		if (value != null) {
			map.put(key, new StringItem(key, value));
		}
	}

	public String getString(String key, String defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).getValue() : defaultValue;
	}

	public void putObject(String key, StringExternalizable object) {
		if (object != null) {
			StringBundle bundle = newInstance();
			object.writeToBundle(bundle);
			map.put(key, new StringBundleItem(key, bundle));
		}
	}

	public void putList(String key, String itemName, List<? extends StringExternalizable> list) {
		if (list != null) {
			List<Item> itemList = new ArrayList<>();
			for (StringExternalizable ex : list) {
				if (ex != null) {
					StringBundle bundle = newInstance();
					ex.writeToBundle(bundle);
					itemList.add(new StringBundleItem(itemName, bundle));
				}
			}
			map.put(key, new StringListItem(key, itemList));
		}
	}

	public void putBundleList(String key, String itemName, List<StringBundle> list) {
		if (list != null) {
			List<Item> itemList = new ArrayList<>();
			for (StringBundle bundle : list) {
				itemList.add(new StringBundleItem(itemName, bundle));
			}
			map.put(key, new StringListItem(key, itemList));
		}
	}

	public void putBundle(String key, StringBundle bundle) {
		map.put(key, new StringBundleItem(key, bundle));
	}

	public void putArray(String key, int[] array) {
		if (array != null) {
			map.put(key, new StringItem(key, intArrayToString(array)));
		}
	}

	public int[] getIntArray(String key, int[] defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asIntArray(defaultValue) : defaultValue;
	}

	public void putArray(String key, int[][] array) {
		if (array != null) {
			map.put(key, new StringItem(key, intIntArrayToString(array)));
		}
	}

	public int[][] getIntIntArray(String key, int[][] defaultValue) {
		Item item = map.get(key);
		return item instanceof StringItem ? ((StringItem) item).asIntIntArray(defaultValue) : defaultValue;
	}

	public void putArray(String key, long[] array) {
		if (array != null) {
			map.put(key, new StringItem(key, longArrayToString(array)));
		}
	}

	public void putArray(String key, float[] array) {
		if (array != null) {
			map.put(key, new StringItem(key, floatArrayToString(array)));
		}
	}

	public void putArray(String key, String[] array) {
		if (array != null) {
			map.put(key, new StringItem(key, strArrayToString(array)));
		}
	}

	public void putArray(String key, String[][] array) {
		if (array != null) {
			map.put(key, new StringItem(key, strStrArrayToString(array)));
		}
	}

	public <T> void putMap(String key, TIntObjectHashMap<T> map) {
		if (map != null) {
			StringBundle bundle = newInstance();
			TIntObjectIterator<T> it = map.iterator();
			while (it.hasNext()) {
				it.advance();
				int k = it.key();
				T v = it.value();
				bundle.putString(String.valueOf(k), String.valueOf(v));
			}
			this.map.put(key, new StringBundleItem(key, bundle));
		}
	}

	public <K, V> void putMap(String key, Map<K, V> map) {
		if (map != null) {
			StringBundle bundle = newInstance();
			for (Map.Entry<K, V> entry : map.entrySet()) {
				bundle.putString(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
			}
			this.map.put(key, new StringBundleItem(key, bundle));
		}
	}

	private static String intArrayToString(int[] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int value : a) {
			if (b.length() > 0) {
				b.append(",");
			}
			b.append(value);
		}
		return b.toString();
	}

	private static int[] stringToIntArray(String a) throws NumberFormatException {
		if (a == null) {
			return null;
		}
		String[] items = a.split(",");
		int[] res = new int[items.length];
		for (int i = 0; i < items.length; i++) {
			res[i] = Integer.parseInt(items[i]);
		}
		return res;
	}

	private static String longArrayToString(long[] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (long value : a) {
			if (b.length() > 0) {
				b.append(",");
			}
			b.append(value);
		}
		return b.toString();
	}

	private static long[] stringToLongArray(String a) throws NumberFormatException {
		if (a == null) {
			return null;
		}
		String[] items = a.split(",");
		long[] res = new long[items.length];
		for (int i = 0; i < items.length; i++) {
			res[i] = Integer.parseInt(items[i]);
		}
		return res;
	}

	private static String floatArrayToString(float[] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (float value : a) {
			if (b.length() > 0) {
				b.append(",");
			}
			b.append(value);
		}
		return b.toString();
	}

	private static float[] stringToFloatArray(String a) throws NumberFormatException {
		if (a == null) {
			return null;
		}
		String[] items = a.split(",");
		float[] res = new float[items.length];
		for (int i = 0; i < items.length; i++) {
			res[i] = Float.parseFloat(items[i]);
		}
		return res;
	}

	private static String intIntArrayToString(int[][] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0) {
				b.append(";");
			}
			int[] arr = a[i];
			if (arr != null && arr.length > 0) {
				b.append(intArrayToString(arr));
			}
		}
		/*
		for (int i = 0; i < a.length; i++) {
			int[] value = a[i];
			if (value != null && value.length > 0) {
				if (b.length() > 0) {
					b.append(";");
				}
				b.append(i).append(":").append(intArrayToString(value));
			}
		}
		*/
		return b.toString();
	}

	private static int[][] stringToIntIntArray(String a) throws NumberFormatException {
		if (a == null) {
			return null;
		}
		String[] items = a.split(";");
		int[][] res = new int[items.length][];
		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			if (Algorithms.isEmpty(item)) {
				res[i] = null;
			} else {
				String[] subItems = item.split(",");
				res[i] = new int[subItems.length];
				for (int k = 0; k < subItems.length; k++) {
					res[i][k] = Integer.parseInt(subItems[k]);
				}
			}
		}
		/*
		String[] items = a.split(";");
		int[][] res = new int[items.length][];
		for (int i = 0; i < items.length; i++) {
			String[] subItems = a.split(",");
			res[i] = new int[subItems.length];
			for (int k = 0; k < subItems.length; k++) {
				res[i][k] = Integer.parseInt(subItems[k]);
			}
		}
		*/
		return res;
	}

	private static String strArrayToString(String[] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (String value : a) {
			if (b.length() > 0) {
				b.append(0x1E);
			}
			b.append(value);
		}
		return b.toString();
	}

	private static String strStrArrayToString(String[][] a) {
		if (a == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			String[] value = a[i];
			if (value != null && value.length > 0) {
				if (b.length() > 0) {
					b.append(0x1F);
				}
				b.append(String.valueOf(i)).append(":").append(strArrayToString(value));
			}
		}
		return b.toString();
	}
}
