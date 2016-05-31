/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package com.example.app.ui.vtcrop;

import com.example.app.support.VTCropJSLibrary;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.proteusframework.cms.support.ImageFileUtil;
import net.proteusframework.core.StringFactory;
import net.proteusframework.core.image.Thumbnailer;
import net.proteusframework.core.locale.TextSource;
import net.proteusframework.core.locale.annotation.I18N;
import net.proteusframework.core.locale.annotation.I18NFile;
import net.proteusframework.core.locale.annotation.L10N;
import net.proteusframework.core.metric.Dimension;
import net.proteusframework.core.notification.Notifiable;
import net.proteusframework.internet.http.resource.FactoryResource;
import net.proteusframework.internet.http.resource.html.NDE;
import net.proteusframework.ui.miwt.Image;
import net.proteusframework.ui.miwt.MIWTException;
import net.proteusframework.ui.miwt.component.Container;
import net.proteusframework.ui.miwt.component.FileField;
import net.proteusframework.ui.miwt.component.ImageComponent;
import net.proteusframework.ui.miwt.component.PushButton;
import net.proteusframework.ui.miwt.component.composite.editor.ValueEditor;
import net.proteusframework.ui.miwt.event.ActionEvent;
import net.proteusframework.ui.miwt.event.ActionListener;
import net.proteusframework.ui.miwt.util.CommonActions;

/**
 * UI for user to upload a picture.  The picture will be cropped according to the configuration determined by the
 * {@link VTCropPictureEditorConfig}.
 * <p>
 * You can listen for ActionEvents on {@link  #ACTION_UI_VALUE_UPDATED}  action command to be notified when the user has
 * provided a new picture via this UI.
 * </p>
 * @author Alan Holt (aholt@venturetech.net)
 */
@SuppressWarnings("unused")
@I18NFile(symbolPrefix = "com.lrsuccess.ldp.ui.vtcrop.VTCropPictureEditor", i18n = @I18N(symbol = "Button Text Crop Image", l10n
    = @L10N("Done Cropping")))
@Configurable
public class VTCropPictureEditor extends Container implements ValueEditor<FileItem>
{
    /** Logger. */
    private final static Logger _logger = LogManager.getLogger(VTCropPictureEditor.class);

    /** html classname for image selected */ 
    public static final String CLASS_IMAGE_SELECTED = "image-selected";
    /** html classname for image not selected */
    public static final String CLASS_NO_IMAGE_SELECTED = "no-image-selected";
    /** html classname for view mode */
    public static final String CLASS_VIEW_MODE = "mode-view";
    /** html classname for crop mode */
    public static final String CLASS_CROP_MODE = "mode-crop";

    /** UI Value Updated */
    public final static String ACTION_UI_VALUE_UPDATED = "ui-value-updated";

    /** File Field. */
    private final FileField _fileField = new FileField();
    /** File Field. */
    private final ImageComponent _picture = new ImageComponent();

    /** Actions container */
    private final Container _actionsCon = new Container();

    /** UI Value - UI state isn't held in component. */
    private FileItem _uiValue;
    /** Additional UI Values.  Only used if the config specified more than one ImageScale */
    private final HashMap<String, FileItem> _additionalUiValues = new HashMap<>();

    /** Modified Flag. */
    private boolean _modified;

    /** Picture Editor config */
    private final VTCropPictureEditorConfig _config;

    /** Editable mode? */
    private boolean _editable = true;

    /** crop button text */
    private TextSource _cropButtonText;

    /** default resource */
    private FactoryResource _defaultResource;

    /** file item factory */
    @Autowired
    private FileItemFactory _fileItemFactory;

    /**
     * Create an instance.
     *
     * @param cfg the configuration
     */
    public VTCropPictureEditor(VTCropPictureEditorConfig cfg)
    {
        super();
        addClassName("picture-editor");
        _config = cfg;

        _picture.setImageCaching(false);
        _fileField.setAccept("image/*");
        _fileField.addPropertyChangeListener(FileField.PROP_FILE_ITEMS, evt -> {
            @SuppressWarnings("unchecked")
            final List<FileItem> files = (List<FileItem>) evt.getNewValue();
            if (files != null && files.size() > 0)
            {
                _uiValue = files.get(0);
                if(files.size() > 1)
                {
                    _additionalUiValues.clear();
                    for(int i = 1; i < files.size(); i++)
                    {
                        FileItem file = files.get(i);
                        _additionalUiValues.put(file.getName(), file);
                    }
                }
                _picture.setImage(new Image(_uiValue));
                _modified = true;
            }
            _fileField.resetFile();
        });
    }

