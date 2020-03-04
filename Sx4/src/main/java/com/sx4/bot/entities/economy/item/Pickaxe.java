package com.sx4.bot.entities.economy.item;

import java.util.List;

public class Pickaxe extends Tool {

	private final long minYield;
	private final long maxYield;
	
	private final double multiplier;

	public Pickaxe(String name, long price, long currentDurability, long maxDurability, List<ItemStack<Material>> craft, Material repairItem, long minYield, long maxYield, double multiplier) {
		super(name, price, ItemType.PICKAXE, currentDurability, maxDurability, craft, repairItem);
		
		this.minYield = minYield;
		this.maxYield = maxYield;
		this.multiplier = multiplier;
	}
	
	public Pickaxe(String name, long price, long maxDurability, List<ItemStack<Material>> craft, Material repairItem, long minYield, long maxYield, double multiplier) {
		this(name, price, maxDurability, maxDurability, craft, repairItem, minYield, maxYield, multiplier);
	}
	
	public double getMultiplier() {
		return this.multiplier;
	}
	
	public long getMinYield() {
		return this.minYield;
	}
	
	public long getMaxYield() {
		return this.maxYield;
	}
	
}
