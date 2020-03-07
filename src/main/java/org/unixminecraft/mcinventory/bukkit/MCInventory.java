/*
 * MCInventory Copyright (C) 2020 unixminecraft
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package org.unixminecraft.mcinventory.bukkit;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class MCInventory extends JavaPlugin implements Listener {
	
	private static final String PERMISSION_COMMAND_INVENTORY_USE = "mcinventory.command.inventory.use";
	private static final String PERMISSION_COMMAND_INVENTORY_CLEAR = "mcinventory.command.inventory.clear";
	private static final String PERMISSION_COMMAND_INVENTORY_DESELECT = "mcinventory.command.inventory.deselect";
	private static final String PERMISSION_COMMAND_INVENTORY_HELP = "mcinventory.command.inventory.help";
	private static final String PERMISSION_COMMAND_INVENTORY_OPEN = "mcinventory.command.inventory.open";
	private static final String PERMISSION_COMMAND_INVENTORY_SELECT = "mcinventory.command.inventory.select";
	
	private Logger logger;
	
	private HashSet<UUID> preSelectedPlayers;
	private ConcurrentHashMap<UUID, Villager> selectedVillager;
	private ConcurrentHashMap<UUID, VillagerInventory> openedVillagerInventories;
	
	@Override
	public void onEnable() {
		
		logger = getLogger();
		
		displayLicenseInformation();
		
		preSelectedPlayers = new HashSet<UUID>();
		selectedVillager = new ConcurrentHashMap<UUID, Villager>();
		openedVillagerInventories = new ConcurrentHashMap<UUID, VillagerInventory>();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(final CommandSender commandSender, final Command command, final String alias, final String[] parameters) {
		
		final HashSet<String> commands = new HashSet<String>();
		
		commands.add("inventory");
		
		if(!commands.contains(command.getName())) {
			
			logger.log(Level.WARNING, "Command " + command.getName() + " is being executed by MCInventory.");
			logger.log(Level.WARNING, "The command is not registered to MCInventory.");
			
			sendMessage(commandSender, "&cInternal error, please try again. If the issue persists, please contact a server administrator.&r");
			return true;
		}
		
		if(!(commandSender instanceof Player)) {
			
			sendMessage(commandSender, "&cThis command can only be executed by a Player.&r");
			return true;
		}
		
		final Player player = (Player) commandSender;
		
		if(command.getName().equals("inventory")) {
			
			if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_USE)) {
				sendMessage(player, "&cNo permission.&r");
				return true;
			}
			
			if(parameters.length == 0) {
				
				sendMessage(player, "&aThis command is used for manipulating the inventories of Villagers.&r");
				sendMessage(player, "&aFor more command options, use \"&r&6/inventory help&r&a\".&r");
				return true;
			}
			else if(parameters.length == 1) {
				
				if(parameters[0].equalsIgnoreCase("clear")) {
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_CLEAR)) {
						sendMessage(player, "&cNo permission.&r");
						return true;
					}
					
					final UUID playerId = player.getUniqueId();
					if(!selectedVillager.containsKey(playerId)) {
						sendMessage(player, "&cNo Villager selected. Can't clear nothing's inventory...&r");
						return true;
					}
					
					selectedVillager.get(playerId).getInventory().clear();
					
					sendMessage(player, "&aVillager's inventory cleared.&r");
					return true;
				}
				else if(parameters[0].equalsIgnoreCase("deselect")) {
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_DESELECT)) {
						sendMessage(player, "&cNo permission.&r");
						return true;
					}
					
					final UUID playerId = player.getUniqueId();
					if(!selectedVillager.containsKey(playerId)) {
						sendMessage(player, "&cYou haven't selected a Villager, we can't deselect nothing.&r");
						return true;
					}
					
					selectedVillager.remove(playerId);
					
					sendMessage(player, "&aDe-selected Villager.&r");
					return true;
				}
				else if(parameters[0].equalsIgnoreCase("help")) {
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_HELP)) {
						
						sendMessage(player, "&cNo permission. You should talk to a server administrator and get this fixed.&r");
						sendMessage(player, "&cYou don't even have permission for the \"/inventory help\" command, that's not good.&r");
						return true;
					}
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_USE)) {
						
						sendMessage(player, "&cYou clearly broke something, you don't even have permission to use the base \"/inventory\" command.&r");
						sendMessage(player, "&cGo talk to a server administrator, that should probably be fixed.&r");
						return true;
					}
					
					sendMessage(player, "&6/inventory&r &a->&r &bThe base command. Only informative, doesn't do much else.&r");
					
					if(player.hasPermission(PERMISSION_COMMAND_INVENTORY_CLEAR)) {
						sendMessage(player, "&6/inventory clear&r &a->&r &bClears the selected Villager's inventory. You have to have selected a Villager for this to work.&r");
					}
					if(player.hasPermission(PERMISSION_COMMAND_INVENTORY_DESELECT)) {
						sendMessage(player, "&6/inventory deselect&r &a->&r &bDeselects the currently-selected Villager. You are required to have selected a Villager first.&r");
					}
					
					sendMessage(player, "&6/inventory help&r &a->&r &bThis command. Displays the help message. Don't know what else you were expecting.&r");
					
					if(player.hasPermission(PERMISSION_COMMAND_INVENTORY_OPEN)) {
						sendMessage(player, "&6/inventory open&r &a->&r &bOpens the currently-selected Villager's inventory. Selecting a Villager is highly recommended before attempting to do this.&r");
					}
					if(player.hasPermission(PERMISSION_COMMAND_INVENTORY_SELECT)) {
						sendMessage(player, "&6/inventory select&r &a->&r &bSelects a Villager to perform inventory operations on. Will override any previous selection.&r");
					}
					
					return true;
				}
				else if(parameters[0].equalsIgnoreCase("open")) {
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_OPEN)) {
						sendMessage(player, "&cNo permission.&r");
						return true;
					}
					
					final UUID playerId = player.getUniqueId();
					if(!selectedVillager.containsKey(playerId)) {
						sendMessage(player, "&cNo Villager selected. Opening the void is a bit difficult.&r");
						return true;
					}
					
					final VillagerInventory villagerInventory = new VillagerInventory(selectedVillager.get(playerId));
					villagerInventory.openInventory(player);
					
					openedVillagerInventories.put(playerId, villagerInventory);
					
					return true;
				}
				else if(parameters[0].equalsIgnoreCase("select")) {
					
					if(!player.hasPermission(PERMISSION_COMMAND_INVENTORY_SELECT)) {
						sendMessage(player, "&cNo permission.&r");
						return true;
					}
					
					final UUID playerId = player.getUniqueId();
					if(preSelectedPlayers.add(playerId)) {
						
						sendMessage(player, "&aSelect a villager by right-clicking on them.&r");
						return true;
					}
					else {
						
						sendMessage(player, "&cYou haven't yet selected any villager from last time. Try selecting one before attempting this command again.&r");
						return true;
					}
				}
				else {
					
					sendMessage(player, "&cInvalid usage of the \"/inventory\" command.&r &bPlease see \"&r&6/inventory help&r&b\" for a list of available commands.&r");
					return true;
				}
			}
			else {
				
				sendMessage(player, "&cToo many arguments!&r &bUse \"&r&6/inventory help&r&b for command syntax.&r");
				return true;
			}
		}
		else {
						
			logger.log(Level.WARNING, "Command " + command.getName() + " is not registered with MCInventory.");
			logger.log(Level.WARNING, "The command already passed the registration check. Further investigation required.");
			
			sendMessage(player, "&cInternal error, please try again. If the issue persists, please contact a server administrator.&r");
			return true;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(final EntityDeathEvent event) {
		
		final Entity entity = event.getEntity();
		if(!(entity instanceof Player) && !(entity instanceof Villager)) {
			return;
		}
		
		if(entity instanceof Player) {
			
			final UUID playerId = ((Player) entity).getUniqueId();
			if(preSelectedPlayers.contains(playerId)) {
				preSelectedPlayers.remove(playerId);
			}
			
			return;
		}
		else if(entity instanceof Villager) {
			
			final Villager villager = (Villager) entity;
			if(!selectedVillager.containsValue(villager)) {
				return;
			}
			
			final HashSet<UUID> playerIds = new HashSet<UUID>();
			for(final UUID playerId : selectedVillager.keySet()) {
				if(selectedVillager.get(playerId).equals(villager)) {
					playerIds.add(playerId);
				}
			}
			
			final Server server = getServer();
			for(final UUID playerId : playerIds) {
				
				selectedVillager.remove(playerId);
				final Player player = server.getPlayer(playerId);
				
				if(player == null) {
					continue;
				}
				if(!player.isOnline()) {
					continue;
				}
				
				sendMessage(player, "&6The Villager you selected has died and has been automatically deselected.&r");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClose(final InventoryCloseEvent event) {
		
		final HumanEntity humanEntity = event.getPlayer();
		if(!(humanEntity instanceof Player)) {
			return;
		}
		
		final Player player = (Player) humanEntity;
		final UUID playerId = player.getUniqueId();
		
		if(!openedVillagerInventories.containsKey(playerId)) {
			return;
		}
		
		openedVillagerInventories.get(playerId).closeInventory(player);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
		
		final Player player = event.getPlayer();
		final UUID playerId = player.getUniqueId();
		
		if(!preSelectedPlayers.contains(playerId)) {
			return;
		}
		
		preSelectedPlayers.remove(playerId);
		
		final Entity entity = event.getRightClicked();
		if(!(entity instanceof Villager)) {
			return;
		}
		
		final Villager villager = (Villager) entity;
		
		event.setCancelled(true);
		
		if(!selectedVillager.containsKey(playerId)) {
			
			selectedVillager.put(playerId, villager);
			sendMessage(player, "&aVillager selected.&r");
		}
		else if(selectedVillager.get(playerId).equals(villager)) {
			
			selectedVillager.put(playerId, villager);
			sendMessage(player, "&6No update, Villager already selected.&r");
		}
		else {
			
			selectedVillager.put(playerId, villager);
			sendMessage(player, "&aSelected Villager updated.&r");
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		
		final UUID playerId = event.getPlayer().getUniqueId();
		
		preSelectedPlayers.remove(playerId);
		selectedVillager.remove(playerId);
	}
	
	private void sendMessage(final CommandSender commandSender, final String message) {
		
		commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	private void displayLicenseInformation() {
		
		logger.log(Level.INFO, "//==============================================//");
		logger.log(Level.INFO, "// MCTeleports Copyright (C) 2020 unixminecraft //");
		logger.log(Level.INFO, "//                                              //");
		logger.log(Level.INFO, "// This program is free software: you can       //");
		logger.log(Level.INFO, "// redistribute it and/or modify it under the   //");
		logger.log(Level.INFO, "// terms of these GNU General Public License as //");
		logger.log(Level.INFO, "// published by the Free Software Foundation,   //");
		logger.log(Level.INFO, "// either version 3 of the License, or          //");
		logger.log(Level.INFO, "// (at your opinion) any later version.         //");
		logger.log(Level.INFO, "//                                              //");
		logger.log(Level.INFO, "// This program is distributed in the hope that //");
		logger.log(Level.INFO, "// it will be useful, but WITHOUT ANY WARRANTY; //");
		logger.log(Level.INFO, "// without even the implied warranty of         //");
		logger.log(Level.INFO, "// MERCHANTABILITY or FITNESS FOR A PARTICULAR  //");
		logger.log(Level.INFO, "// PURPOSE. See GNU General Public License for  //");
		logger.log(Level.INFO, "// more details.                                //");
		logger.log(Level.INFO, "//                                              //");
		logger.log(Level.INFO, "// You should have received a copy of the GNU   //");
		logger.log(Level.INFO, "// General Public License along with this       //");
		logger.log(Level.INFO, "// program. If not, see                         //");
		logger.log(Level.INFO, "// <http://www.gnu.org/licenses/>               //");
		logger.log(Level.INFO, "//==============================================//");
	}
}
