package com.github.frizzy.PoeDDSExtractor;

/**
 * Record tracking the texture information stored within a .dds file.
 *
 * @param name        The name/path of this texture.
 * @param path        The path to the .dds file this texture
 *                    is contained in.
 * @param coordinates The x1, x2, y1, y2 coordinates of the texture
 *                    in the .dds file.
 *
 * @version 0.0.2
 * @since 0.0.2
 */
public record Texture( String name , String path , int[] coordinates ) {



}