    @Override
    public List<NDE> getNDEs()
    {
        return Collections.singletonList(VTCropJSLibrary.VTCropPictureEditor.getNDE());
    }

    @Override
    public void init()
    {
        super.init();

        setAttribute("data-crop_opts", _config.toJson());

        // Create drop-zone container in case someone wants to use DnD and VTCrop with this.
        add(of("drop-zone", _picture));

        add(of("crop-file", _fileField));

        _actionsCon.addClassName("actions bottom");
        add(_actionsCon);

        if(getValue() == null && getDefaultResource() != null)
            _picture.setImage(new Image(getDefaultResource()));

        _viewDefault();
    }

    /**
     * Set the base part of the classname based on the data's current state.
     * @param viewMode boolean flag -- if true, adds the view mode classname to component, otherwise adds the crop mode classname
     * if null, does nothing to the mode classnames
     */
    private void setBaseClass(@Nullable Boolean viewMode)
    {
        if(_uiValue == null)
        {
            removeClassName(CLASS_IMAGE_SELECTED);
            addClassName(CLASS_NO_IMAGE_SELECTED);
        }
        else
        {
            removeClassName(CLASS_NO_IMAGE_SELECTED);
            addClassName(CLASS_IMAGE_SELECTED);
        }
        if(viewMode != null)
        {
            if (viewMode)
            {
                removeClassName(CLASS_CROP_MODE);
                addClassName(CLASS_VIEW_MODE);
            }
            else
            {
                removeClassName(CLASS_VIEW_MODE);
                addClassName(CLASS_CROP_MODE);
            }
        }
    }

    /**
     * Switch the UI to the default view.  The default view for editable mode: the file field is hidden and a button is shown to
     * change the picture. In non-editable mode, it just shows the current picture.
     */
    private void _viewDefault()
    {
        setBaseClass(true);

        _actionsCon.removeAllComponents();
        _fileField.setVisible(false);

        if (!_editable)
            return;

        final PushButton change = CommonActions.CHANGE.push();
        final PushButton saveAndCrop = CommonActions.SAVE.push();
        saveAndCrop.addClassName("done-cropping");
        saveAndCrop.setLabel(getCropButtonText() != null
            ? getCropButtonText()
            : VTCropPictureEditorLOK.BUTTON_TEXT_CROP_IMAGE());
        saveAndCrop.setVisible(false);
        final PushButton cancel = CommonActions.CANCEL.push();
        cancel.setVisible(false);
        final PushButton remove = CommonActions.REMOVE.push();
        remove.setVisible(false);

        _actionsCon.add(remove);
        _actionsCon.add(saveAndCrop);
        _actionsCon.add(cancel);
        _actionsCon.add(change);

        saveAndCrop.addActionListener(ev1 -> {
            fire(new ActionEvent(this, this, ACTION_UI_VALUE_UPDATED));
            _viewDefault();
        });

        cancel.addActionListener(ev1 -> _viewDefault());

        remove.addActionListener(ev -> {
            setValue(null);
            _modified = true;
            fire(new ActionEvent(this, this, ACTION_UI_VALUE_UPDATED));
            _viewDefault();
        });

        if (_uiValue != null)
        {
            remove.setVisible(true);
        }

        change.addActionListener(ev -> {
            setBaseClass(false);

            _fileField.setVisible(true);

            change.setVisible(false);
            remove.setVisible(false);
            saveAndCrop.setVisible(true);
            cancel.setVisible(true);
        });
    }

    @Nullable
    @Override
    public FileItem getValue()
    {
        return _uiValue;
    }

    @Override
    public void setValue(@Nullable FileItem value)
    {
        _uiValue = value;

        if (value == null)
        {
            _fileField.resetFile();
            _picture.setImage(getDefaultResource() != null ? new Image(getDefaultResource()) : null);
        }
        else
        {
            _picture.setImage(new Image(value));

        }
        _modified = false;
        setBaseClass(null);

        if(isInited())
            _viewDefault();
    }

    @Override
    public ValueEditor.ModificationState getModificationState()
    {
        return (_modified) ? ValueEditor.ModificationState.CHANGED : ValueEditor.ModificationState.UNCHANGED;
    }

    @Override
    public FileItem commitValue() throws MIWTException
    {
        setValue(getUIValue(Level.DEBUG));
        return getValue();
    }

