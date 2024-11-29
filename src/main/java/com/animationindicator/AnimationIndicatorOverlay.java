package com.animationindicator;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class AnimationIndicatorOverlay extends Overlay
{
	private final Client client;
	
	private final AnimationIndicatorPlugin plugin;
	
	private final AnimationIndicatorConfig config;
	
	@Inject
	private AnimationIndicatorOverlay(Client client, AnimationIndicatorPlugin plugin, AnimationIndicatorConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}
	
	@Override
	public Dimension render(Graphics2D graphics2D) {
		Color outlineColor = config.outlineColour();
		Color fillColor = config.fillColour();
		int lineAlpha = outlineColor.getAlpha();
		int fillAlpha = fillColor.getAlpha();
		HighlightType highlightType = config.hightlightType();
		
		Color outlineWithAlpha = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), lineAlpha);
		Color fillWithAlpha = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillAlpha);
		
		for (Map.Entry<Integer, NPC> npcStore : plugin.getAnimStorage()) {
			NPC npc = npcStore.getValue();
			NPCComposition npcComposition = npc.getTransformedComposition();
			
			if (npc.isDead() || npcComposition == null) {
				continue;
			}
			
			int size = npcComposition.getSize();
			Shape npcShape = getNpcShape(npc, size, highlightType);
			
			if (npcShape != null) {
				graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics2D.setColor(outlineWithAlpha);
				graphics2D.setStroke(new BasicStroke(2f));
				graphics2D.draw(npcShape);
				graphics2D.setColor(fillWithAlpha);
				graphics2D.fill(npcShape);
			}
		}
		
		return null;
	}
	
	private Shape getNpcShape(NPC npc, int size, HighlightType highlightType) {
		switch (highlightType) {
			case TILE:
			case TRUE_TILE:
				LocalPoint localPoint = highlightType == HighlightType.TILE
						? npc.getLocalLocation()
						: LocalPoint.fromWorld(client, npc.getWorldLocation());
				
				if (localPoint != null) {
					if (highlightType == HighlightType.TRUE_TILE) {
						localPoint = new LocalPoint(
								localPoint.getX() + size * 128 / 2 - 64,
								localPoint.getY() + size * 128 / 2 - 64
						);
					}
					return Perspective.getCanvasTileAreaPoly(client, localPoint, size);
				}
				break;
			case HULL:
				return npc.getConvexHull();
		}
		return null;
	}
}
