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

/**
 * Example client side image cropper integration for MIWT.
 * @requires dnd-file.js
 * @requires jquery.js
 * @requires jquery.color.js
 * @requires jquery.Jcrop.js
 * @requires jquery.Jcrop.css
 */
(function (w, d, $) {
  var PICTURE_SELECTOR = '.picture-editor';
  var CROPPED_PICTURE_DATA = 'cropped-picture-data[data-url]1';
  var CROPPED_PICTURE_NAME = 'cropped-picture-data[data-url]1[name]';
  var CROP_SELECTION_MIN_BOX_WIDTH = 20;
  var CROP_SELECTION_MIN_BOX_HEIGHT = 20;

  var CSS_CANCEL_BUTTON_CLASS = 'cancel';
  var CSS_SAVE_BUTTON_CLASS = 'save';
  var CSS_NO_IMAGE_CLASS = 'no-image-selected';
  var CSS_HIDDEN_CLASS = 'hidden';
  var CSS_UNLOCKED_CLASS = 'unlocked';
  var CSS_LOCKED_CLASS = 'locked';
  var CSS_MODE_CROP = 'mode-crop';
  var onSubmitChain;

  var cropSettingDefaults = {
    parent_target: $(PICTURE_SELECTOR).get(0),
    image_selector: undefined,
    accept_undefined_image_target: false,
    image_target: undefined,
    drop_zone_target: undefined,
    constrain_aspectratio: undefined,
    min_width: 60,
    min_height: 60,
    max_width: 500,
    max_height: 500,
    crop_width: 200,
    crop_height: 200,
    image_type: 'image/jpeg',
    image_scales: [{ scale: 1.0, quality: 1.0, fileName: 'img1' }]
  };

  function VTImageCropper() {
    var vtcrop;
    var dnd, $img;
    var cropSettings;
    var $context, $dropZone,
        miwtInputDatas = [],
        miwtInputNames = [];
    var api;
    var previousIsCrop;

    function resize(el, w, h) {
      el.width = w;
      el.height = h;
      el.style.width = w + 'px';
      el.style.height = h + 'px';
    }

    function updateImage() {
      var w =  $img.width();
      var h = $img.height();
      var dropZone = dnd.getDropZone();

      resize(dropZone, w, h);
    }

    function loadImageIntoWorkspace(imgFile) {
      if (!imgFile.type.match(/image.*/)) return;
      var reader = new FileReader();
      reader.onload = function (e) {
        if(vtcrop) {
          vtcrop.setImageSrc(e.target.result);
        } else {
          $img.attr('src', e.target.result);
        }
      };
      reader.readAsDataURL(imgFile);
    }

    function saveCroppedImageData(callback) {
      if(vtcrop) {
        vtcrop.crop(function cropCallback(imgDataArr) {
          for(var i = 0; i < imgDataArr.length; i++) {
            miwtInputDatas[i].val(imgDataArr[i]);
          }
          callback();
        });
      }
    }

    function clearCroppedImageData(callback) {
      miwtInputDatas.forEach(function(val, index, array) {
        val.val('');
      });
      callback();
    }

    function onFilesSelected(fileList) {
      if(fileList && fileList.length > 0)
        loadImageIntoWorkspace(fileList[0]);
      dnd.clearFileInput();
    }

    function createHiddenInput(id, name, value) {
      var dataInput = d.createElement('input');
      dataInput.type = 'hidden';
      dataInput.name = name;
      dataInput.id = id;
      dataInput.value = value;
      return dataInput;
    }

    function isSupported() {
      return w.cms && w.cms.file && w.cms.file.DnD;
    }

    function isCropMode() {
      return $context.hasClass(CSS_MODE_CROP);
    }

    function hasChangedMode() {
      return !!previousIsCrop != isCropMode();
    }

    function initConfig() {
      var config = $context.data('crop_opts');
      cropSettings = $.extend({}, cropSettingDefaults, config);
      cropSettings.parent_target = $context.get(0);
      cropSettings.image_selector = 'img';
    }

    function initCrop() {
      var fileInput;
      $dropZone = $context.find('.drop-zone');
      $img = $context.hasClass(CSS_NO_IMAGE_CLASS) ? $([]) : $context.find(cropSettings.image_selector);
      $img.css("max-height", cropSettings.max_height);
      $img.css("max-width", cropSettings.max_width);
      cropSettings.image_target = $img.get(0);
      cropSettings.accept_undefined_image_target = true;
      fileInput = $context.find('input[type=file]').get(0);
      if(cropSettings.image_scales != undefined && cropSettings.image_scales.length > 0) {
        cropSettings.image_scales.forEach(function(val, index, array) {
          var imgNum = index + 1;
          miwtInputDatas.push($(createHiddenInput('cropped-picture-data[data-url]' + imgNum,
              fileInput.id + '[data-url]' + imgNum, '')));
          miwtInputNames.push($(createHiddenInput('cropped-picture-data[data-url]' + imgNum + '[name]',
              fileInput.id + '[data-url]' + imgNum + '[name]', val.fileName != undefined ? val.fileName : 'img' + imgNum)));
        });
      }
      miwtInputDatas.forEach(function(val, index, array) {
        $dropZone.append(val);
      });
      miwtInputNames.forEach(function(val, index, array) {
        $dropZone.append(val);
      });

      if (vtcrop) { vtcrop.destroy(); }
      vtcrop = new VTCrop(cropSettings);
      $img = $(vtcrop.getImageTarget());

      if (dnd) { dnd.destroy(); }
      dnd = new w.cms.file.DnD(fileInput, $dropZone.get(0));

      // $img.each(function(){
      //   if (this.complete) $img.load();
      // });

      $img.attr('id', fileInput.id + "-picture");

      dnd.addListener(onFilesSelected);
    }

    function init(context) {
      $context = $(context);

      initConfig();
      update();

      $context.data('image-cropper-init', true);
      $context.data('image-cropper', api);

      return true;
    }

    function update() {
      if (hasChangedMode()) {
        destroy();

        if (isCropMode()) {
          initCrop();
        }
      }

      previousIsCrop = isCropMode();
    }

    function destroy() {
      if (dnd) dnd.destroy();
      if (vtcrop) vtcrop.destroy();
      if ($img && $img.length) $img.off('load');
      $context.data('image-cropper-init', false);
      $context.data('image-cropper', {});
    }

    api = {
      init: init,
      update: update,
      clearCroppedImageData: clearCroppedImageData,
      saveCroppedImageData: saveCroppedImageData,
      destroy: destroy
    };

    return api;
  }

  function VTImageCropperManager() {
    var $context;

    function update() {
      addCroppers();
    }

    function submitForm(form) {
      if(onSubmitChain) {
        onSubmitChain.call(form);
      } else if(form.MIWTSubmit) {
        form.MIWTSubmit();
      } else {
        form.submit();
      }
    }

    function onSubmitHandeler(e) {
      var form = this;
      if(e)
        e.preventDefault();

      var btnhitId = $context.find('input[name=btnhit]').val();

      if (btnhitId.length) {
        var $btnhitEl = $(document.getElementById(btnhitId));
        var $cropper = $btnhitEl.closest(PICTURE_SELECTOR);

        if ($btnhitEl.length && $cropper.length) {
          function submitFormCallback() {
            $(form).unbind('submit', onSubmitHandeler);
            submitForm(form);
          }

          if ($btnhitEl.hasClass(CSS_CANCEL_BUTTON_CLASS)) {
            $cropper.data('image-cropper').clearCroppedImageData(submitFormCallback);
            return false;
          } else if ($btnhitEl.hasClass(CSS_SAVE_BUTTON_CLASS)) {
            $cropper.data('image-cropper').saveCroppedImageData(submitFormCallback);
            return false;
          }
        }
      }
      submitForm(form);
      return false;
    }

    function addCroppers() {
      $context.find(PICTURE_SELECTOR).each(function() {
        var $cropper = $(this);
        if ($cropper.data('image-cropper-init')) {
          $cropper.data('image-cropper').update();
        } else {
          var ic = new VTImageCropper();
          ic.init(this);
        }
      });
    }

    function init(context) {
      $context = $(context);

      addCroppers();
    }

    return {
      init: init,
      update: update,
      callbacks: {
        onSubmit: onSubmitHandeler
      }
    };
  }

  function init() {
    if(onSubmitChain)
      return;
    var forms = $('form');
    forms.each(function(idx, form) {
      if($(form).find(PICTURE_SELECTOR).length > 0) {
        var icm = new VTImageCropperManager();
        if($(form).hasClass('miwt-form')) {
          var originalPostUpdate = $.noop;
          if (!form.submit_options) {
            form.submit_options = {};
          }

          if (form.submit_options.postUpdate) {
            originalPostUpdate = form.submit_options.postUpdate;
          }

          form.submit_options.postUpdate = function() {
            originalPostUpdate();
            icm.update();
          };
          onSubmitChain = form.MIWTSubmit;
          form.MIWTSubmit = icm.callbacks.onSubmit;

          icm.init(form);
        } else {
          $(form).bind('submit', icm.callbacks.onSubmit);
        }
      }
    });
  }

  $(init);
  $(function() {
    if(w.miwtajax) miwtajax.addMIWTAJAXRequestCompleteCallback(init);
  });
})(window, document, jQuery);
