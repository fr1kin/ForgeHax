package com.matt.forgehax.util.color;

/**
 * Created on 2/6/2018 by fr1kin
 */
public interface Colors {
    Color WHITE           = Color.of(255,     255,    255,    255).toImmutable();
    Color BLACK           = Color.of(0,       0,      0,      255).toImmutable();
    Color RED             = Color.of(255,     0,      0,      255).toImmutable();
    Color GREEN           = Color.of(0,       255,    0,      255).toImmutable();
    Color BLUE            = Color.of(0,       0,      255,    255).toImmutable();
    Color ORANGE          = Color.of(255,     128,    0,      255).toImmutable();
    Color PURPLE          = Color.of(163,     73,     163,    255).toImmutable();
    Color GRAY            = Color.of(127,     127,    127,    255).toImmutable();
    Color DARK_RED        = Color.of(64,      0,      0,      255).toImmutable();
    Color YELLOW          = Color.of(255,     255,    0,      255).toImmutable();
}
