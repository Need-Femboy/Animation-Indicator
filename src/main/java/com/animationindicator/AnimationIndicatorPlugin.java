package com.animationindicator;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;

@Slf4j
@PluginDescriptor(
		name = "Animation Indicator",
		description = "Highlights the tiles of a monster when it changes animation",
		tags = {"combat"}
)
public class AnimationIndicatorPlugin extends Plugin
{
	@Inject
	private Client client;
	
	@Inject
	private OverlayManager overlayManager;
	
	@Inject
	private AnimationIndicatorConfig config;
	
	@Inject
	private AnimationIndicatorOverlay overlay;
	
	private final List<String> npcNames = new ArrayList<>();
	@Getter
	private final List<Map.Entry<Integer, NPC>> animStorage = new ArrayList<>();
	private final Set<Integer> ignoredAnims = ImmutableSet.of(AnimationID.IDLE);
	private final Set<String> blockedNpcs = ImmutableSet.of("Verzik Vitur", "JalTok-Jad", "Alchemical Hydra");
	
	protected void startUp()
	{
		updateNpcNames(config.npcList());
		overlayManager.add(overlay);
	}
	
	protected void shutDown()
	{
		npcNames.clear();
		overlayManager.remove(overlay);
	}
	
	@Subscribe
	public void onGameTick(GameTick event)
	{
		int currentTick = client.getTickCount();
		animStorage.removeIf(entry -> entry.getKey() < currentTick);
	}
	
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (!(actor instanceof NPC)) {
			return;
		}
		
		NPC npc = (NPC) actor;
		
		int animId = actor.getAnimation();
		if (ignoredAnims.contains(animId) || !isNpcInList(npc)) {
			return;
		}
		
		animStorage.add(new AbstractMap.SimpleEntry<>(client.getTickCount(), npc));
	}
	
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (MenuAction.of(event.getType() % MENU_ACTION_DEPRIORITIZE_OFFSET) != MenuAction.EXAMINE_NPC) {
			return;
		}
		
		NPC npc = client.getCachedNPCs()[event.getIdentifier()];
		if (npc == null || npc.getName() == null || !client.isKeyPressed(KeyCode.KC_SHIFT)) {
			return;
		}
		
		String option = blockedNpcs.contains(npc.getName())
				? "[Animation Blocked]"
				: isNpcInList(npc) ? "Ignore-Animation" : "Track-Animation";
		
		client.createMenuEntry(-1)
				.setOption(option)
				.setTarget(event.getTarget())
				.setIdentifier(event.getIdentifier())
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setType(MenuAction.RUNELITE)
				.onClick(this::tagNpc);
	}
	
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (AnimationIndicatorConfig.CONFIGNAME.equals(event.getGroup())) {
			updateNpcNames(config.npcList());
		}
	}
	
	private void tagNpc(MenuEntry event) {
		if (event.getType() != MenuAction.RUNELITE) {
			return;
		}
		
		NPC npc = client.getCachedNPCs()[event.getIdentifier()];
		if (npc == null || npc.getName() == null) {
			return;
		}
		
		String name = npc.getName().toLowerCase();
		if (event.getOption().contains("Track")) {
			addNpcToConfig(name, npcNames);
		} else if (event.getOption().contains("Ignore")) {
			removeNpcFromConfig(name, npcNames);
		}
		
		config.setNpcList(Text.toCSV(npcNames));
	}
	
	
	private void updateNpcNames(String configStr) {
		npcNames.clear();
		if (configStr.isEmpty()) {
			return;
		}
		
		Arrays.stream(configStr.split(","))
				.map(String::trim)
				.filter(entry -> !entry.isEmpty())
				.map(String::toLowerCase)
				.forEach(npcNames::add);
	}
	
	private void addNpcToConfig(String name, List<String> list) {
		list.removeIf(entry -> entry.equalsIgnoreCase(name) || entry.startsWith(name + ":"));
		list.add(name);
	}
	
	private void removeNpcFromConfig(String name, List<String> list) {
		list.removeIf(entry -> entry.equalsIgnoreCase(name) || entry.startsWith(name + ":"));
	}
	
	private boolean isNpcInList(NPC npc) {
		String name = npc.getName() == null ? "" : npc.getName().toLowerCase();
		return npcNames.stream()
				.anyMatch(entry -> entry.contains(":")
						? WildcardMatcher.matches(entry.split(":")[0], name)
						: WildcardMatcher.matches(entry, name));
	}
	
	@Provides
	AnimationIndicatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnimationIndicatorConfig.class);
	}
}
