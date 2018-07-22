package de.Ste3et_C0st.FurnitureLib.Command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import com.comphenix.protocol.wrappers.EnumWrappers;

import de.Ste3et_C0st.FurnitureLib.NBT.NBTCompressedStreamTools;
import de.Ste3et_C0st.FurnitureLib.NBT.NBTTagCompound;
import de.Ste3et_C0st.FurnitureLib.Utilitis.MaterialConverter;
import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib;
import de.Ste3et_C0st.FurnitureLib.main.Type.PlaceableSide;

public class converCommand {

	public converCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		//furniture convert <Database/models>
		if(args.length == 2) {
			if(args[1].equalsIgnoreCase("database")) {
				
			}else if(args[1].equalsIgnoreCase("models")) {
				File folder = new File("plugins/" + FurnitureLib.getInstance().getName() + "/Crafting");
				if(folder.exists()) {
					for(File f : folder.listFiles()) {
						YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
						YamlConfiguration newConfig = new YamlConfiguration();
						Reader inReader = new InputStreamReader(FurnitureLib.getInstance().getResource("default.dModel"));
						newConfig.addDefaults(YamlConfiguration.loadConfiguration(inReader));
						newConfig.options().copyDefaults(true);
						newConfig.options().copyHeader(true);
						String header = getHeader(f.getName().replace(".yml", ""), config);
						String systemID = config.getString(header + ".system-ID");
						String name = config.getString(header + ".name");
						try {
							String material = config.getString(header + ".material");
							boolean glow = config.getBoolean(header + ".glow");
							List<String> stringList = new ArrayList<String>();
							if(config.isList(header + ".lore")) stringList = config.getStringList(header + ".lore");
							Material mat = MaterialConverter.getMaterialFromOld(material);
							if(mat == null) return;
							
							newConfig.set(header + ".system-ID", systemID);
							newConfig.set(header + ".displayName", name);
							newConfig.set(header + ".itemGlowEffect", glow);
							newConfig.set(header + ".spawnMaterial", mat.name());
							newConfig.set(header + ".itemLore", stringList);
							
							if(config.contains(header + ".creator")) {
								newConfig.set(header + ".creator", UUID.fromString(config.getString(header + ".creator")).toString());
							}
							
							if(config.contains(header + ".PlaceAbleSide")) {
								newConfig.set(header + ".placeAbleSide", PlaceableSide.valueOf(config.getString(header + ".PlaceAbleSide")).name());
							}
							
							if(config.contains(header + ".crafting.recipe")) {
								newConfig.set(header + ".crafting.recipe", config.getString(header + ".crafting.recipe"));
								newConfig.set(header + ".crafting.disable", config.getBoolean(header + ".crafting.disable"));
								config.getConfigurationSection(header + ".crafting.index").getKeys(false).stream().forEach(letter -> {
									Material cM = MaterialConverter.getMaterialFromOld(config.getString(header + ".crafting.index." + letter));
									newConfig.set(header + ".crafting.index." + letter, cM.name());
								});
							}
							
							
							
							
							if(config.contains(header + ".ProjectModels.ArmorStands")) {
								config.getConfigurationSection(header + ".ProjectModels.ArmorStands").getKeys(false).stream().forEach(letter -> {
									String md5 = config.getString(header + ".ProjectModels.ArmorStands." + letter);
									byte[] by = Base64.decodeBase64(md5);
									ByteArrayInputStream bin = new ByteArrayInputStream(by);
									try {
										NBTTagCompound metadata = NBTCompressedStreamTools.read(bin);
										NBTTagCompound inventory = metadata.getCompound("Inventory");
										NBTTagCompound updatetInventory = new NBTTagCompound();
										for(Object object : EnumWrappers.ItemSlot.values()){
											if(!inventory.getString(object.toString()).equalsIgnoreCase("NONE")){
												NBTTagCompound item = MaterialConverter.convertNMSItemStack(inventory.getCompound(object.toString()+""));
												updatetInventory.set(object.toString(), item);
											}else {
												updatetInventory.setString(object.toString(), "NONE");
											}
										}
										metadata.set("Inventory", updatetInventory);
										byte[] out = NBTCompressedStreamTools.toByte(metadata);
										newConfig.set(header + ".projectData.entitys." + letter, Base64.encodeBase64String(out));
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
							}
							
							if(config.contains(header + ".ProjectModels.Block")) {
								if(config.isConfigurationSection(header + ".ProjectModels.Block")) {
									config.getConfigurationSection(header + ".ProjectModels.Block").getKeys(false).stream().forEach(letter -> {
										double x = config.getDouble(header + ".ProjectModels.Block." + letter + ".X-Offset");
										double y = config.getDouble(header + ".ProjectModels.Block." + letter + ".Y-Offset");
										double z = config.getDouble(header + ".ProjectModels.Block." + letter + ".Z-Offset");
										Material materialBlock = MaterialConverter.getMaterialFromOld(config.getString(header + ".ProjectModels.Block." + letter + ".Type"));
										newConfig.set(header + ".projectData.blockList."+letter+".xOffset", x);
										newConfig.set(header + ".projectData.blockList."+letter+".yOffset", y);
										newConfig.set(header + ".projectData.blockList."+letter+".zOffset", z);
										newConfig.set(header + ".projectData.blockList."+letter+".material", materialBlock.name());
									});
								}
							}
							newConfig.save(new File("plugins/"+FurnitureLib.getInstance().getName()+"/models/" + f.getName().replace(".yml", ".dModel")));
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
					sender.sendMessage("convert finish");
					folder.renameTo(new File("plugins/" + FurnitureLib.getInstance().getName() + "/CraftingOld"));
				}
				sender.sendMessage("Nichts zu konvertieren");
			}
		}
	}
	

	
	public String getHeader(String fileName, YamlConfiguration config){
		try{
			return (String) config.getConfigurationSection("").getKeys(false).toArray()[0];
		}catch(ArrayIndexOutOfBoundsException ex){
			return fileName;
		}
	}

}