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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

final class VillagerInventory {
	
	private final Villager villager;
	private final Inventory villagerInventory;
	
	VillagerInventory(final Villager villager) {
		
		this.villager = villager;
		this.villagerInventory = Bukkit.createInventory(villager, 9);
		
		final Inventory inventory = villager.getInventory();
		for(int index = 0; index < inventory.getSize(); index++) {
			villagerInventory.setItem(index, inventory.getItem(index));
		}
	}
	
	InventoryView openInventory(final Player player) {
		
		return player.openInventory(villagerInventory);
	}
	
	void closeInventory(final Player player) {
		
		final Inventory inventory = villager.getInventory();
		for(int index = 0; index < inventory.getSize(); index++) {
			inventory.setItem(index, villagerInventory.getItem(index));
		}
	}
}
