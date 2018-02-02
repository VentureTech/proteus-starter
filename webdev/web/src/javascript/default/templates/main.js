/* Required. */
//=require ../partials/charts.js
//=require ../partials/custom-dropdown.js
//=require ../partials/data-download.js
//=require ../partials/error-messages.js
//=require ../partials/nav-highlight.js
//=require ../partials/responsive-view.js
//=require ../partials/select2.js
//=require ../partials/tooltips.js
//=require ../partials/popovers.js

/* Bundle */
//=require ../partials/top-menu.js

// Note: openSidebar function is declared in the HTML of the page (header)
jQuery(function () {

	setupErrorMessages()

	initSelect2()

	handleDataDownload()

	enableTooltips()

	enablePopovers()

	setupCharts()

	navHighlight()

	setupCustomDropdowns()

	$('form.miwt-form').each(function (idx, form) {
		//noinspection JSUnusedGlobalSymbols
		form.submit_options = {
			preProcessNode: function (data) {
				destroySelect2(document.getElementById(data.refid))
				return data.content
			},
			postProcessNode: function (data) {
				$.each(data, function (idx, ctx) {
					initSelect2(ctx)
					handleDataDownload(ctx)
					enableTooltips(ctx)
					enablePopovers(ctx)
					setupCharts(ctx)
					navHighlight()
					setupCustomDropdowns(ctx)
					if ($(ctx).hasClass('message-container') || $(ctx).find('.message-container').length > 0)
						errorMessageCleanup()
					setTimeout(function () {
						setupErrorMessages(ctx)
					}, 1)
				})
			},
			postUpdate: function () {
				$(this).trigger('vs:miwt-post-update')
			}
		}
	})

})