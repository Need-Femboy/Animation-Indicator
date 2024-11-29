package com.animationindicator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AnimationIndicatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AnimationIndicatorPlugin.class);
		RuneLite.main(args);
	}
}