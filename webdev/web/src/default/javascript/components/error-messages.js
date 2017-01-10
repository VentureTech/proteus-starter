/**
 * Moves error messages to the source of the error message.
 */
var CN_HAS_ERROR = "has-error";

function errorMessageCleanup(){
	$("." + CN_HAS_ERROR).each(function (idx, el) {
		var $el = $(el);
		$el.removeClass(CN_HAS_ERROR);
		$el.find('.error-message').remove();
	});
}

function setupErrorMessages(ctx){
	var $ctx = $(ctx || document), hasErrors;
	var $mcs = $ctx.hasClass('message-container') ? $ctx : $ctx.find('.message-container');
	if($mcs.length === 0)
		return;
	$mcs.each(function (idx, mc) {
		var $mc = $(mc);
		$mc.find('.error [data-source]').each(function (idx, el) {
			var $el = $(el), id = $el.data('source');
			var $prop = $('#' + id);
			if($prop.length === 0)
				return;
			if($prop.prop('tagName').toLowerCase() != 'div') {
				$prop.wrap("<span></span>");
				$prop.parent().addClass(CN_HAS_ERROR);
				$prop.parent().append('<span class="error-message"><span class="error-text">' + $el.text() + '</span></span>');
				hasErrors = true;
			} else {
				$prop.addClass(CN_HAS_ERROR);
				$prop.append('<div class="error-message"><span class="error-text">' + $el.text() + '</span></div>');
				hasErrors = true;
			}
			$el.parent().remove();
		});
	});
	if(hasErrors) {
		$mcs.append('<div class="message error"><span class="brief">Please review the errors below</span></div>');
		//noinspection JSUnresolvedFunction - defined in MIWT util.js
		var scrollTo = $('.error-message').parent().first()[0];
		setTimeout(function(){
			scrollTo.scrollIntoViewIfNeeded(true);
		}, 50);
	}

	// if($mc.children().length === 0)
	// 	$mc.addClass('empty');
}