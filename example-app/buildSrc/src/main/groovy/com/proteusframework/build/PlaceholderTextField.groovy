package com.proteusframework.build

import groovy.transform.CompileStatic

import javax.swing.*
import javax.swing.text.Document
import java.awt.*

/**
 * From Peter Tseng.
 * @author Peter Tseng (noreply@i2rd.com)
 */
@CompileStatic
public class PlaceholderTextField extends JTextField
{


    /** placeholder text. */
    private String placeholder;

    /**
     * Constructor.
     */
    public PlaceholderTextField()
    {
    }

    /**
     * Constructor.
     * @param pDoc document.
     * @param pText the text.
     * @param pColumns the columns.
     */
    public PlaceholderTextField(
        final Document pDoc,
        final String pText,
        final int pColumns)
    {
        super(pDoc, pText, pColumns);
    }

    /**
     * Constructor.
     * @param pColumns the columns.
     */
    public PlaceholderTextField(final int pColumns)
    {
        super(pColumns);
    }

    /**
     * Constructor.
     * @param pText the text.
     */
    public PlaceholderTextField(final String pText)
    {
        super(pText);
    }

    /**
     * Constructor.
     * @param pText the text.
     * @param pColumns the columns.
     */
    public PlaceholderTextField(final String pText, final int pColumns)
    {
        super(pText, pColumns);
    }

    /**
     * Get the placeholder text.
     * @return the text.
     */
    public String getPlaceholder()
    {
        return placeholder;
    }

    @Override
    protected void paintComponent(final Graphics pG)
    {
        super.paintComponent(pG);

        if (placeholder.length() == 0 || getText().length() > 0)
        {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;

        def key_antialiasing = RenderingHints.KEY_ANTIALIASING as java.awt.RenderingHints.Key
        def value_antialias_on = RenderingHints.VALUE_ANTIALIAS_ON
        g.setRenderingHint(key_antialiasing, value_antialias_on);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics()
            .getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(final String s)
    {
        placeholder = s;
    }

}