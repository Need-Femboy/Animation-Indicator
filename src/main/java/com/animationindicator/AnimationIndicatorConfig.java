package com.animationindicator;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

import static com.animationindicator.AnimationIndicatorConfig.CONFIGNAME;

@ConfigGroup(CONFIGNAME)
public interface AnimationIndicatorConfig extends Config
{
	String CONFIGNAME = "AnimationIndicator";
	
	@ConfigItem(
			keyName = "npcList",
			name = "NPC names",
			description = "",
			position = 0
	)
	default String npcList()
	{
		return "";
	}
	
	@ConfigItem(
			keyName = "npcList",
			name = "",
			description = ""
	)
	void setNpcList(String names);
	
	@Alpha
	@ConfigItem(
			keyName = "fillColour",
			name = "Fill Colour",
			description = "",
			position = 1
	)
	default Color fillColour()
	{
		return new Color(0, 255, 0, 24);
	}
	
	@Alpha
	@ConfigItem(
			keyName = "outlineColour",
			name = "Outline Colour",
			description = "",
			position = 2
	)
	default Color outlineColour()
	{
		return new Color(0, 86, 0, 132);
	}
	
	@ConfigItem(
			keyName = "highlightType",
			name = "Hightlight type",
			description = "",
			position = 3
	)
	default HighlightType hightlightType()
	{
		return HighlightType.TILE;
	}
	
}
