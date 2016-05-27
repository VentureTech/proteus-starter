/* Required. */
//=require ../components/responsive-view.js 
//=require ../components/select2.js 
//=require ../components/error-messages.js 
//=require ../components/charts.js 
//=require ../components/data-download.js 
//=require ../components/nav-highlight.js 
//=require ../components/tooltips.js 

/* Bundle */
//=require ../components/top-menu.js 


// Note: openSidebar function is declared in the HTML of the page (header)
jQuery(function () {

	setupErrorMessages();

	initSelect2();

	handleDataDownload();

	enableTooltips();

	setupCharts();

	navHighlight();

	$('form.miwt-form').each(function (idx, form) {
		//noinspection JSUnusedGlobalSymbols
		form.submit_options = {
			preProcessNode: function (data) {
				destroySelect2(document.getElementById(data.refid));
				return data.content;
			},
			postProcessNode: function (data) {
				$.each(data, function (idx, ctx) {
					initSelect2(ctx);
					handleDataDownload(ctx);
					enableTooltips(ctx);
					setupCharts(ctx);
					navHighlight();
					if($(ctx).hasClass('message-container') || $(ctx).find('.message-container').length > 0)
						errorMessageCleanup();
					setTimeout(function () {
						setupErrorMessages(ctx);
					}, 1);
				});
			},
			postUpdate: function () {
				$(this).trigger('vs:miwt-post-update');
			}
		};
	});


});