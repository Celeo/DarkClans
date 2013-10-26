package com.darktidegames.celeo.clans;

public class DeathCase
{

	public final boolean foughtBack;
	public final boolean attacking;

	/**
	 * 
	 * @param foughtBack
	 *            boolean
	 * @param attacking
	 *            boolean
	 */
	public DeathCase(boolean foughtBack, boolean attacking)
	{
		this.foughtBack = foughtBack;
		this.attacking = attacking;
	}

	/**
	 * @param object
	 *            Object - check against
	 * @return <b>True</b> if the objects are the same
	 */
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof DeathCase))
			return false;
		DeathCase check = (DeathCase) object;
		return check.attacking == this.attacking
				&& check.foughtBack == this.foughtBack;
	}

}