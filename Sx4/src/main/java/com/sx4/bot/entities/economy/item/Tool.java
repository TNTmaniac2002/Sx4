package com.sx4.bot.entities.economy.item;

import java.util.List;

public class Tool extends Item {
	
	private final long currentDurability;
	private final long maxDurability;
	
	private final List<ItemStack<Material>> craft;
	private final Material repairItem;

	public Tool(String name, long price, ItemType type, long maxDurability, List<ItemStack<Material>> craft, Material repairItem) {
		this(name, price, type, maxDurability, maxDurability, craft, repairItem);
	}
	
	public Tool(String name, long price, ItemType type, long currentDurability, long maxDurability, List<ItemStack<Material>> craft, Material repairItem) {
		super(name, price, type);
		
		this.currentDurability = currentDurability;
		this.maxDurability = maxDurability;
		this.craft = craft;
		this.repairItem = repairItem;
	}
	
	public long getCurrentDurability() {
		return this.currentDurability;
	}
	
	public long getMaxDurability() {
		return this.maxDurability;
	}
	
	public List<ItemStack<Material>> getCraft() {
		return this.craft;
	}
	
	public Material getRepairItem() {
		return this.repairItem;
	}
	
}