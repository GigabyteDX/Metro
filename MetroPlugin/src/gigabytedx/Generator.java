package gigabytedx;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class Generator {

	Main plugin;
	Mod mod;
	int prevX, prevZ;

	public static List<String> moduleChuncks = new ArrayList<String>();

	public Generator(Main plugin, Mod mod, int prevX, int prevZ) {

		this.plugin = plugin;
		this.prevX = prevX;
		this.prevZ = prevZ;
		this.plugin = plugin;
		this.mod = mod;
		placeModule(mod);
		Generate();
	}

	private void Generate() {

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {

				// loop through sides of module
				for (Object x : Main.moduleConfigurations.get(mod.getName()).getList("Sides")) {

					// ignore side if it is the previously placed module

					if (mod.getX() + getDirX((String) x) == prevX && mod.getZ() + getDirZ((String) x) == prevZ) {
					} else {

						try {
							String newModName = getRandModule((String) x, checkAroundModule(x));
							// Generator.moduleChuncks.add(mod.getX() / 16 +
							// getDirX((String) x) / 16 + "." + mod.getZ() / 16
							// + getDirZ((String) x) / 16);
							// setCheckedBlocks(mod.getX()+ getDirX((String) x),
							// mod.getZ() + getDirZ((String) x));
							newGenerator(new Mod(mod.getX() + getDirX((String) x), mod.getZ() + getDirZ((String) x), newModName), mod.getX(), mod.getZ());
						} catch (NullPointerException e) {
							placeModule(new Mod(mod.getX() + getDirX((String) x), mod.getZ() + getDirZ((String) x), "TunnelEnd"));
						}

					}
				}

			}

			private List<String> checkAroundModule(Object x) {

				List<String> moduleList = getCompatList(x);
				List<String> compatablelist = new ArrayList<String>();
				int checks = 1;
				for (String module : moduleList) {

					for (Object side : Main.moduleConfigurations.get(module).getList("Sides")) {

						for (Object s : Main.moduleConfigurations.get(module).getList((String) side)) {

							if (containsModule(mod.getX() + getDirX((String) x) + getXCoordString(s), mod.getZ() + getDirZ((String) x) + getZCoordString(s)) && notPrev(x, s)) {
								break;
							} else if (!containsModule(mod.getX() + getDirX((String) x) + getXCoordString(s), mod.getZ() + getDirZ((String) x) + getZCoordString(s)) && notPrev(x, s)) {
								checks++;
							}

							if (checks == Main.moduleConfigurations.get(module).getList("Sides").size()) {
								checks = 1;
								compatablelist.add(module);

							}
						}

					}

				}
				return compatablelist;

			}

			private boolean notPrev(Object x, Object s) {

				if (mod.getX() == mod.getX() + getDirX((String) x) + getXCoordString(s) && mod.getZ() == mod.getZ() + getDirZ((String) x) + getZCoordString(s)) {
					return false;
				}
				return true;
			}

			private List<String> getCompatList(Object x) {

				switch ((String) x) {
				case "North":
					return Main.northCompat;
				case "East":
					return Main.eastCompat;
				case "South":
					return Main.southCompat;
				case "West":
					return Main.westCompat;
				}
				return null;
			}

		}, 100);
	}

	@SuppressWarnings("deprecation")
	private void setBlocks(String[] blockArray, Byte[] blockDataArray, int xOffset, int zOffset) {

		int blockCount = 0;

		for (int x = xOffset; x < xOffset + 16; x++) {
			for (int z = zOffset; z < zOffset + 16; z++) {

				Bukkit.getWorld("world").getBlockAt(x, 10, z).setType(Material.getMaterial(blockArray[blockCount]));

				Bukkit.getWorld("world").getBlockAt(x, 10, z).setData(blockDataArray[blockCount]);

				blockCount++;

			}
		}

	}

	private void setCheckedBlocks(int xOffset, int zOffset) {

		for (int x = xOffset; x < xOffset + 16; x++) {
			for (int z = zOffset; z < zOffset + 16; z++) {

				Bukkit.getWorld("world").getBlockAt(x, 5, z).setType(Material.BEDROCK);

			}
		}

	}

	private void setDoneBlocks(int xOffset, int zOffset) {

		for (int x = xOffset; x < xOffset + 16; x++) {
			for (int z = zOffset; z < zOffset + 16; z++) {

				Bukkit.getWorld("world").getBlockAt(x, 5, z).setType(Material.REDSTONE_BLOCK);

			}
		}

	}

	private void newGenerator(Mod mod, int x, int z) {

		Main.generators.add(new Generator(plugin, mod, x, z));
	}

	@SuppressWarnings("unused")
	private void destroyGenerator() {

		Main.generators.remove(this);
	}

	private void placeModule(Mod mod) {

		try {
			setBlocks((String[]) ReadAndWrite.read("ModuleData/" + mod.getName() + "_BlockTypes", plugin.getDataFolder()), (Byte[]) ReadAndWrite.read("ModuleData/" + mod.getName() + "_BlockData", plugin.getDataFolder()), mod.getX(), mod.getZ());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		moduleChuncks.add(mod.getX() / 16 + "." + mod.getZ() / 16);
		setDoneBlocks(mod.getX(), mod.getZ());
	}

	private String getRandModule(String side, List<String> list) {

		String temp;

		if (list.size() != 0) {
			int idx = new Random().nextInt(list.size());
			temp = (String) list.get(idx);
		} else {
			temp = null;
		}
		for (Object sids : Main.moduleConfigurations.get(temp).getList("Sides")) {

			for (Object string : Main.moduleConfigurations.get(temp).getList((String) sids)) {

				if (mod.getX() == getXCoordString(string) + mod.getX() + getDirX(side) && mod.getZ() == mod.getZ() + getDirZ(side) + getZCoordString(string)) {
				} else {

					moduleChuncks.add((getDirX(side) / 16 + mod.getX() / 16 + getXCoordString(string) / 16) + "." + (getDirZ(side) / 16 + mod.getZ() / 16 + getZCoordString(string) / 16));

					setCheckedBlocks(getDirX(side) + mod.getX() + getXCoordString(string), getDirZ(side) + mod.getZ() + getZCoordString(string));

					Bukkit.broadcastMessage("   side" + side);
					Bukkit.broadcastMessage("   temp " + temp);
					Bukkit.broadcastMessage("   string" + string);
					Bukkit.broadcastMessage("---------------------------------");
					Bukkit.broadcastMessage("---------------------------------");
					Bukkit.broadcastMessage("---------------------------------");

				}
			}
		}
		return temp;

	}

	private boolean containsModule(int x, int y) {

		if (moduleChuncks.contains(Integer.toString(x / 16) + "." + Integer.toString(y / 16))) {

			return true;
		}
		return false;

	}

	private int getDirX(String object) {

		switch (object) {

		case "East":
			return 16;
		case "West":
			return -16;
		case "North":
			return 0;
		case "South":
			return 0;
		default:
			return 0;
		}
	}

	private int getDirZ(String object) {

		switch (object) {

		case "North":
			return -16;
		case "South":
			return 16;
		case "East":
			return 0;
		case "West":
			return 0;
		default:
			return 0;
		}
	}

	private int getXCoordString(Object s) {

		String string = (String) s;
		int x;
		if (string.charAt(0) == 'P') {
			x = Integer.parseInt((String) string.subSequence(1, 6));
		} else {
			x = Integer.parseInt("-" + (String) string.subSequence(1, 6));
		}
		return x * 16;
	}

	private int getZCoordString(Object s) {

		String string = (String) s;
		int z;
		if (string.charAt(6) == 'P') {
			z = Integer.parseInt((String) string.subSequence(7, 12));
		} else {
			z = Integer.parseInt("-" + (String) string.subSequence(7, 12));
		}
		return z * 16;

	}
}
