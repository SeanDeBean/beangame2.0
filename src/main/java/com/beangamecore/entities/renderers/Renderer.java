package com.beangamecore.entities.renderers;

import org.bukkit.Location;

import com.beangamecore.entities.tntspider.Spider;

public interface Renderer {
    void renderSpider(Spider spider, RenderDebugOptions debug);
    void clearSpider(Spider spider);
    default void renderTarget(Location location, Object identifier) {}
    default void clear(Object identifier) {}
}
