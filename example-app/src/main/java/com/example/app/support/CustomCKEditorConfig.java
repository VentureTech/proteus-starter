package com.example.app.support;

import javax.annotation.Nonnull;

/**
 * Rich editor config types.
 *
 * @author Alan Holt (aholt@venturetech.net)
 * @since 12/16/15 9:59 AM
 */
public enum CustomCKEditorConfig
{
    /** Standard ck editor configuration */
    standard("standard-custom.js"),
    /** Minimal ck editor configuration */
    minimal("minimal-custom.js");

    private final String _jsFile;

    CustomCKEditorConfig(@Nonnull String jsFile)
    {
        _jsFile = jsFile;
    }


    @Override
    public String toString()
    {
        return _jsFile;
    }
}
