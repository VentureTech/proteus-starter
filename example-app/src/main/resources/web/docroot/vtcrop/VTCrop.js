//Example crop_opts
var crop_opts = {
  //The parent element that contains the image and all associated elements for cropping
  parent_target: $(document).get(0),
  //The sub-selector used to find the img element within the parent_target
  image_selector: undefined,
  //Boolean flag:  if true, a default value will not be assigned to image_target if crop_opts.image_target is undefined
  accept_undefined_image_target: false,
  //The img element.  If not assigned, it will be found.  Also see accept_undefined_image_target documentation
  image_target: undefined,
  //The drop zone target element
  drop_zone_target: undefined,
  //Boolean flag:  if true, constrain the image during resizing to the images original aspect ratio
  constrain_aspectratio: undefined,
  //Image minimum width
  min_width: 60,
  //Image minimum height
  min_height: 60,
  //Image and Parent maximum width
  max_width: 500,
  //Image and Parent maximum height
  max_height: 500,
  //The crop width.  This will be the width of the cropped image.
  crop_width: 200,
  //The crop height.  This will be the height of the cropped image.
  crop_height: 200,
  //The image background string.  This will be the background of the image.
  //Keep in mind that if you use the default (transparent), the image_type will be set to the default no matter what.
  image_background_str: undefined,
  //The image type of the image element
  image_type: undefined,
  //Additional image scale sizes. Will scale the cropped image from the original to a size based on scale size.
  //If I crop to 100 x 100 and I specify an additional scale size of 2.0,
  //it will include an additional image in the crop method callback that is 200 x 200. Example:  [2.0, 0.5]
  image_scales: [{ scale: 1.0, quality: 1.0, fileName: 'img1' }]
};
/**
 * Client side image cropper.
 * @requires jquery.js
 */