    @Override
    public boolean validateUIValue(@SuppressWarnings("rawtypes") Notifiable notifiable)
    {
        // Nothing to validate.
        return true;
    }

    @Override
    public boolean isEditable()
    {
        return _editable;
    }

    @Override
    public void setEditable(boolean b)
    {
        if (b != _editable)
        {
            _editable = b;
            _viewDefault();
        }
        else
        {
            _editable = b;
        }
    }

    /**
     * Add an ActionListener to get an event when the user has uploaded a new image to the UI.  This is essentially a
     * notification that the UIValue has changed.
     *
     * @param listener The ActionListener
     */
    public void addActionListener(ActionListener listener)
    {
        addListener(ActionListener.class, listener);
    }

    /**
     * Remove an action listener.
     *
     * @param listener The listener.
     */
    public void removeActionListener(ActionListener listener)
    {
        removeListener(ActionListener.class, listener);
    }


    @Nullable
    @Override
    public FileItem getUIValue(Level logErrorLevel)
    {
        return _getUIValue(_uiValue);
    }

    @Nullable
    private FileItem _getUIValue(@Nullable FileItem uiValue)
    {
        if (uiValue == null)
        {
            return null;
        }

        Dimension origDim, newDim;
        try
        {
            origDim = ImageFileUtil.getDimension(uiValue);
        }
        catch (IOException e)
        {
            _logger.error("Skipping scaling because an error occurred.", e);
            return uiValue;
        }

        if(origDim == null)
        {
            _logger.warn("Skipping scaling because retrieved dimension for ui value was null.");
            return uiValue;
        }

        VTCropPictureEditorConfig.ImageScaleOption option = _config.getImageScaleOptionForName(_uiValue.getName());
        int newDimWidth = option != null && option.scale != null
            ? Double.valueOf(_config.getCropWidth() * option.scale).intValue()
            : _config.getCropWidth();
        int newDimHeight = option != null && option.scale != null
            ? Double.valueOf(_config.getCropHeight() * option.scale).intValue()
            : _config.getCropHeight();

        newDim =
            ImageFileUtil.getScaledDimension(origDim, newDimWidth, newDimHeight);

        if (!newDim.equals(origDim))
        {
            _logger.debug("Scale to " + newDim.getWidthMetric() + " x " + newDim.getHeightMetric());
            FileItem scaledFileItem = _fileItemFactory.createItem(
                uiValue.getFieldName(), uiValue.getContentType(),
                uiValue.isFormField(), uiValue.getName());
            try(InputStream is = uiValue.getInputStream();
                OutputStream os = scaledFileItem.getOutputStream())
            {
                Thumbnailer tn = Thumbnailer.getInstance(is);
                BufferedImage scaledImg =
                    tn.getQualityThumbnail(newDim.getWidthMetric().intValue(), newDim.getHeightMetric().intValue());
                String extension = StringFactory.getExtension(uiValue.getName());
                ImageIO.write(scaledImg, extension, os);
                uiValue.delete();
                uiValue = scaledFileItem;
            }
            catch (Throwable e)
            {
                _logger.warn("Skipping scaling due to:", e);
            }
        }

        return uiValue;
    }

    /**
     * Gets crop button text.
     *
     * @return the crop button text
     */
    public TextSource getCropButtonText()
    {
        return _cropButtonText;
    }

    /**
     * Sets crop button text.
     *
     * @param cropButtonText the crop button text
     */
    public void setCropButtonText(TextSource cropButtonText)
    {
        _cropButtonText = cropButtonText;
    }

    /**
     * Gets default resource.  This is the default image if no image has been set.
     *
     * @return the default resource
     */
    public FactoryResource getDefaultResource()
    {
        return _defaultResource;
    }

    /**
     * Sets default resource. This is the default image if no image has been set.
     *
     * @param defaultResource the default resource
     */
    public void setDefaultResource(FactoryResource defaultResource)
    {
        _defaultResource = defaultResource;
    }

    /**
     *   Get all additional UI Values that were cropped by the cropper.
     *   This should be called if you are expecting additional files, otherwise it will return an empty HashMap.
     *   @return a HashMap of additional UI Values that were cropped/scaled by VTCrop.
     */
    public HashMap<String, FileItem> getAdditionalUiValues()
    {
        HashMap<String, FileItem> additionalUIValues = new HashMap<>();
        _additionalUiValues.entrySet().forEach(entry -> additionalUIValues.put(entry.getKey(), _getUIValue(entry.getValue())));
        return additionalUIValues;
    }
}