function VTCrop(crop_opts) {
  // Some variable and settings
  var container_class = 'resize-container',
      container_selector = '.' + container_class,
      $container_target,
      hide_container = false,
      container_display = undefined,
      orig_src = new Image(),
      is_initted = false,
      is_destroyed = false;

  var $parent_target = $(crop_opts.parent_target),
      parentTargetId = $parent_target.attr('id'),
      parentTargetSelector = '#' + parentTargetId;

  var image_selector = crop_opts.image_selector != undefined ? crop_opts.image_selector : 'img',
      image_target = crop_opts.image_target != undefined
          ? crop_opts.image_target
          : crop_opts.accept_undefined_image_target
          ? undefined
          : $parent_target.find(image_selector).get(0);

  //noinspection LongLine
  var drop_zone_class = 'drop-zone',
      drop_zone_selector = '.' + drop_zone_class,
      drop_zone_target = crop_opts.drop_zone_target != undefined ? crop_opts.drop_zone_target : $parent_target.find(drop_zone_selector).get(0);

  var overlay_class = 'overlay',
      overlay_selector = '.' + overlay_class,
      overlay_target = undefined;

  var overlay_inner_class = 'overlay-inner',
      overlay_inner_selector = '.' + overlay_inner_class,
      overlay_inner_target = undefined;

  var event_state = {},
      constrain = crop_opts.constrain_aspectratio != undefined ? crop_opts.constrain_aspectratio : true;

  //noinspection LongLine
  var min_width = crop_opts.min_width,
      min_height = crop_opts.min_height,
      max_width = crop_opts.max_width,
      max_height = crop_opts.max_height,
      crop_width = crop_opts.crop_width,
      crop_height = crop_opts.crop_height,
      default_image_background_str = 'rgba(0, 0, 200, 0)',
      image_background_str = crop_opts.image_background_str != undefined
          ? crop_opts.image_background_str
          : default_image_background_str,
      image_type = crop_opts.image_type != undefined && image_background_str !== default_image_background_str
          ? crop_opts.image_type
          : 'image/png',
      image_scales = crop_opts.image_scales != undefined ? crop_opts.image_scales : [{ scale: 1.0, quality: 1.0, fileName: 'img1' }];

  var $overlayBefore = undefined,
      $overlayAfter = undefined,
      $overlayInnerBefore = undefined,
      $overlayInnerAfter = undefined;

  var resize_canvas = document.createElement('canvas'),
      drop_zone_canvas = document.createElement('canvas');

  //noinspection LongLine
  var constrain_button_label = 'Lock Aspect Ratio',
      constrained_message = '<div class="message message-locked">Uncheck the box to unlock aspect ratio.</div>',
      constrained_message_selector = '.message-locked',
      unconstrained_message = '<div class="message message-unlocked">Check the box to lock aspect ratio to image\'s original aspect ratio.</div>',
      unconstrained_message_selector = '.message-unlocked',
      $constrain_button = $([
        '<div class="option opt-aspect-ratio">',
        '<label><input type="checkbox" name="vtcrop-aspect-ratio" />' + constrain_button_label + '</label>',
        '</div>'
      ].join(''));

  var begin_action_events = 'mousedown touchstart',
      continue_action_events = 'mousemove touchmove',
      end_action_events = 'mouseup touchend mouseleave touchcancel touchleave';

  var brief_tips_text = 'Tip: The cropped area of the image must be inside the solid red border box.',
      brief_tips = '<div class="message brief-tips">' + brief_tips_text + '</div>',
      $brief_tips = $(brief_tips);

  var done_cropping_button_selector = '.done-cropping',
      done_cropping_button_display = undefined;

  window.VTCROP_DEBUG = false || window.VTCROP_DEBUG;

  function init(){
    //Only bother initting the cropper if it is not initted or if it is destroyed
    if(!is_initted || is_destroyed) {
      $parent_target.prepend(brief_tips);

      //Make sure that drop_zone_target is not undefined
      if(drop_zone_target == undefined) {
        $parent_target.prepend('<div class="' + drop_zone_class + '"></div>');
        drop_zone_target = $parent_target.find(drop_zone_selector).get(0);
      }
      overlay_target = $(drop_zone_target).find(overlay_selector).get(0);

      //Make sure that overlay_target is not undefined
      if(overlay_target == undefined) {
        $(drop_zone_target).prepend('<div class="' + overlay_class + '"></div>');
        overlay_target = $(drop_zone_target).find(overlay_selector).get(0);
      }
      overlay_inner_target = $(overlay_target).find(overlay_inner_selector).get(0);

      //Make sure that overlay_inner_target is not undefined
      if(overlay_inner_target == undefined) {
        $(overlay_target).prepend('<div class="' + overlay_inner_class + '"></div>');

        overlay_inner_target = $(overlay_target).find(overlay_inner_selector).get(0);
      }

      //Make sure the image_target is not undefined
      if(image_target == undefined) {
        if($(drop_zone_target).find('img').length == 0) {
          $(drop_zone_target).append('<img />');
        }
        hide_container = true;

        image_target = $(drop_zone_target).find('img').get(0);

        drop_zone_canvas.getContext('2d').fillText('Drop image here or choose below', 75, 80);
        $(drop_zone_target).append(drop_zone_canvas);
        $(drop_zone_canvas).css({
          top: '50%',
          left: '50%',
          transform: 'translateX(-50%) translateY(-50%)',
          'z-index': '999',
          position: 'absolute'
        });
      }

      //Set up the constrain text on the parent element
      if(!is_initted) {
        $constrain_button.find('input').on('click', function(e) {
          if(constrain) {
            $(constrained_message_selector).remove();
            $constrain_button.append($(unconstrained_message_selector));
            constrain = false;
          } else {
            $(unconstrained_message_selector).remove();
            $constrain_button.append($(constrained_message_selector));
            constrain = true;
            preScale({use_orig_ar: true});
          }
        });
      }
      var checked = constrain;
      var messageElement = constrain ? constrained_message : unconstrained_message;
      $constrain_button.find("input").attr("checked", checked);
      $constrain_button.append(messageElement);
      $(drop_zone_target).after($constrain_button);

      //Make sure the image_target is inside the drop_zone
      $(image_target).remove();
      $(overlay_target).after(image_target);

      // When resizing, we will always use this copy of the original as the base
      orig_src.src = image_target.src;

      //Set up the resize handles
      $(image_target).wrap('<div class="' + container_class + '"></div>')
          .before('<span class="resize-handle resize-handle-nw"></span>')
          .before('<span class="resize-handle resize-handle-ne"></span>')
          .after('<span class="resize-handle resize-handle-se"></span>')
          .after('<span class="resize-handle resize-handle-sw"></span>');

      $container_target = $(drop_zone_target).find(container_selector);
      container_display = $container_target.css("display");
      done_cropping_button_display = $parent_target.find(done_cropping_button_selector).css("display");
      if(hide_container) {
        $container_target.css({display: 'none'});
        $parent_target.find(done_cropping_button_selector).css({display: 'none'});
      }

      //Set the overlay width and height based on crop_opts
      $(overlay_target).width(crop_width);
      $(overlay_target).height(crop_height);

      //Set the :before and :after values
      var pseudoCropWidth = crop_width + 4;
      var pseudoCropHeight = crop_height + 4;
      //noinspection LongLine
      $overlayBefore = $("<style>" + parentTargetSelector + " " + overlay_selector + ":before{width: " + pseudoCropWidth + "px;}</style>");
      //noinspection LongLine
      $overlayAfter = $("<style>" + parentTargetSelector + " " + overlay_selector + ":after{width: " + pseudoCropWidth + "px;}</style>");
      //noinspection LongLine
      $overlayInnerBefore = $("<style>" + parentTargetSelector + " " + overlay_selector + " " + overlay_inner_selector + ":before{height: " + pseudoCropHeight + "px;}</style>");
      //noinspection LongLine
      $overlayInnerAfter = $("<style>" + parentTargetSelector + " " + overlay_selector + " " + overlay_inner_selector + ":after{height: " + pseudoCropHeight + "px;}</style>");
      $overlayBefore.appendTo('head');
      $overlayAfter.appendTo('head');
      $overlayInnerBefore.appendTo('head');
      $overlayInnerAfter.appendTo('head');

      //Pre-scale the image, if it is larger than the crop width or crop height
      preScale({line_up_overlay: true});

      //Set other style settings
      $(drop_zone_target).css('height', max_height);
      $(drop_zone_target).css('max-height', max_height);
      $(drop_zone_target).css('width', max_width);
      $(drop_zone_target).css('max-width', max_width);
      $parent_target.css('width', max_width);
      $parent_target.css('max-width', max_width);

      //Remove the jcrop-wrapper if it exists
      $parent_target.find('.jcrop-wrapper').remove();

      var eventData = {
        event_state: event_state,
        parent_target: $parent_target.get(0),
        container_target: $container_target.get(0),
        resize_image_callback: resizeImage,
        image_target: image_target,
        orig_src: orig_src,
        get_constrained_height_callback: getConstrainedHeight,
        get_constrained_width_callback: getConstrainedWidth
      };

      // Add events
      $parent_target.on(begin_action_events, ' .resize-handle', eventData, startResize);
      $parent_target.on(begin_action_events, ' img', eventData, startMoving);

      is_initted = true;
      is_destroyed = false;
    }
  }

  function startResize(e){
    e.preventDefault();
    e.stopPropagation();
    saveEventState(e);
    $(e.data.parent_target).on(continue_action_events, null, e.data, resizing);
    $(e.data.parent_target).on(end_action_events, null, e.data, endResize);
  }

  function endResize(e){
    e.preventDefault();
    $(e.data.parent_target).off(end_action_events, null, endResize);
    $(e.data.parent_target).off(continue_action_events, null, resizing);
  }

  function saveEventState(e){
    // Save the initial event details and container state
    e.data.event_state.container_width = $(e.data.container_target).width();
    e.data.event_state.container_height = $(e.data.container_target).height();
    e.data.event_state.container_left = $(e.data.container_target).offset().left;
    e.data.event_state.container_top = $(e.data.container_target).offset().top;
    e.data.event_state.mouse_x = (e.clientX || e.pageX || e.originalEvent.touches[0].clientX) + $(window).scrollLeft();
    e.data.event_state.mouse_y = (e.clientY || e.pageY || e.originalEvent.touches[0].clientY) + $(window).scrollTop();

    // This is a fix for mobile safari
    // For some reason it does not allow a direct copy of the touches property
    if(typeof e.originalEvent.touches !== 'undefined'){
      e.data.event_state.touches = [];
      $.each(e.originalEvent.touches, function(i, ob){
        e.data.event_state.touches[i] = {};
        e.data.event_state.touches[i].clientX = 0+ob.clientX;
        e.data.event_state.touches[i].clientY = 0+ob.clientY;
      });
    }
    e.data.event_state.evnt = e;
  }

  function resizing(e){
    var mouse={},width,height,left,top,offset=$(e.data.container_target).offset();
    mouse.x = (e.clientX || e.pageX || e.originalEvent.touches[0].clientX) + $(window).scrollLeft();
    mouse.y = (e.clientY || e.pageY || e.originalEvent.touches[0].clientY) + $(window).scrollTop();

    // Position image differently depending on the corner dragged and constraints
    if( $(e.data.event_state.evnt.target).hasClass('resize-handle-se') ){
      width = mouse.x - e.data.event_state.container_left;
      height = mouse.y  - e.data.event_state.container_top;
      left = e.data.event_state.container_left;
      top = e.data.event_state.container_top;
    } else if($(e.data.event_state.evnt.target).hasClass('resize-handle-sw') ){
      width = e.data.event_state.container_width - (mouse.x - e.data.event_state.container_left);
      height = mouse.y  - e.data.event_state.container_top;
      left = mouse.x;
      top = e.data.event_state.container_top;
    } else if($(e.data.event_state.evnt.target).hasClass('resize-handle-nw') ){
      width = e.data.event_state.container_width - (mouse.x - e.data.event_state.container_left);
      height = e.data.event_state.container_height - (mouse.y - e.data.event_state.container_top);
      left = mouse.x;
      top = mouse.y;
      if(constrain){
        top = mouse.y - ((width / e.data.orig_src.width * e.data.orig_src.height) - height);
      }
    } else if($(e.data.event_state.evnt.target).hasClass('resize-handle-ne') ){
      width = mouse.x - e.data.event_state.container_left;
      height = e.data.event_state.container_height - (mouse.y - e.data.event_state.container_top);
      left = e.data.event_state.container_left;
      top = mouse.y;
      if(constrain){
        top = mouse.y - ((width / e.data.orig_src.width * e.data.orig_src.height) - height);
      }
    }

    // Optionally maintain aspect ratio
    if(constrain){
      height = e.data.get_constrained_height_callback(width, e.data.orig_src.width, e.data.orig_src.height);
    }

    //Commented out the limitation on max_height to account for images with wonky aspect ratios
    if(width > min_width && height > min_height /*&& width < max_width && height < max_height*/){
      // To improve performance you might limit how often resizeImage() is called
      e.data.resize_image_callback(width, height, e.data.orig_src);
      // Without this Firefox will not re-calculate the the image dimensions until drag end
      $(e.data.container_target).offset({'left': left, 'top': top});
    }
  }

  function getConstrainedHeight(width, origWidth, origHeight) {
    var ratio = origWidth / origHeight;
    return width/ratio;
  }

  function getConstrainedWidth(height, origWidth, origHeight) {
    var ratio = origWidth / origHeight;
    return height * ratio;
  }

  function resizeImage(width, height, image){
    if(image == undefined) {
      image = orig_src;
    }
    var context = resize_canvas.getContext('2d');
    var compositeOperation = context.globalCompositeOperation;
    window.VTCROP_DEBUG && console.log("resizing to: " + width + "w x " + height + "h");
    resize_canvas.width = width;
    resize_canvas.height = height;
    context.globalCompositeOperation = "destination-over";
    context.fillStyle = image_background_str;
    context.fillRect(0,0,width,height);
    context.globalCompositeOperation = compositeOperation;
    context.drawImage(image, 0, 0, width, height);
    $(image_target).attr('src', resize_canvas.toDataURL(image_type));
  }

  function scaleImage(width, height, image, callback){
    if(image == undefined) {
      image = orig_src;
    }
    var scaledSrc,
        sizedWidth = image.naturalWidth,
        sizedHeight = image.naturalHeight;

    var scalingDirection = (sizedWidth < width && sizedHeight < height) ? "up" : "down";

    window.VTCROP_DEBUG && console.log({ scalingDirection: scalingDirection,
      sizedWidth: sizedWidth,
      sizedHeight: sizedHeight,
      width: width,
      height: height,
      src: image.src
    });
    scaledSrc = image.src;

    var tmp = new Image();
    tmp.onload = function scaleTmpOnload() {
      var tmpCanvas = document.createElement('canvas'),
          tmpContext = tmpCanvas.getContext('2d'),
          compositeOperation = tmpContext.globalCompositeOperation;

      if(scalingDirection === "down") {
        sizedWidth /= 2;
        sizedHeight /= 2;
      } else {
        sizedWidth += (sizedWidth / 2);
        sizedHeight += (sizedHeight / 2);
      }

      if((scalingDirection === "down" && sizedWidth < width) || sizedWidth > width) sizedWidth = width;
      if((scalingDirection === "down" && sizedHeight < height) || sizedHeight > height) sizedHeight = height;

      tmpCanvas.width = sizedWidth;
      tmpCanvas.height = sizedHeight;

      tmpContext.globalCompositeOperation = "destination-over";
      tmpContext.fillStyle = image_background_str;
      tmpContext.fillRect(0,0,width,height);
      tmpContext.globalCompositeOperation = compositeOperation;

      tmpContext.drawImage(tmp, 0, 0, sizedWidth, sizedHeight);
      scaledSrc = tmpCanvas.toDataURL(image_type);
      window.VTCROP_DEBUG && console.log({ scaledSrc: scaledSrc });

      if((scalingDirection === "down" && (sizedWidth <= width || sizedHeight <= height))
          || (sizedWidth >= width || sizedHeight >= height)) {
        callback(scaledSrc);
      } else {
        tmp.src = scaledSrc;
      }
    };
    tmp.src = scaledSrc;
  }

  function startMoving(e){
    e.preventDefault();
    e.stopPropagation();
    saveEventState(e);
    $(e.data.parent_target).on(continue_action_events, null, e.data, moving);
    $(e.data.parent_target).on(end_action_events, null, e.data, endMoving);
  }

  function endMoving(e){
    e.preventDefault();
    $(e.data.parent_target).off(end_action_events, null, endMoving);
    $(e.data.parent_target).off(continue_action_events, null, moving);
  }

  function moving(e){
    var  mouse={}, touches;
    e.preventDefault();
    e.stopPropagation();

    touches = e.originalEvent.touches;

    mouse.x = (e.clientX || e.pageX || touches[0].clientX) + $(window).scrollLeft();
    mouse.y = (e.clientY || e.pageY || touches[0].clientY) + $(window).scrollTop();
    $(e.data.container_target).offset({
      'left': mouse.x - ( e.data.event_state.mouse_x - e.data.event_state.container_left ),
      'top': mouse.y - ( e.data.event_state.mouse_y - e.data.event_state.container_top )
    });
    // Watch for pinch zoom gesture while moving
    if(e.data.event_state.touches && e.data.event_state.touches.length > 1 && touches.length > 1){
      var width = e.data.event_state.container_width, height = e.data.event_state.container_height;
      var a = e.data.event_state.touches[0].clientX - e.data.event_state.touches[1].clientX;
      a = a * a;
      var b = e.data.event_state.touches[0].clientY - e.data.event_state.touches[1].clientY;
      b = b * b;
      var dist1 = Math.sqrt( a + b );

      a = e.originalEvent.touches[0].clientX - touches[1].clientX;
      a = a * a;
      b = e.originalEvent.touches[0].clientY - touches[1].clientY;
      b = b * b;
      var dist2 = Math.sqrt( a + b );

      var ratio = dist2 /dist1;

      width = width * ratio;
      height = height * ratio;
      // To improve performance you might limit how often resizeImage() is called
      e.data.resize_image_callback(width, height, e.data.orig_src);
    }
  }

  function preScale(prescale_options) {
    var use_orig_ar = prescale_options.use_orig_ar != undefined ? prescale_options.use_orig_ar : false,
        line_up_overlay = prescale_options.line_up_overlay != undefined ? prescale_options.line_up_overlay : false;

    function preScaleCallback(imgData) {
      image_target.src = imgData;
    }

    $("<img/>")
        .attr("src", $(image_target).attr("src"))
        .load(function() {
          var square = this.height == this.width;
          var vertical = square != true && this.height > this.width;
          var origHeight = use_orig_ar ? orig_src.height : this.height,
              origWidth = use_orig_ar ? orig_src.width : this.width;
          if((square || vertical) && this.height != crop_height) {
            var new_height = crop_height,
                new_width = getConstrainedWidth(new_height, origWidth, origHeight);
            if(new_width > crop_width) {
              new_width = crop_width;
              new_height = getConstrainedHeight(new_width, origWidth, origHeight);
            }
            scaleImage(new_width, new_height, this, preScaleCallback);
          }
          else if(this.width != crop_width) {
            var new_width = crop_width,
                new_height = getConstrainedHeight(new_width, origWidth, origHeight);
            if(new_height > crop_height) {
              new_height = crop_height;
              new_width = getConstrainedWidth(new_height, origWidth, origHeight);
            }
            scaleImage(new_width, new_height, this, preScaleCallback);
          }
        });
    if(line_up_overlay) {
      var transformVal = 'translateY(-50%)';
      $container_target.css({
        top: '50%',
        transform: transformVal,
        '-webkit-transform': transformVal,
        '-o-transform': transformVal,
        '-moz-transform': transformVal,
        '-ms-transform': transformVal
      });
    }
  }

  function crop(callback){
    //Find the part of the image that is inside the crop box
    //noinspection LongLine
    var crop_canvas,
        context,
        image_pos_left = $container_target.offset().left,
        image_pos_top = $container_target.offset().top,
        sx = Math.round($(overlay_target).offset().left - image_pos_left),
        sy = Math.round($(overlay_target).offset().top - image_pos_top),
        dx = sx < 0 ? Math.abs(sx) < 5 ? 0 : Math.abs(sx) : 0,
        dy = sy < 0 ? Math.abs(sy) < 5 ? 0 : Math.abs(sy) : 0,
        overlay_width = $(overlay_target).width(),
        overlay_height = $(overlay_target).height(),
        img_width = $container_target.width(),
        img_height = $container_target.height(),
        sWidth = (dx + img_width) > overlay_width ? img_width - ((dx + img_width) - overlay_width) :
            (sx + img_width) > overlay_width ? img_width - ((sx + img_width) - overlay_width)
                : img_width > overlay_width ? overlay_width : img_width,
        sHeight = (dy + img_height) > overlay_height ? img_height - ((dy + img_height) - overlay_height) :
            (sy + img_height) > overlay_height ? img_height - ((sy + img_height) - overlay_height)
                : img_height > overlay_height ? overlay_height : img_height,
        dWidth = sWidth <= overlay_width ? sWidth : overlay_width,
        dHeight = sHeight <= overlay_height ? sHeight : overlay_height;

    sx = sx < 0 ? 0 : sx;
    sy = sy < 0 ? 0 : sy;
    dWidth = dWidth > overlay_width ? overlay_width : dWidth;
    dHeight = dHeight > overlay_height ? overlay_height : dHeight;

    crop_canvas = document.createElement('canvas');
    context = crop_canvas.getContext('2d');
    crop_canvas.width = overlay_width;
    crop_canvas.height = overlay_height;

    //Set the background to the configured image background
    var compositeOperation = context.globalCompositeOperation;
    context.globalCompositeOperation = "destination-over";
    context.fillStyle = image_background_str;
    context.fillRect(0,0,crop_canvas.width,crop_canvas.height);
    context.globalCompositeOperation = compositeOperation;

    var values = {
      canvasWidth: overlay_width,
      canvasHeight: overlay_height,
      sizedWidth: img_width,
      sizedHeight: img_height,
      sx: sx,
      sy: sy,
      sWidth: sWidth,
      sHeight: sHeight,
      dx: dx,
      dy: dy,
      dWidth: dWidth,
      dHeight: dHeight
    };

    window.VTCROP_DEBUG && console.log(values);

    var scaleMap = { count: 0 };
    image_scales.forEach(function(img_scale, idx, arr) {
      scaledCrop(img_scale, values, function(imgData) {
        scaleMap[img_scale.scale] = imgData;
        scaleMap.count += 1;
        if(scaleMap.count == image_scales.length) {
          var returned = [];
          for(var i = 0; i < image_scales.length; i++) {
            returned.push(scaleMap[image_scales[i].scale]);
          }
          callback(returned);
        }
      });
    });
  }

  function scaledCrop(imageScale, values, callback) {
    var canvasWidth = values.canvasWidth * imageScale.scale,
        canvasHeight = values.canvasHeight * imageScale.scale,
        scaledWidth = values.sizedWidth * imageScale.scale,
        scaledHeight = values.sizedHeight * imageScale.scale,
        scaled_img = new Image(),
        sx = values.sx * imageScale.scale,
        sy = values.sy * imageScale.scale,
        sWidth = values.sWidth * imageScale.scale,
        sHeight = values.sHeight * imageScale.scale,
        dx = values.dx * imageScale.scale,
        dy = values.dy * imageScale.scale,
        dWidth = values.dWidth * imageScale.scale,
        dHeight = values.dHeight * imageScale.scale;

    scaled_img.onload = function() {
      window.VTCROP_DEBUG && console.log({ scaled_img_src: scaled_img.src });

      var tmpCanvas = document.createElement('canvas'),
          tmpContext = tmpCanvas.getContext('2d');
      tmpCanvas.width = canvasWidth;
      tmpCanvas.height = canvasHeight;

      var compositeOperation = tmpContext.globalCompositeOperation;
      tmpContext.globalCompositeOperation = "destination-over";
      tmpContext.fillStyle = image_background_str;
      tmpContext.fillRect(0,0,tmpCanvas.width,tmpCanvas.height);
      tmpContext.globalCompositeOperation = compositeOperation;

      var scaledValues = {
        scale: imageScale.scale,
        quality: imageScale.quality,
        sizedWidth: scaledWidth,
        sizedHeight: scaledHeight,
        sx: sx,
        sy: sy,
        sWidth: sWidth,
        sHeight: sHeight,
        dx: dx,
        dy: dy,
        dWidth: dWidth,
        dHeight: dHeight
      };
      window.VTCROP_DEBUG && console.log(scaledValues);

      tmpContext.drawImage(scaled_img, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
      var data = tmpCanvas.toDataURL(image_type, imageScale.quality);
      window.VTCROP_DEBUG && console.log({ scaledCrop: data });
      callback(data);
    };

    scaleImage(scaledWidth, scaledHeight, undefined, function(imgData) {
      scaled_img.src = imgData;
    });
  }

  function destroy() {
    $brief_tips.remove();
    $(image_target).remove();
    $(overlay_target).remove();
    $container_target.remove();
    $(drop_zone_target).find('.resize-handle').remove();
    $(drop_zone_canvas).remove();
    $(drop_zone_target).append(image_target);
    $(image_target).removeAttr('style');
    $(image_target).attr('src', orig_src.src);
    $overlayBefore.remove();
    $overlayAfter.remove();
    $overlayInnerBefore.remove();
    $overlayInnerAfter.remove();
    $parent_target.off('mousedown touchstart', ' .resize-handle', startResize);
    $parent_target.off('mousedown touchstart', ' img', startMoving);

    is_destroyed = true;
  }

  function getImageTarget() {
    return image_target;
  }

  function setImageSrc(image_src) {
    $(image_target).attr('src', image_src);
    orig_src.src = image_src;
    preScale({line_up_overlay: true});
    $(drop_zone_canvas).remove();
    $container_target.css({display: container_display});
    $parent_target.find(done_cropping_button_selector).css({display: done_cropping_button_display});

    if(is_destroyed) {
      init();
    }
  }

  this.preScale = preScale;
  this.crop = crop;
  this.destroy = destroy;
  this.getImageTarget = getImageTarget;
  this.setImageSrc = setImageSrc;

  init();
}
